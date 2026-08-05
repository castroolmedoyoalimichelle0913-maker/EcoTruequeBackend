package com.example.repository

import com.example.models.Chat
import com.example.models.Mensaje
import com.example.tables.Chats
import com.example.tables.Mensajes
import com.example.tables.Propuestas
import com.example.tables.Materiales
import com.example.tables.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ChatRepository {

    fun crear(propuestaId: Int, usuario1Id: Int, usuario2Id: Int, fecha: String): Int = transaction {
        val existente = Chats
            .selectAll()
            .where {
                (Chats.propuestaId eq propuestaId) and
                    ((Chats.usuario1Id eq usuario1Id) or (Chats.usuario1Id eq usuario2Id))
            }
            .limit(1)
            .firstOrNull()

        if (existente != null) {
            return@transaction existente[Chats.id].value
        }

        Chats.insert {
            it[Chats.propuestaId] = propuestaId
            it[Chats.usuario1Id] = usuario1Id
            it[Chats.usuario2Id] = usuario2Id
            it[fechaCreacion] = fecha
        }[Chats.id].value
    }

    fun obtenerPorPropuesta(propuestaId: Int): Chat? = transaction {
        Chats
            .selectAll()
            .where { Chats.propuestaId eq propuestaId }
            .limit(1)
            .firstOrNull()
            ?.let { rowToChat(it) }
    }

    fun obtenerPorIdUsuario(chatId: Int): Chat? = transaction {
        Chats
            .selectAll()
            .where { Chats.id eq chatId }
            .limit(1)
            .firstOrNull()
            ?.let { rowToChat(it) }
    }

    fun obtenerPorUsuario(usuarioId: Int): List<Chat> = transaction {
        val propuesta = Propuestas.alias("propuesta")
        val materialDeseado = Materiales.alias("materialDeseado")

        Chats
            .join(propuesta, JoinType.LEFT, Chats.propuestaId, propuesta[Propuestas.id])
            .join(materialDeseado, JoinType.LEFT, propuesta[Propuestas.materialDeseadoId], materialDeseado[Materiales.id])
            .selectAll()
            .where {
                (Chats.usuario1Id eq usuarioId) or (Chats.usuario2Id eq usuarioId)
            }
            .orderBy(Chats.id, SortOrder.DESC)
            .map { row ->
                val chatId = row[Chats.id].value
                val usuario1Id = row[Chats.usuario1Id]
                val usuario2Id = row[Chats.usuario2Id]
                val otroId = if (usuario1Id == usuarioId) usuario2Id else usuario1Id

                val otroRow = Usuarios
                    .selectAll()
                    .where { Usuarios.id eq otroId }
                    .limit(1)
                    .firstOrNull()

                val ultimo = obtenerUltimoMensaje(chatId)
                val noLeidos = contarNoLeidos(chatId, usuarioId)

                Chat(
                    id = chatId,
                    propuestaId = row[Chats.propuestaId],
                    usuario1Id = usuario1Id,
                    usuario2Id = usuario2Id,
                    fechaCreacion = row[Chats.fechaCreacion],
                    otroNombre = otroRow?.get(Usuarios.nombre) ?: "Usuario",
                    otroFoto = otroRow?.get(Usuarios.fotoPerfil),
                    ultimoMensaje = ultimo?.contenido,
                    ultimaHora = ultimo?.hora,
                    noLeidos = noLeidos,
                    materialNombre = row.getOrNull(materialDeseado[Materiales.nombre])
                )
            }
    }

    private fun obtenerUltimoMensaje(chatId: Int): Mensaje? = transaction {
        Mensajes
            .selectAll()
            .where { Mensajes.chatId eq chatId }
            .orderBy(Mensajes.id, SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.let { rowToMensaje(it) }
    }

    private fun contarNoLeidos(chatId: Int, usuarioId: Int): Int = transaction {
        Mensajes
            .selectAll()
            .where {
                (Mensajes.chatId eq chatId) and
                    (Mensajes.emisorId neq usuarioId) and
                    (Mensajes.leido eq 0)
            }
            .count().toInt()
    }

    fun contarNoLeidosTotal(usuarioId: Int): Int = transaction {
        val chats = Chats
            .selectAll()
            .where { (Chats.usuario1Id eq usuarioId) or (Chats.usuario2Id eq usuarioId) }
            .map { it[Chats.id].value }

        if (chats.isEmpty()) return@transaction 0

        Mensajes
            .selectAll()
            .where {
                (Mensajes.chatId inList chats) and
                    (Mensajes.emisorId neq usuarioId) and
                    (Mensajes.leido eq 0)
            }
            .count().toInt()
    }

    fun enviarMensaje(chatId: Int, emisorId: Int, contenido: String, hora: String): Int = transaction {
        Mensajes.insert {
            it[Mensajes.chatId] = chatId
            it[Mensajes.emisorId] = emisorId
            it[Mensajes.contenido] = contenido
            it[Mensajes.hora] = hora
            it[Mensajes.leido] = 0
        }[Mensajes.id].value
    }

    fun obtenerMensajes(chatId: Int): List<Mensaje> = transaction {
        val emisor = Usuarios.alias("emisor")
        Mensajes
            .join(emisor, JoinType.LEFT, Mensajes.emisorId, emisor[Usuarios.id])
            .selectAll()
            .where { Mensajes.chatId eq chatId }
            .orderBy(Mensajes.id, SortOrder.ASC)
            .map { row ->
                rowToMensaje(row).copy(emisorNombre = row.getOrNull(emisor[Usuarios.nombre]))
            }
    }

    fun marcarLeidos(chatId: Int, usuarioId: Int): Boolean = transaction {
        Mensajes.update({
            (Mensajes.chatId eq chatId) and
                (Mensajes.emisorId neq usuarioId) and
                (Mensajes.leido eq 0)
        }) {
            it[Mensajes.leido] = 1
        } > 0
    }

    private fun rowToChat(
        row: ResultRow,
        otro: Alias<Usuarios>? = null,
        materialDeseado: Alias<Materiales>? = null
    ): Chat {
        return Chat(
            id = row[Chats.id].value,
            propuestaId = row[Chats.propuestaId],
            usuario1Id = row[Chats.usuario1Id],
            usuario2Id = row[Chats.usuario2Id],
            fechaCreacion = row[Chats.fechaCreacion],
            otroNombre = otro?.let { row.getOrNull(it[Usuarios.nombre]) },
            otroFoto = otro?.let { row.getOrNull(it[Usuarios.fotoPerfil]) },
            materialNombre = materialDeseado?.let { row.getOrNull(it[Materiales.nombre]) }
        )
    }

    private fun rowToMensaje(row: ResultRow): Mensaje {
        return Mensaje(
            id = row[Mensajes.id].value,
            chatId = row[Mensajes.chatId],
            emisorId = row[Mensajes.emisorId],
            contenido = row[Mensajes.contenido],
            hora = row[Mensajes.hora],
            leido = row[Mensajes.leido]
        )
    }
}
