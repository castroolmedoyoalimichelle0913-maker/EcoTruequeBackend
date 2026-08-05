package com.example.repository

import com.example.models.Propuesta
import com.example.tables.Materiales
import com.example.tables.Propuestas
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PropuestaRepository {

    fun insertar(propuesta: Propuesta): Int = transaction {
        Propuestas.insert {
            it[oferenteId] = propuesta.oferenteId
            it[receptorId] = propuesta.receptorId
            it[materialOfertaId] = propuesta.materialOfertaId
            it[materialDeseadoId] = propuesta.materialDeseadoId
            it[mensaje] = propuesta.mensaje
            it[estado] = propuesta.estado
            it[fecha] = propuesta.fecha
        }[Propuestas.id].value
    }

    fun obtenerPorId(id: Int): ResultRow? = transaction {
        Propuestas
            .selectAll()
            .where { Propuestas.id eq id }
            .limit(1)
            .firstOrNull()
    }

    fun obtenerPorUsuario(usuarioId: Int): List<Propuesta> = transaction {
        val oferente = Usuarios.alias("oferente")
        val materialOferta = Materiales.alias("materialOferta")
        val materialDeseado = Materiales.alias("materialDeseado")

        Propuestas
            .join(oferente, JoinType.LEFT, Propuestas.oferenteId, oferente[Usuarios.id])
            .join(materialOferta, JoinType.LEFT, Propuestas.materialOfertaId, materialOferta[Materiales.id])
            .join(materialDeseado, JoinType.LEFT, Propuestas.materialDeseadoId, materialDeseado[Materiales.id])
            .selectAll()
            .where { (Propuestas.oferenteId eq usuarioId) or (Propuestas.receptorId eq usuarioId) }
            .orderBy(Propuestas.id, SortOrder.DESC)
            .map { rowToPropuesta(it, oferente, materialOferta, materialDeseado) }
    }

    fun actualizarEstado(id: Int, estado: String): Boolean = transaction {
        Propuestas.update({ Propuestas.id eq id }) {
            it[Propuestas.estado] = estado
        } > 0
    }

    fun existePropuestaActiva(oferenteId: Int, receptorId: Int, materialDeseadoId: Int): Boolean = transaction {
        Propuestas
            .selectAll()
            .where {
                (Propuestas.oferenteId eq oferenteId) and
                    (Propuestas.receptorId eq receptorId) and
                    (Propuestas.materialDeseadoId eq materialDeseadoId) and
                    (Propuestas.estado inList listOf("Pendiente", "En proceso"))
            }
            .limit(1)
            .count() > 0
    }

    fun contarCompletados(usuarioId: Int): Int = transaction {
        Propuestas
            .selectAll()
            .where {
                (Propuestas.estado eq "Completado") and
                    ((Propuestas.oferenteId eq usuarioId) or (Propuestas.receptorId eq usuarioId))
            }
            .count().toInt()
    }

    private fun rowToPropuesta(
        row: ResultRow,
        oferente: Alias<Usuarios>,
        materialOferta: Alias<Materiales>,
        materialDeseado: Alias<Materiales>
    ): Propuesta {
        return Propuesta(
            id = row[Propuestas.id].value,
            oferenteId = row[Propuestas.oferenteId],
            receptorId = row[Propuestas.receptorId],
            materialOfertaId = row[Propuestas.materialOfertaId],
            materialDeseadoId = row[Propuestas.materialDeseadoId],
            mensaje = row[Propuestas.mensaje],
            estado = row[Propuestas.estado],
            fecha = row[Propuestas.fecha],
            oferenteNombre = row.getOrNull(oferente[Usuarios.nombre]),
            materialOfertaNombre = row.getOrNull(materialOferta[Materiales.nombre]),
            materialDeseadoNombre = row.getOrNull(materialDeseado[Materiales.nombre])
        )
    }
}
