package com.example.repository

import com.example.models.BitacoraRegistro
import com.example.tables.Bitacora
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BitacoraRepository {

    fun registrar(usuarioId: Int?, correo: String, tipoUsuario: String, accion: String, ip: String?) {
        try {
            transaction {
                Bitacora.insert {
                    it[Bitacora.usuarioId] = usuarioId
                    it[Bitacora.correo] = correo
                    it[Bitacora.tipoUsuario] = tipoUsuario
                    it[Bitacora.accion] = accion
                    it[Bitacora.ip] = ip
                    it[Bitacora.fecha] = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                }
            }
        } catch (_: Exception) {
        }
    }

    fun listar(): List<BitacoraRegistro> = transaction {
        Bitacora
            .selectAll()
            .orderBy(Bitacora.id, SortOrder.DESC)
            .limit(200)
            .map {
                BitacoraRegistro(
                    id = it[Bitacora.id].value,
                    usuarioId = it[Bitacora.usuarioId],
                    correo = it[Bitacora.correo],
                    tipoUsuario = it[Bitacora.tipoUsuario],
                    accion = it[Bitacora.accion],
                    ip = it[Bitacora.ip],
                    fecha = it[Bitacora.fecha]
                )
            }
    }
}
