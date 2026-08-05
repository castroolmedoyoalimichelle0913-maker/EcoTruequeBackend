package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Bitacora : IntIdTable("bitacora") {

    val usuarioId = integer("usuario_id").nullable()
    val correo = varchar("correo", 150)
    val tipoUsuario = varchar("tipo_usuario", 20)
    val accion = varchar("accion", 200)
    val ip = varchar("ip", 60).nullable()
    val fecha = varchar("fecha", 30)
}
