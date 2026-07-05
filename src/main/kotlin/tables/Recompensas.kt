package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Recompensas : IntIdTable("recompensas") {

    val nombre = varchar("nombre", 100)
    val descripcion = varchar("descripcion", 255)
    val puntosNecesarios = integer("puntos_necesarios")
    val imagen = varchar("imagen", 255).nullable()
    val disponible = bool("disponible").default(true)
}