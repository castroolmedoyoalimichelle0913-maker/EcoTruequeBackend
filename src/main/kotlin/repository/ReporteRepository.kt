package com.example.repository

import com.example.models.Reporte
import com.example.tables.Reportes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ReporteRepository {

    fun crear(reporte: Reporte): Int = transaction {
        Reportes.insert {
            it[usuarioReportaId] = reporte.usuarioReportaId
            it[tipo] = reporte.tipo
            it[referenciaId] = reporte.referenciaId
            it[motivo] = reporte.motivo
            it[estado] = reporte.estado
            it[fecha] = reporte.fecha
        }[Reportes.id].value
    }

    fun listar(): List<Reporte> = transaction {
        Reportes
            .selectAll()
            .orderBy(Reportes.id, SortOrder.DESC)
            .limit(200)
            .map { rowToReporte(it) }
    }

    fun listarPendientes(): List<Reporte> = transaction {
        Reportes
            .selectAll()
            .where { Reportes.estado eq "pendiente" }
            .orderBy(Reportes.id, SortOrder.DESC)
            .map { rowToReporte(it) }
    }

    fun resolver(id: Int): Boolean = transaction {
        Reportes.update({ Reportes.id eq id }) {
            it[estado] = "resuelto"
        } > 0
    }

    fun eliminar(id: Int): Boolean = transaction {
        Reportes.deleteWhere { Reportes.id eq id } > 0
    }

    private fun rowToReporte(row: ResultRow): Reporte {
        return Reporte(
            id = row[Reportes.id].value,
            usuarioReportaId = row[Reportes.usuarioReportaId],
            tipo = row[Reportes.tipo],
            referenciaId = row[Reportes.referenciaId],
            motivo = row[Reportes.motivo],
            estado = row[Reportes.estado],
            fecha = row[Reportes.fecha]
        )
    }
}
