package com.example.routes

import com.example.auth.JwtService
import com.example.auth.PasswordUtils
import com.example.models.LoginRequest
import com.example.models.LoginResponse
import com.example.models.PublicProfile
import com.example.models.Usuario
import com.example.repository.InsigniaRepository
import com.example.repository.MaterialRepository
import com.example.repository.PropuestaRepository
import com.example.repository.UsuarioRepository
import com.example.tables.Usuarios
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.usuarioRoutes() {

    val repository = UsuarioRepository()

    route("/usuarios") {

        post("/registro") {
            try {
                val usuario = call.receive<Usuario>()

                val existente = repository.buscarPorCorreo(usuario.correo)

                if (existente != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "Ese correo ya esta registrado")
                    )
                    return@post
                }

                repository.registrar(usuario)

                call.respond(
                    HttpStatusCode.Created,
                    mapOf("mensaje" to "Usuario registrado correctamente")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al registrar usuario: ${e.message}")
                )
            }
        }

        post("/login") {
            try {
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
                        mapOf("error" to "Contrasena incorrecta")
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
                        id = usuario[Usuarios.id].value,
                        nombre = usuario[Usuarios.nombre],
                        correo = usuario[Usuarios.correo],
                        puntos = usuario[Usuarios.puntos],
                        fotoPerfil = usuario[Usuarios.fotoPerfil]
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error en login: ${e.message}")
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID invalido"))
                return@get
            }

            try {
                val usuario = repository.obtenerPorId(id)
                if (usuario == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                    return@get
                }

                val materialRepository = MaterialRepository()
                val propuestaRepository = PropuestaRepository()
                val numeroMateriales = materialRepository.obtenerPorUsuario(id).size
                val numeroTrueques = propuestaRepository.contarCompletados(id)

                call.respond(
                    PublicProfile(
                        id = usuario[Usuarios.id].value,
                        nombre = usuario[Usuarios.nombre],
                        correo = usuario[Usuarios.correo],
                        puntos = usuario[Usuarios.puntos],
                        fotoPerfil = usuario[Usuarios.fotoPerfil],
                        fechaRegistro = usuario[Usuarios.fechaRegistro],
                        numeroMateriales = numeroMateriales,
                        numeroTrueques = numeroTrueques
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener perfil: ${e.message}")
                )
            }
        }
    }
}

fun Route.miPerfilRoutes() {

    val repository = UsuarioRepository()

    authenticate("auth-jwt") {

        route("/miperfil") {

            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val usuario = repository.obtenerPorId(userId)
                    if (usuario == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                        return@get
                    }

                    val materialRepository = MaterialRepository()
                    val propuestaRepository = PropuestaRepository()
                    val numeroMateriales = materialRepository.obtenerPorUsuario(userId).size
                    val numeroTrueques = propuestaRepository.contarCompletados(userId)

                    call.respond(
                        PublicProfile(
                            id = usuario[Usuarios.id].value,
                            nombre = usuario[Usuarios.nombre],
                            correo = usuario[Usuarios.correo],
                            puntos = usuario[Usuarios.puntos],
                            fotoPerfil = usuario[Usuarios.fotoPerfil],
                            fechaRegistro = usuario[Usuarios.fechaRegistro],
                            numeroMateriales = numeroMateriales,
                            numeroTrueques = numeroTrueques
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al obtener tu perfil: ${e.message}")
                    )
                }
            }

            post("/foto") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, String>>()
                    val foto = request["foto"] ?: ""

                    repository.actualizarFotoPerfil(userId, foto)
                    call.respond(mapOf("mensaje" to "Foto de perfil actualizada"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar foto: ${e.message}")
                    )
                }
            }
        }
    }
}
