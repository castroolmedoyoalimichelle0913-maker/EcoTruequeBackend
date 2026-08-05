package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Reportes : IntIdTable("reportes") {

    val usuarioReportaId = integer("usuario_reporta_id").nullable()
    val tipo = varchar("tipo", 30)
    val referenciaId = integer("referencia_id").nullable()
    val motivo = text("motivo")
    val estado = varchar("estado", 20).default("pendiente")
    val fecha = varchar("fecha", 30)
}
