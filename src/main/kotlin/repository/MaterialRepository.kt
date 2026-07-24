package com.example.repository

import com.example.models.Material
import com.example.tables.Materiales
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class MaterialRepository {

    fun obtenerTodos(): List<Material> = transaction {
        Materiales.selectAll().map {
            Material(
                id = it[Materiales.id].value,
                nombre = it[Materiales.nombre],
                descripcion = it[Materiales.descripcion],
                categoria = it[Materiales.categoria],
                puntos = it[Materiales.puntos],
                imagen = it[Materiales.imagen],
                latitud = it[Materiales.latitud],
                longitud = it[Materiales.longitud]
            )
        }
    }

    fun obtenerPorId(id: Int): Material? = transaction {
        Materiales
            .selectAll()
            .where { Materiales.id eq id }
            .limit(1)
            .toList()
            .firstOrNull()
            ?.let {
                Material(
                    id = it[Materiales.id].value,
                    nombre = it[Materiales.nombre],
                    descripcion = it[Materiales.descripcion],
                    categoria = it[Materiales.categoria],
                    puntos = it[Materiales.puntos],
                    imagen = it[Materiales.imagen],
                    latitud = it[Materiales.latitud],
                    longitud = it[Materiales.longitud]
                )
            }
    }

    fun insertar(material: Material) = transaction {
        Materiales.insert {
            it[nombre] = material.nombre
            it[descripcion] = material.descripcion
            it[categoria] = material.categoria
            it[puntos] = material.puntos
            it[imagen] = material.imagen
            it[latitud] = material.latitud
            it[longitud] = material.longitud
        }
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
}