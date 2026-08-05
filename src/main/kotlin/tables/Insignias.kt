package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Insignias : IntIdTable("insignias") {

    val nombre = varchar("nombre", 100)
    val descripcion = varchar("descripcion", 255)
    val imagen = varchar("imagen", 30)
    val requerimiento = varchar("requerimiento", 30)
    val cantidadRequerida = integer("cantidad_requerida")
}
