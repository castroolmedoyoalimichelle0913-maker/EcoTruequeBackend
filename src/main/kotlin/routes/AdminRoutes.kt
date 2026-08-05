package com.example.routes

import com.example.models.Reporte
import com.example.repository.BitacoraRepository
import com.example.repository.MaterialRepository
import com.example.repository.ReporteRepository
import com.example.repository.UsuarioRepository
import com.example.tables.Usuarios
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.adminRoutes() {

    val usuarioRepository = UsuarioRepository()
    val materialRepository = MaterialRepository()
    val reporteRepository = ReporteRepository()
    val bitacoraRepository = BitacoraRepository()

    val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun esAdmin(call: ApplicationCall): Boolean {
        val principal = call.principal<JWTPrincipal>() ?: return false
        val userId = principal.payload.getClaim("id").asInt()
        return usuarioRepository.obtenerRol(userId) == "admin"
    }

    fun registrarAccionAdmin(call: ApplicationCall, accion: String, detalle: String = "") {
        try {
            val principal = call.principal<JWTPrincipal>() ?: return
            val userId = principal.payload.getClaim("id").asInt()
            val usuario = usuarioRepository.obtenerPorId(userId)
            if (usuario != null) {
                bitacoraRepository.registrar(
                    usuarioId = userId,
                    correo = usuario[Usuarios.correo],
                    tipoUsuario = "admin",
                    accion = "$accion $detalle".trim(),
                    ip = obtenerIp(call)
                )
            }
        } catch (_: Exception) {
        }
    }

    authenticate("auth-jwt") {
        route("/admin") {

            get("/usuarios") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@get
                }
                try {
                    call.respond(usuarioRepository.listarUsuarios())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            delete("/usuarios/{id}") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@delete
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@delete
                }
                try {
                    val usuario = usuarioRepository.obtenerPorId(id)
                    usuarioRepository.eliminar(id)
                    registrarAccionAdmin(call, "Elimino al usuario", usuario?.get(Usuarios.correo) ?: "")
                    call.respond(mapOf("mensaje" to "Usuario eliminado"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            post("/usuarios/{id}/suspender") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@post
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }
                try {
                    val usuario = usuarioRepository.obtenerPorId(id)
                    usuarioRepository.setActivo(id, false)
                    registrarAccionAdmin(call, "Suspendio la cuenta de", usuario?.get(Usuarios.correo) ?: "")
                    call.respond(mapOf("mensaje" to "Cuenta suspendida"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            post("/usuarios/{id}/reactivar") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@post
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }
                try {
                    val usuario = usuarioRepository.obtenerPorId(id)
                    usuarioRepository.setActivo(id, true)
                    registrarAccionAdmin(call, "Reactivio la cuenta de", usuario?.get(Usuarios.correo) ?: "")
                    call.respond(mapOf("mensaje" to "Cuenta reactivada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            delete("/materiales/{id}") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@delete
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@delete
                }
                try {
                    materialRepository.eliminar(id)
                    registrarAccionAdmin(call, "Elimino la publicacion", "#$id")
                    call.respond(mapOf("mensaje" to "Publicacion eliminada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            post("/materiales/{id}/borrar-imagen") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@post
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }
                try {
                    materialRepository.limpiarImagen(id)
                    registrarAccionAdmin(call, "Elimino la imagen de la publicacion", "#$id")
                    call.respond(mapOf("mensaje" to "Imagen eliminada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            get("/reportes") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@get
                }
                try {
                    call.respond(reporteRepository.listar())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            post("/reportes/{id}/resolver") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@post
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }
                try {
                    reporteRepository.resolver(id)
                    registrarAccionAdmin(call, "Resolvio el reporte", "#$id")
                    call.respond(mapOf("mensaje" to "Reporte resuelto"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            delete("/reportes/{id}") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@delete
                }
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@delete
                }
                try {
                    reporteRepository.eliminar(id)
                    registrarAccionAdmin(call, "Elimino el reporte", "#$id")
                    call.respond(mapOf("mensaje" to "Reporte eliminado"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }

            get("/bitacora") {
                if (!esAdmin(call)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso no autorizado"))
                    return@get
                }
                try {
                    call.respond(bitacoraRepository.listar())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error: ${e.message}"))
                }
            }
        }

        route("/reportes") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Reporte>()
                    val ahora = LocalDateTime.now().format(formato)

                    val id = reporteRepository.crear(
                        request.copy(
                            usuarioReportaId = userId,
                            estado = "pendiente",
                            fecha = ahora
                        )
                    )

                    call.respond(HttpStatusCode.Created, mapOf("mensaje" to "Reporte enviado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al enviar reporte: ${e.message}")
                    )
                }
            }
        }
    }
}
