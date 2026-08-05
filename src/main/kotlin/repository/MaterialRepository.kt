package com.example.repository

import com.example.models.Material
import com.example.tables.Materiales
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MaterialRepository {

    fun obtenerTodos(): List<Material> = transaction {
        Materiales
            .leftJoin(Usuarios, { Materiales.usuarioId }, { Usuarios.id })
            .selectAll()
            .orderBy(Materiales.id, SortOrder.DESC)
            .map { rowToMaterial(it) }
    }

    fun obtenerPorId(id: Int): Material? = transaction {
        Materiales
            .leftJoin(Usuarios, { Materiales.usuarioId }, { Usuarios.id })
            .selectAll()
            .where { Materiales.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let { rowToMaterial(it) }
    }

    fun obtenerPorUsuario(usuarioId: Int): List<Material> = transaction {
        Materiales
            .leftJoin(Usuarios, { Materiales.usuarioId }, { Usuarios.id })
            .selectAll()
            .where { Materiales.usuarioId eq usuarioId }
            .orderBy(Materiales.id, SortOrder.DESC)
            .map { rowToMaterial(it) }
    }

    fun buscar(
        query: String?,
        categoria: String?,
        puntosMin: Int?,
        puntosMax: Int?,
        desde: String?,
        hasta: String?,
        lat: Double?,
        lng: Double?,
        distanciaKm: Double?
    ): List<Material> {
        val todos = obtenerTodos()

        return todos.filter { m ->
            var ok = true

            if (ok && !query.isNullOrBlank()) {
                val q = query.lowercase()
                val campos = listOfNotNull(
                    m.nombre,
                    m.descripcion,
                    m.categoria,
                    m.etiquetas,
                    m.usuarioNombre
                ).joinToString(" ").lowercase()
                ok = campos.contains(q)
            }

            if (ok && !categoria.isNullOrBlank()) {
                ok = m.categoria.equals(categoria, ignoreCase = true)
            }

            if (ok && puntosMin != null) {
                ok = m.puntos >= puntosMin
            }

            if (ok && puntosMax != null) {
                ok = m.puntos <= puntosMax
            }

            if (ok && !desde.isNullOrBlank()) {
                val fecha = m.fechaPublicacion?.take(10) ?: ""
                ok = fecha >= desde
            }

            if (ok && !hasta.isNullOrBlank()) {
                val fecha = m.fechaPublicacion?.take(10) ?: ""
                ok = fecha <= hasta
            }

            if (ok && lat != null && lng != null && distanciaKm != null && m.latitud != null && m.longitud != null) {
                val mLat = m.latitud
                val mLng = m.longitud
                ok = calcularDistanciaKm(lat, lng, mLat, mLng) <= distanciaKm
            } else if (ok && distanciaKm != null) {
                ok = false
            }

            ok
        }
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
            it[fechaPublicacion] = material.fechaPublicacion ?: ""
            it[etiquetas] = material.etiquetas ?: ""
            it[estado] = material.estado ?: "disponible"
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
            it[etiquetas] = material.etiquetas ?: ""
        } > 0
    }

    fun eliminar(id: Int): Boolean = transaction {
        Materiales.deleteWhere { Materiales.id eq id } > 0
    }

    fun marcarEstado(id: Int, estado: String): Boolean = transaction {
        Materiales.update({ Materiales.id eq id }) {
            it[Materiales.estado] = estado
        } > 0
    }

    fun limpiarImagen(id: Int): Boolean = transaction {
        Materiales.update({ Materiales.id eq id }) {
            it[imagen] = null
        } > 0
    }

    fun calcularDistanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val radio = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radio * c
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
            usuarioId = row[Materiales.usuarioId],
            fechaPublicacion = row.getOrNull(Materiales.fechaPublicacion),
            usuarioNombre = row.getOrNull(Usuarios.nombre),
            usuarioFoto = row.getOrNull(Usuarios.fotoPerfil),
            usuarioPuntos = row.getOrNull(Usuarios.puntos),
            etiquetas = row.getOrNull(Materiales.etiquetas),
            estado = row.getOrNull(Materiales.estado)
        )
    }
}
