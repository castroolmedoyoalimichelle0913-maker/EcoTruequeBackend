package com.example.repository

import com.example.models.Material
import com.example.tables.Materiales
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class MaterialRepository {

    fun obtenerTodos(): List<Material> = transaction {
        Materiales.selectAll().map { rowToMaterial(it) }
    }

    fun obtenerPorId(id: Int): Material? = transaction {
        Materiales
            .selectAll()
            .where { Materiales.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let { rowToMaterial(it) }
    }

    fun obtenerPorUsuario(usuarioId: Int): List<Material> = transaction {
        Materiales
            .selectAll()
            .where { Materiales.usuarioId eq usuarioId }
            .map { rowToMaterial(it) }
    }

    fun insertar(material: Material): Int = transaction {
        Materiales.insert {
            it[nombre] = material.nombre
            it[descripcion] = material.descripcion
            it[categoria] = material.categoria
            it[puntos] = material.puntos
            it[imagen] = material.imagen
            it[latitud] = material.latitud
            it[longitud] = material.longitud
            it[usuarioId] = material.usuarioId
        }[Materiales.id].value
    }

    fun actualizar(id: Int, material: Material): Boolean = transaction {
        Materiales.update({ Materiales.id eq id }) {
            it[nombre] = material.nombre
            it[descripcion] = material.descripcion
            it[categoria] = material.categoria
            it[puntos] = material.puntos
            it[imagen] = material.imagen
            it[latitud] = material.latitud
            it[longitud] = material.longitud
        } > 0
    }

    fun eliminar(id: Int): Boolean = transaction {
        Materiales.deleteWhere { Materiales.id eq id } > 0
    }

    private fun rowToMaterial(row: ResultRow): Material {
        return Material(
            id = row[Materiales.id].value,
            nombre = row[Materiales.nombre],
            descripcion = row[Materiales.descripcion],
            categoria = row[Materiales.categoria],
            puntos = row[Materiales.puntos],
            imagen = row[Materiales.imagen],
            latitud = row[Materiales.latitud],
            longitud = row[Materiales.longitud],
            usuarioId = row[Materiales.usuarioId]
        )
    }
}
