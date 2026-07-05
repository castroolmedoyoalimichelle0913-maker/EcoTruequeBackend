package com.example.routes

import com.example.auth.JwtService
import com.example.auth.PasswordUtils
import com.example.models.LoginRequest
import com.example.models.LoginResponse
import com.example.models.Usuario
import com.example.repository.UsuarioRepository
import com.example.tables.Usuarios
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.usuarioRoutes() {

    val repository = UsuarioRepository()

    route("/usuarios") {

        post("/registro") {
            val usuario = call.receive<Usuario>()

            val existente = repository.buscarPorCorreo(usuario.correo)

            if (existente != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "Ese correo ya está registrado")
                )
                return@post
            }

            repository.registrar(usuario)

            call.respond(
                HttpStatusCode.Created,
                mapOf("mensaje" to "Usuario registrado correctamente")
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val usuario = repository.buscarPorCorreo(request.correo)

            if (usuario == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Usuario no encontrado")
                )
                return@post
            }

            val passwordCorrecta = PasswordUtils.verify(
                request.password,
                usuario[Usuarios.password]
            )

            if (!passwordCorrecta) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Contraseña incorrecta")
                )
                return@post
            }

            val token = JwtService.generateToken(
                id = usuario[Usuarios.id].value,
                correo = usuario[Usuarios.correo]
            )

            call.respond(
                LoginResponse(
                    token = token,
                    nombre = usuario[Usuarios.nombre],
                    correo = usuario[Usuarios.correo],
                    puntos = usuario[Usuarios.puntos]
                )
            )
        }
    }
}
