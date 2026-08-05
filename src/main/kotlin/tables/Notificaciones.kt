package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Notificaciones : IntIdTable("notificaciones") {

    val usuarioId = integer("usuario_id")
    val tipo = varchar("tipo", 30)
    val titulo = varchar("titulo", 150)
    val mensaje = text("mensaje")
    val leido = integer("leido").default(0)
    val fecha = varchar("fecha", 30)
}
