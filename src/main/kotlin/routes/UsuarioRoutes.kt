package com.example.routes

import com.example.auth.JwtService
import com.example.auth.PasswordUtils
import com.example.models.LoginRequest
import com.example.models.LoginResponse
import com.example.models.PublicProfile
import com.example.models.Usuario
import com.example.repository.BitacoraRepository
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

fun obtenerIp(call: ApplicationCall): String? {
    return try {
        call.request.headers["X-Forwarded-For"]
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: call.request.local.remoteHost
    } catch (_: Exception) {
        null
    }
}

fun Route.usuarioRoutes() {

    val repository = UsuarioRepository()
    val bitacoraRepository = BitacoraRepository()

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

                repository.registrar(usuario.copy(rol = "usuario", activo = true))

                if (!repository.existeAdmin()) {
                    repository.buscarPorCorreo(usuario.correo)?.let {
                        repository.setRol(it[Usuarios.id].value, "admin")
                    }
                }

                bitacoraRepository.registrar(
                    usuarioId = null,
                    correo = usuario.correo,
                    tipoUsuario = "usuario",
                    accion = "Registro de cuenta",
                    ip = obtenerIp(call)
                )

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

                if (!usuario[Usuarios.activo]) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Tu cuenta ha sido suspendida. Contacta al administrador.")
                    )
                    return@post
                }

                val userId = usuario[Usuarios.id].value
                val rol = usuario[Usuarios.rol]

                val token = JwtService.generateToken(
                    id = userId,
                    correo = usuario[Usuarios.correo]
                )

                bitacoraRepository.registrar(
                    usuarioId = userId,
                    correo = usuario[Usuarios.correo],
                    tipoUsuario = rol,
                    accion = "Inicio de sesion",
                    ip = obtenerIp(call)
                )

                call.respond(
                    LoginResponse(
                        token = token,
                        id = userId,
                        nombre = usuario[Usuarios.nombre],
                        correo = usuario[Usuarios.correo],
                        puntos = usuario[Usuarios.puntos],
                        fotoPerfil = usuario[Usuarios.fotoPerfil],
                        rol = rol,
                        activo = usuario[Usuarios.activo]
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
                val insigniaRepository = InsigniaRepository()
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
                        numeroTrueques = numeroTrueques,
                        rol = usuario[Usuarios.rol],
                        activo = usuario[Usuarios.activo],
                        notificaciones = usuario[Usuarios.notificaciones],
                        numeroInsignias = insigniaRepository.contarInsigniasUsuario(id)
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
                    val insigniaRepository = InsigniaRepository()
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
                            numeroTrueques = numeroTrueques,
                            rol = usuario[Usuarios.rol],
                            activo = usuario[Usuarios.activo],
                            notificaciones = usuario[Usuarios.notificaciones],
                            numeroInsignias = insigniaRepository.contarInsigniasUsuario(userId)
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

            put("/nombre") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, String>>()
                    val nombre = request["nombre"]?.trim()

                    if (nombre.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El nombre no puede estar vacio"))
                        return@put
                    }

                    repository.actualizarNombre(userId, nombre)
                    call.respond(mapOf("mensaje" to "Nombre actualizado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar nombre: ${e.message}")
                    )
                }
            }

            put("/correo") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, String>>()
                    val correo = request["correo"]?.trim()

                    if (correo.isNullOrBlank() || !correo.contains("@")) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Correo invalido"))
                        return@put
                    }

                    val existente = repository.buscarPorCorreo(correo)
                    if (existente != null && existente[Usuarios.id].value != userId) {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "Ese correo ya esta en uso"))
                        return@put
                    }

                    repository.actualizarCorreo(userId, correo)
                    call.respond(mapOf("mensaje" to "Correo actualizado"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar correo: ${e.message}")
                    )
                }
            }

            put("/password") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, String>>()
                    val actual = request["passwordActual"] ?: ""
                    val nueva = request["passwordNueva"] ?: ""

                    if (nueva.length < 6) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "La contrasena debe tener al menos 6 caracteres"))
                        return@put
                    }

                    val usuario = repository.obtenerPorId(userId)
                    if (usuario == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
                        return@put
                    }

                    if (!PasswordUtils.verify(actual, usuario[Usuarios.password])) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "La contrasena actual es incorrecta"))
                        return@put
                    }

                    repository.actualizarPassword(userId, nueva)
                    call.respond(mapOf("mensaje" to "Contrasena actualizada"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar contrasena: ${e.message}")
                    )
                }
            }

            put("/notificaciones") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()

                    val request = call.receive<Map<String, Boolean>>()
                    val activas = request["activas"] ?: true

                    repository.cambiarNotificaciones(userId, activas)
                    call.respond(mapOf("mensaje" to "Preferencias de notificacion actualizadas"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al actualizar preferencias: ${e.message}")
                    )
                }
            }
        }
    }
}
