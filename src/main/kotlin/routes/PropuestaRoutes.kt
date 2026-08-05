package com.example.routes

import com.example.models.Propuesta
import com.example.repository.ChatRepository
import com.example.repository.InsigniaRepository
import com.example.repository.MaterialRepository
import com.example.repository.NotificacionRepository
import com.example.repository.PropuestaRepository
import com.example.repository.UsuarioRepository
import com.example.tables.Materiales
import com.example.tables.Propuestas
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.propuestaRoutes() {

    val repository = PropuestaRepository()
    val materialRepository = MaterialRepository()
    val chatRepository = ChatRepository()
    val notificacionRepository = NotificacionRepository()
    val usuarioRepository = UsuarioRepository()
    val insigniaRepository = InsigniaRepository()

    val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    authenticate("auth-jwt") {

        route("/propuestas") {

            get("/mis") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(repository.obtenerPorUsuario(userId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener propuestas: ${e.message}")
                    )
                }
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val oferenteId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Propuesta>()
                    val materialDeseado = materialRepository.obtenerPorId(request.materialDeseadoId)

                    if (materialDeseado == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "El material deseado no existe"))
                        return@post
                    }

                    val receptorId = materialDeseado.usuarioId
                    if (receptorId == null || receptorId == oferenteId) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No puedes proponer sobre tu propio material"))
                        return@post
                    }

                    val miMaterial = materialRepository.obtenerPorId(request.materialOfertaId)
                    if (miMaterial == null || miMaterial.usuarioId != oferenteId) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Selecciona un material tuyo para ofrecer"))
                        return@post
                    }

                    if (repository.existePropuestaActiva(oferenteId, receptorId, request.materialDeseadoId)) {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "Ya tienes una propuesta activa sobre este material"))
                        return@post
                    }

                    val ahora = LocalDateTime.now().format(formato)
                    val id = repository.insertar(
                        request.copy(
                            oferenteId = oferenteId,
                            receptorId = receptorId,
                            estado = "Pendiente",
                            fecha = ahora
                        )
                    )

                    val receptor = usuarioRepository.obtenerPorId(receptorId)
                    notificacionRepository.crearSiActivadas(
                        usuarioId = receptorId,
                        tipo = "propuesta",
                        titulo = "Nueva propuesta de intercambio",
                        mensaje = "${receptor?.get(com.example.tables.Usuarios.nombre) ?: "Alguien"} quiere intercambiar contigo: \"${miMaterial.nombre}\" por \"${materialDeseado.nombre}\"",
                        fecha = ahora
                    )

                    call.respond(HttpStatusCode.Created, mapOf("id" to id, "mensaje" to "Propuesta enviada"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al crear propuesta: ${e.message}")
                    )
                }
            }

            post("/{id}/aceptar") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val propuesta = repository.obtenerPorId(id)
                    if (propuesta == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Propuesta no encontrada"))
                        return@post
                    }

                    if (propuesta[Propuestas.receptorId] != userId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo el receptor puede aceptar la propuesta"))
                        return@post
                    }

                    if (propuesta[Propuestas.estado] != "Pendiente") {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "La propuesta ya fue respondida"))
                        return@post
                    }

                    repository.actualizarEstado(id, "En proceso")

                    materialRepository.marcarEstado(propuesta[Propuestas.materialOfertaId], "reservado")
                    materialRepository.marcarEstado(propuesta[Propuestas.materialDeseadoId], "reservado")

                    val ahora = LocalDateTime.now().format(formato)
                    val oferenteId = propuesta[Propuestas.oferenteId]
                    val receptorId = propuesta[Propuestas.receptorId]
                    chatRepository.crear(id, oferenteId, receptorId, ahora)

                    val receptor = usuarioRepository.obtenerPorId(receptorId)
                    notificacionRepository.crearSiActivadas(
                        usuarioId = oferenteId,
                        tipo = "aceptar",
                        titulo = "Propuesta aceptada",
                        mensaje = "${receptor?.get(com.example.tables.Usuarios.nombre) ?: "El usuario"} aceptó tu propuesta. ¡Ya pueden coordinar el intercambio!",
                        fecha = ahora
                    )

                    call.respond(mapOf("mensaje" to "Propuesta aceptada"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al aceptar propuesta: ${e.message}")
                    )
                }
            }

            post("/{id}/rechazar") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val propuesta = repository.obtenerPorId(id)
                    if (propuesta == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Propuesta no encontrada"))
                        return@post
                    }

                    if (propuesta[Propuestas.receptorId] != userId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo el receptor puede rechazar la propuesta"))
                        return@post
                    }

                    repository.actualizarEstado(id, "Rechazado")

                    materialRepository.marcarEstado(propuesta[Propuestas.materialOfertaId], "disponible")
                    materialRepository.marcarEstado(propuesta[Propuestas.materialDeseadoId], "disponible")

                    val ahora = LocalDateTime.now().format(formato)
                    notificacionRepository.crearSiActivadas(
                        usuarioId = propuesta[Propuestas.oferenteId],
                        tipo = "rechazar",
                        titulo = "Propuesta rechazada",
                        mensaje = "Tu propuesta de intercambio fue rechazada",
                        fecha = ahora
                    )

                    call.respond(mapOf("mensaje" to "Propuesta rechazada"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al rechazar propuesta: ${e.message}")
                    )
                }
            }

            post("/{id}/completar") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val propuesta = repository.obtenerPorId(id)
                    if (propuesta == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Propuesta no encontrada"))
                        return@post
                    }

                    val oferenteId = propuesta[Propuestas.oferenteId]
                    val receptorId = propuesta[Propuestas.receptorId]

                    if (userId != oferenteId && userId != receptorId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No participas en esta propuesta"))
                        return@post
                    }

                    if (propuesta[Propuestas.estado] != "En proceso") {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "La propuesta no está en proceso"))
                        return@post
                    }

                    repository.actualizarEstado(id, "Completado")

                    materialRepository.marcarEstado(propuesta[Propuestas.materialOfertaId], "intercambiado")
                    materialRepository.marcarEstado(propuesta[Propuestas.materialDeseadoId], "intercambiado")

                    val ahora = LocalDateTime.now().format(formato)

                    val materialOferta = materialRepository.obtenerPorId(propuesta[Propuestas.materialOfertaId])
                    val materialDeseado = materialRepository.obtenerPorId(propuesta[Propuestas.materialDeseadoId])

                    val puntosOferente = materialOferta?.puntos ?: 0
                    val puntosReceptor = materialDeseado?.puntos ?: 0

                    if (puntosOferente > 0) usuarioRepository.sumarPuntos(oferenteId, puntosOferente)
                    if (puntosReceptor > 0) usuarioRepository.sumarPuntos(receptorId, puntosReceptor)

                    notificacionRepository.crearSiActivadas(
                        usuarioId = oferenteId,
                        tipo = "completar",
                        titulo = "Intercambio completado",
                        mensaje = "¡Intercambio completado! Ganaste $puntosOferente puntos verdes",
                        fecha = ahora
                    )

                    notificacionRepository.crearSiActivadas(
                        usuarioId = receptorId,
                        tipo = "completar",
                        titulo = "Intercambio completado",
                        mensaje = "¡Intercambio completado! Ganaste $puntosReceptor puntos verdes",
                        fecha = ahora
                    )

                    recompensarInsignias(oferenteId, insigniaRepository, ahora)
                    recompensarInsignias(receptorId, insigniaRepository, ahora)

                    call.respond(mapOf("mensaje" to "Intercambio completado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al completar intercambio: ${e.message}")
                    )
                }
            }
        }
    }
}

fun recompensarInsignias(usuarioId: Int, insigniaRepository: InsigniaRepository, fecha: String) {
    val trueques = insigniaRepository.contarTruequesCompletados(usuarioId)
    val materiales = insigniaRepository.contarMateriales(usuarioId)
    val puntos = insigniaRepository.obtenerPuntos(usuarioId)

    val candidatas = listOf(
        Pair("trueques", trueques),
        Pair("materiales", materiales),
        Pair("puntos", puntos)
    )

    candidatas.forEach { (requerimiento, cantidad) ->
        val insigniaId = insigniaRepository.obtenerPorRequerimiento(requerimiento)
        if (insigniaId != null) {
            val insignia = insigniaRepository.obtenerTodas().firstOrNull { it.id == insigniaId }
            if (insignia != null && cantidad >= insignia.cantidadRequerida) {
                insigniaRepository.asignar(usuarioId, insigniaId, fecha)
            }
        }
    }
}
