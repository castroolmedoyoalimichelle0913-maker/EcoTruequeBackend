package com.example.routes

import com.example.models.Material
import com.example.repository.MaterialRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                    return@get
                }

                val material = repository.obtenerPorId(id)

                if (material == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                    return@get
                }

                call.respond(material)
            }

            post {
                try {
                    val material = call.receive<Material>()
                    repository.insertar(material)

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
                    val material = call.receive<Material>()
                    val actualizado = repository.actualizar(id, material)

                    if (!actualizado) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                        return@put
                    }

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
                    val eliminado = repository.eliminar(id)

                    if (!eliminado) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Material no encontrado"))
                        return@delete
                    }

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