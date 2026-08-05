package com.example.repository

import com.example.auth.PasswordUtils
import com.example.models.Usuario
import com.example.models.AdminUsuario
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UsuarioRepository {

    fun registrar(usuario: Usuario) {
        transaction {
            Usuarios.insert {
                it[nombre] = usuario.nombre
                it[correo] = usuario.correo
                it[password] = PasswordUtils.hash(usuario.password)
                it[telefono] = usuario.telefono
                it[puntos] = usuario.puntos
                it[fechaRegistro] = usuario.fechaRegistro
                it[fotoPerfil] = usuario.fotoPerfil
                it[rol] = usuario.rol
                it[activo] = usuario.activo
            }
        }
    }

    fun buscarPorCorreo(correo: String): ResultRow? {
        return transaction {
            Usuarios
                .selectAll()
                .where { Usuarios.correo eq correo }
                .limit(1)
                .toList()
                .firstOrNull()
        }
    }

    fun login(correo: String): ResultRow? {
        return buscarPorCorreo(correo)
    }

    fun obtenerPorId(id: Int): ResultRow? {
        return transaction {
            Usuarios
                .selectAll()
                .where { Usuarios.id eq id }
                .limit(1)
                .toList()
                .firstOrNull()
        }
    }

    fun actualizarFotoPerfil(id: Int, foto: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[fotoPerfil] = foto
        } > 0
    }

    fun actualizarNombre(id: Int, nombre: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.nombre] = nombre
        } > 0
    }

    fun actualizarCorreo(id: Int, correo: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.correo] = correo
        } > 0
    }

    fun actualizarPassword(id: Int, passwordNueva: String): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.password] = PasswordUtils.hash(passwordNueva)
        } > 0
    }

    fun cambiarNotificaciones(id: Int, activas: Boolean): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.notificaciones] = activas
        } > 0
    }

    fun obtenerRol(id: Int): String? = transaction {
        Usuarios
            .select(Usuarios.rol)
            .where { Usuarios.id eq id }
            .limit(1)
            .firstOrNull()
            ?.get(Usuarios.rol)
    }

    fun tieneNotificaciones(id: Int): Boolean = transaction {
        Usuarios
            .select(Usuarios.notificaciones)
            .where { Usuarios.id eq id }
            .limit(1)
            .firstOrNull()
            ?.get(Usuarios.notificaciones) ?: true
    }

    fun setActivo(id: Int, activo: Boolean): Boolean = transaction {
        Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.activo] = activo
        } > 0
    }

    fun sumarPuntos(id: Int, puntosGanados: Int): Boolean = transaction {
        val actual = Usuarios
            .select(Usuarios.puntos)
            .where { Usuarios.id eq id }
            .limit(1)
            .firstOrNull()
            ?.get(Usuarios.puntos) ?: 0

        Usuarios.update({ Usuarios.id eq id }) {
            it[puntos] = actual + puntosGanados
        } > 0
    }

    fun listarUsuarios(): List<AdminUsuario> = transaction {
        Usuarios
            .selectAll()
            .orderBy(Usuarios.id, SortOrder.DESC)
            .map {
                AdminUsuario(
                    id = it[Usuarios.id].value,
                    nombre = it[Usuarios.nombre],
                    correo = it[Usuarios.correo],
                    puntos = it[Usuarios.puntos],
                    rol = it[Usuarios.rol],
                    activo = it[Usuarios.activo],
                    fechaRegistro = it[Usuarios.fechaRegistro]
                )
            }
    }

    fun eliminar(id: Int): Boolean = transaction {
        Usuarios.deleteWhere { Usuarios.id eq id } > 0
    }
}
