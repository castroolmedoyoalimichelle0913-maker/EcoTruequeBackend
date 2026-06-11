package com.example

import com.example.models.Trueque
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {

        get("/") {
            call.respondText("EcoTrueque funcionando")
        }

        get("/api/trueques") {

            val lista = listOf(

                Trueque(
                    id = 1,
                    producto = "Botellas recicladas",
                    usuario = "Ana"
                ),

                Trueque(
                    id = 2,
                    producto = "Cartón reutilizable",
                    usuario = "Carlos"
                )
            )

            call.respond(lista)
        }
    }
}