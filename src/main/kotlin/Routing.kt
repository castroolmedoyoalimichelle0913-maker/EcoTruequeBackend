package com.example

import com.example.routes.chatRoutes
import com.example.routes.insigniaRoutes
import com.example.routes.materialRoutes
import com.example.routes.miPerfilRoutes
import com.example.routes.notificacionRoutes
import com.example.routes.propuestaRoutes
import com.example.routes.usuarioRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        get("/") {
            call.respondText("🌱 EcoTrueque Backend funcionando")
        }

        usuarioRoutes()
        miPerfilRoutes()
        materialRoutes()
        propuestaRoutes()
        chatRoutes()
        notificacionRoutes()
        insigniaRoutes()

        authenticate("auth-jwt") {
            get("/perfil") {
                call.respond(
                    mapOf("mensaje" to "Acceso autorizado")
                )
            }
        }
    }
}
