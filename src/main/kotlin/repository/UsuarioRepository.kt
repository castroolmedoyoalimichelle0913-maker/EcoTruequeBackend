package com.example.repository

import com.example.auth.PasswordUtils
import com.example.models.Usuario
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
}