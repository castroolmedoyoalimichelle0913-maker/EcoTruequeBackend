package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Propuestas : IntIdTable("propuestas") {

    val oferenteId = integer("oferente_id")
    val receptorId = integer("receptor_id")
    val materialOfertaId = integer("material_oferta_id")
    val materialDeseadoId = integer("material_deseado_id")
    val mensaje = text("mensaje").nullable()
    val estado = varchar("estado", 30).default("Pendiente")
    val fecha = varchar("fecha", 30)
}
