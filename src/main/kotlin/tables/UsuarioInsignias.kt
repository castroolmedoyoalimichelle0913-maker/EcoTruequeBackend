package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UsuarioInsignias : IntIdTable("usuario_insignias") {

    val usuarioId = integer("usuario_id")
    val insigniaId = integer("insignia_id")
    val fechaObtenida = varchar("fecha_obtenida", 30)
}
