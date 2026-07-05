package com.example.repository

import com.example.auth.PasswordUtils
import com.example.models.Usuario
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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
}