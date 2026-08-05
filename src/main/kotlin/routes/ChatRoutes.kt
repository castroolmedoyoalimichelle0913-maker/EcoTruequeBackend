package com.example.routes

import com.example.models.Mensaje
import com.example.repository.ChatRepository
import com.example.repository.NotificacionRepository
import com.example.repository.UsuarioRepository
import com.example.tables.Chats
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

fun Route.chatRoutes() {

    val repository = ChatRepository()
    val notificacionRepository = NotificacionRepository()
    val usuarioRepository = UsuarioRepository()

    val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    authenticate("auth-jwt") {

        route("/chats") {

            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(repository.obtenerPorUsuario(userId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener conversaciones: ${e.message}")
                    )
                }
            }

            get("/noLeidos") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(mapOf("cantidad" to repository.contarNoLeidosTotal(userId)))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al contar mensajes no leídos: ${e.message}")
                    )
                }
            }

            get("/propuesta/{propuestaId}") {
                val propuestaId = call.parameters["propuestaId"]?.toIntOrNull()
                if (propuestaId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@get
                }

                val chat = repository.obtenerPorPropuesta(propuestaId)
                if (chat == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Chat no encontrado"))
                    return@get
                }

                call.respond(chat)
            }

            post("/directo") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, Int>>()
                    val otroUsuarioId = request["otroUsuarioId"]

                    if (otroUsuarioId == null || otroUsuarioId == userId) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Destinatario invalido"))
                        return@post
                    }

                    val otro = usuarioRepository.obtenerPorId(otroUsuarioId)
                    if (otro == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                        return@post
                    }

                    val ahora = LocalDateTime.now().format(formato)
                    val chatId = repository.crearDirecto(userId, otroUsuarioId, ahora)

                    val chat = repository.obtenerPorIdUsuario(chatId)
                    call.respond(chat ?: mapOf("chatId" to chatId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al crear conversacion: ${e.message}")
                    )
                }
            }

            get("/{chatId}/mensajes") {
                val chatId = call.parameters["chatId"]?.toIntOrNull()
                if (chatId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@get
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    repository.marcarLeidos(chatId, userId)

                    call.respond(repository.obtenerMensajes(chatId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener mensajes: ${e.message}")
                    )
                }
            }

            post("/{chatId}/mensajes") {
                val chatId = call.parameters["chatId"]?.toIntOrNull()
                if (chatId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                    return@post
                }

                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val emisorId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Mensaje>()
                    val contenido = request.contenido.trim()
                    if (contenido.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El mensaje no puede estar vacío"))
                        return@post
                    }

                    val ahora = LocalDateTime.now().format(formato)
                    repository.enviarMensaje(chatId, emisorId, contenido, ahora)

                    val chat = repository.obtenerPorIdUsuario(chatId)
                    if (chat != null) {
                        val otroId = if (chat.usuario1Id == emisorId) chat.usuario2Id else chat.usuario1Id
                        val otro = usuarioRepository.obtenerPorId(otroId)
                        notificacionRepository.crearSiActivadas(
                            usuarioId = otroId,
                            tipo = "mensaje",
                            titulo = "Nuevo mensaje",
                            mensaje = "${otro?.get(Usuarios.nombre) ?: "Usuario"} te envió un mensaje",
                            fecha = ahora
                        )
                    }

                    call.respond(mapOf("mensaje" to "Mensaje enviado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al enviar mensaje: ${e.message}")
                    )
                }
            }
        }
    }
}
