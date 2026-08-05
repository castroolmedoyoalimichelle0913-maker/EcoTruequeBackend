package com.example.routes

import com.example.repository.NotificacionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificacionRoutes() {

    val repository = NotificacionRepository()

    authenticate("auth-jwt") {

        route("/notificaciones") {

            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(repository.obtenerPorUsuario(userId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener notificaciones: ${e.message}")
                    )
                }
            }

            get("/noLeidas") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    call.respond(mapOf("cantidad" to repository.contarNoLeidas(userId)))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al contar notificaciones: ${e.message}")
                    )
                }
            }

            post("/leidas") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    repository.marcarTodasLeidas(userId)
                    call.respond(mapOf("mensaje" to "Notificaciones marcadas como leídas"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al marcar notificaciones: ${e.message}")
                    )
                }
            }
        }
    }
}
