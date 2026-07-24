package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Materiales : IntIdTable("materiales") {

    val nombre = varchar("nombre", 100)
    val descripcion = varchar("descripcion", 255)
    val categoria = varchar("categoria", 80)
    val puntos = integer("puntos")
    val imagen = text("imagen").nullable()
    val latitud = double("latitud").nullable()
    val longitud = double("longitud").nullable()
    val usuarioId = integer("usuario_id").nullable()
}