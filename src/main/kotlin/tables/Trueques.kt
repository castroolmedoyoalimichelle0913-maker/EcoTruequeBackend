package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Trueques : IntIdTable("trueques") {

    val usuario = reference(
        "usuario_id",
        Usuarios,
        onDelete = ReferenceOption.CASCADE
    )

    val material = reference(
        "material_id",
        Materiales,
        onDelete = ReferenceOption.CASCADE
    )

    val cantidad = integer("cantidad")
    val puntosGanados = integer("puntos_ganados")
    val fecha = varchar("fecha", 30)
    val estado = varchar("estado", 30).default("Pendiente")
}