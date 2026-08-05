package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Chats : IntIdTable("chats") {

    val propuestaId = integer("propuesta_id").nullable()
    val usuario1Id = integer("usuario1_id")
    val usuario2Id = integer("usuario2_id")
    val fechaCreacion = varchar("fecha_creacion", 30)
}
