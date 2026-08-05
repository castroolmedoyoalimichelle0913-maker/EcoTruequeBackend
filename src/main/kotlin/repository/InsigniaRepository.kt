package com.example.repository

import com.example.models.Insignia
import com.example.tables.Insignias
import com.example.tables.UsuarioInsignias
import com.example.tables.Materiales
import com.example.tables.Propuestas
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class InsigniaRepository {

    fun insertarSiNoExiste(nombre: String, descripcion: String, imagen: String, requerimiento: String, cantidadRequerida: Int) {
        transaction {
            val existe = Insignias
                .selectAll()
                .where { Insignias.requerimiento eq requerimiento }
                .limit(1)
                .count() > 0

            if (!existe) {
                Insignias.insert {
                    it[Insignias.nombre] = nombre
                    it[Insignias.descripcion] = descripcion
                    it[Insignias.imagen] = imagen
                    it[Insignias.requerimiento] = requerimiento
                    it[Insignias.cantidadRequerida] = cantidadRequerida
                }
            }
        }
    }

    fun obtenerTodas(): List<Insignia> = transaction {
        Insignias.selectAll().map { rowToInsignia(it) }
    }

    fun obtenerPorRequerimiento(requerimiento: String): Int? = transaction {
        Insignias
            .selectAll()
            .where { Insignias.requerimiento eq requerimiento }
            .limit(1)
            .firstOrNull()
            ?.get(Insignias.id)?.value
    }

    fun obtenerInsigniasUsuario(usuarioId: Int): Map<Int, String> = transaction {
        UsuarioInsignias
            .selectAll()
            .where { UsuarioInsignias.usuarioId eq usuarioId }
            .associate { it[UsuarioInsignias.insigniaId] to it[UsuarioInsignias.fechaObtenida] }
    }

    fun asignar(usuarioId: Int, insigniaId: Int, fecha: String) {
        transaction {
            val existe = UsuarioInsignias
                .selectAll()
                .where {
                    (UsuarioInsignias.usuarioId eq usuarioId) and
                        (UsuarioInsignias.insigniaId eq insigniaId)
                }
                .limit(1)
                .count() > 0

            if (!existe) {
                UsuarioInsignias.insert {
                    it[UsuarioInsignias.usuarioId] = usuarioId
                    it[UsuarioInsignias.insigniaId] = insigniaId
                    it[fechaObtenida] = fecha
                }
            }
        }
    }

    fun contarMateriales(usuarioId: Int): Int = transaction {
        Materiales
            .selectAll()
            .where { Materiales.usuarioId eq usuarioId }
            .count().toInt()
    }

    fun contarTruequesCompletados(usuarioId: Int): Int = transaction {
        Propuestas
            .selectAll()
            .where {
                (Propuestas.estado eq "Completado") and
                    ((Propuestas.oferenteId eq usuarioId) or (Propuestas.receptorId eq usuarioId))
            }
            .count().toInt()
    }

    fun obtenerPuntos(usuarioId: Int): Int = transaction {
        Usuarios
            .select(Usuarios.puntos)
            .where { Usuarios.id eq usuarioId }
            .limit(1)
            .firstOrNull()
            ?.get(Usuarios.puntos) ?: 0
    }

    fun contarInsigniasUsuario(usuarioId: Int): Int = transaction {
        UsuarioInsignias
            .selectAll()
            .where { UsuarioInsignias.usuarioId eq usuarioId }
            .count().toInt()
    }

    private fun rowToInsignia(row: ResultRow): Insignia {
        return Insignia(
            id = row[Insignias.id].value,
            nombre = row[Insignias.nombre],
            descripcion = row[Insignias.descripcion],
            imagen = row[Insignias.imagen],
            requerimiento = row[Insignias.requerimiento],
            cantidadRequerida = row[Insignias.cantidadRequerida]
        )
    }
}
