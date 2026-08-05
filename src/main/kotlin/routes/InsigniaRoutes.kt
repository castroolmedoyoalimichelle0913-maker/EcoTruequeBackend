package com.example.routes

import com.example.models.Insignia
import com.example.repository.InsigniaRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.insigniaRoutes() {

    val repository = InsigniaRepository()

    authenticate("auth-jwt") {

        route("/insignias") {

            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val todas = repository.obtenerTodas()
                    val obtenidas = repository.obtenerInsigniasUsuario(userId)

                    val resultado = todas.map { insignia ->
                        insignia.copy(
                            obtenida = obtenidas.containsKey(insignia.id),
                            fechaObtenida = obtenidas[insignia.id]
                        )
                    }

                    call.respond(resultado)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener insignias: ${e.message}")
                    )
                }
            }

            get("/mias") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val todas = repository.obtenerTodas()
                    val obtenidas = repository.obtenerInsigniasUsuario(userId)

                    val resultado = todas
                        .filter { obtenidas.containsKey(it.id) }
                        .map { it.copy(obtenida = true, fechaObtenida = obtenidas[it.id]) }

                    call.respond(resultado)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener tus insignias: ${e.message}")
                    )
                }
            }
        }
    }
}
