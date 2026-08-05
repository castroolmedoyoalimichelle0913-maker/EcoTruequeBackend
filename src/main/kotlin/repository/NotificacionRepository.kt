package com.example.repository

import com.example.models.Notificacion
import com.example.tables.Notificaciones
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class NotificacionRepository {

    fun crear(usuarioId: Int, tipo: String, titulo: String, mensaje: String, fecha: String): Int = transaction {
        Notificaciones.insert {
            it[Notificaciones.usuarioId] = usuarioId
            it[Notificaciones.tipo] = tipo
            it[Notificaciones.titulo] = titulo
            it[Notificaciones.mensaje] = mensaje
            it[Notificaciones.leido] = 0
            it[Notificaciones.fecha] = fecha
        }[Notificaciones.id].value
    }

    fun obtenerPorUsuario(usuarioId: Int): List<Notificacion> = transaction {
        Notificaciones
            .selectAll()
            .where { Notificaciones.usuarioId eq usuarioId }
            .orderBy(Notificaciones.id, SortOrder.DESC)
            .map { rowToNotificacion(it) }
    }

    fun contarNoLeidas(usuarioId: Int): Int = transaction {
        Notificaciones
            .selectAll()
            .where { (Notificaciones.usuarioId eq usuarioId) and (Notificaciones.leido eq 0) }
            .count().toInt()
    }

    fun marcarTodasLeidas(usuarioId: Int): Boolean = transaction {
        Notificaciones.update({
            (Notificaciones.usuarioId eq usuarioId) and (Notificaciones.leido eq 0)
        }) {
            it[leido] = 1
        } > 0
    }

    private fun rowToNotificacion(row: ResultRow): Notificacion {
        return Notificacion(
            id = row[Notificaciones.id].value,
            usuarioId = row[Notificaciones.usuarioId],
            tipo = row[Notificaciones.tipo],
            titulo = row[Notificaciones.titulo],
            mensaje = row[Notificaciones.mensaje],
            leido = row[Notificaciones.leido],
            fecha = row[Notificaciones.fecha]
        )
    }
}
