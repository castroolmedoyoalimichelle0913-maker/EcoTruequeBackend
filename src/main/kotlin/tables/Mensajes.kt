package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Mensajes : IntIdTable("mensajes") {

    val chatId = integer("chat_id")
    val emisorId = integer("emisor_id")
    val contenido = text("contenido")
    val hora = varchar("hora", 30)
    val leido = integer("leido").default(0)
}
