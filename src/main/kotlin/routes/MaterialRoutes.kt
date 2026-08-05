package com.example.routes

import com.example.models.Material
import com.example.repository.MaterialRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.materialRoutes() {

    val repository = MaterialRepository()

    authenticate("auth-jwt") {

        route("/materiales") {

            get {
                try {
                    call.respond(repository.obtenerTodos())
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener materiales: ${e.message}")
                    )
                }
            }

            get("/mis") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(repository.obtenerPorUsuario(userId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener tus materiales: ${e.message}")
                    )
                }
            }

            get("/buscar") {
                try {
                    val query = call.request.queryParameters["q"]
                    val categoria = call.request.queryParameters["categoria"]
                    val puntosMin = call.request.queryParameters["puntosMin"]?.toIntOrNull()
                    val puntosMax = call.request.queryParameters["puntosMax"]?.toIntOrNull()
                    val desde = call.request.queryParameters["desde"]
                    val hasta = call.request.queryParameters["hasta"]
                    val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                    val lng = call.request.queryParameters["lng"]?.toDoubleOrNull()
                    val distancia = call.request.queryParameters["distanciaKm"]?.toDoubleOrNull()

                    val resultado = repository.buscar(
                        query = query,
                        categoria = categoria,
                        puntosMin = puntosMin,
                        puntosMax = puntosMax,
                        desde = desde,
                        hasta = hasta,
                        lat = lat,
                        lng = lng,
                        distanciaKm = distancia
                    )

                    call.respond(resultado)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al buscar materiales: ${e.message}")
                    )
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@get
                }

                val material = try {
                    repository.obtenerPorId(id)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener material: ${e.message}")
                    )
                    return@get
                }

                if (material == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                    return@get
                }

                call.respond(material)
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val material = call.receive<Material>()
                    val ahora = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val materialConUsuario = material.copy(
                        usuarioId = userId,
                        fechaPublicacion = material.fechaPublicacion ?: ahora
                    )
                    repository.insertar(materialConUsuario)

                    call.respond(
                        HttpStatusCode.Created,
                        mapOf("mensaje" to "Material agregado")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al crear material: ${e.message}")
                    )
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@put
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val existente = repository.obtenerPorId(id)
                    if (existente == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                        return@put
                    }

                    if (existente.usuarioId != userId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No puedes editar material de otro usuario"))
                        return@put
                    }

                    val material = call.receive<Material>()
                    val actualizado = repository.actualizar(id, material)

                    call.respond(mapOf("mensaje" to "Material actualizado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar material: ${e.message}")
                    )
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@delete
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val existente = repository.obtenerPorId(id)
                    if (existente == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                        return@delete
                    }

                    if (existente.usuarioId != userId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No puedes eliminar material de otro usuario"))
                        return@delete
                    }

                    val eliminado = repository.eliminar(id)

                    call.respond(mapOf("mensaje" to "Material eliminado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al eliminar material: ${e.message}")
                    )
                }
            }
        }
    }
}
