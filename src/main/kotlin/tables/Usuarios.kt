package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Usuarios : IntIdTable("usuarios") {

    val nombre = varchar("nombre", 100)
    val correo = varchar("correo", 150).uniqueIndex()
    val password = varchar("password", 255)
    val telefono = varchar("telefono", 20).nullable()
    val puntos = integer("puntos").default(0)
    val fechaRegistro = varchar("fecha_registro", 30)
}