package com.example.database

import com.example.repository.InsigniaRepository
import com.example.tables.Bitacora
import com.example.tables.Chats
import com.example.tables.Insignias
import com.example.tables.Materiales
import com.example.tables.Mensajes
import com.example.tables.Notificaciones
import com.example.tables.Propuestas
import com.example.tables.Recompensas
import com.example.tables.Reportes
import com.example.tables.Trueques
import com.example.tables.Usuarios
import com.example.tables.UsuarioInsignias
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application) {

        val host = System.getenv("MYSQLHOST")
            ?: error("Falta la variable MYSQLHOST")

        val port = System.getenv("MYSQLPORT")
            ?: error("Falta la variable MYSQLPORT")

        val database = System.getenv("MYSQLDATABASE")
            ?: error("Falta la variable MYSQLDATABASE")

        val user = System.getenv("MYSQLUSER")
            ?: error("Falta la variable MYSQLUSER")

        val password = System.getenv("MYSQLPASSWORD")
            ?: error("Falta la variable MYSQLPASSWORD")

        val hikari = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"

            jdbcUrl =
                "jdbc:mysql://$host:$port/$database" +
                        "?useSSL=false" +
                        "&allowPublicKeyRetrieval=true" +
                        "&serverTimezone=UTC"

            username = user
            this.password = password

            maximumPoolSize = 5
            minimumIdle = 1
            connectionTimeout = 30_000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }

        val dataSource = HikariDataSource(hikari)

        Database.connect(dataSource)

        transaction {
            try {
                SchemaUtils.createMissingTablesAndColumns(
                    Usuarios,
                    Materiales,
                    Recompensas,
                    Trueques,
                    Propuestas,
                    Chats,
                    Mensajes,
                    Notificaciones,
                    Insignias,
                    UsuarioInsignias,
                    Reportes,
                    Bitacora
                )
                application.log.info("Schema actualizado correctamente")
            } catch (e: Exception) {
                application.log.warn("createMissingTablesAndColumns fallo: ${e.message}")
            }

            try {
                exec("ALTER TABLE materiales MODIFY COLUMN imagen TEXT NULL")
                application.log.info("Columna imagen cambiada a TEXT")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS latitud DOUBLE NULL")
                application.log.info("Columna latitud agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS longitud DOUBLE NULL")
                application.log.info("Columna longitud agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS usuario_id INT NULL")
                application.log.info("Columna usuario_id agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS fecha_publicacion VARCHAR(30) DEFAULT ''")
                application.log.info("Columna fecha_publicacion agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS foto_perfil TEXT NULL")
                application.log.info("Columna foto_perfil agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS rol VARCHAR(20) DEFAULT 'usuario'")
                application.log.info("Columna rol agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE")
                application.log.info("Columna activo agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS notificaciones BOOLEAN DEFAULT TRUE")
                application.log.info("Columna notificaciones agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS etiquetas VARCHAR(255) DEFAULT ''")
                application.log.info("Columna etiquetas agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE materiales ADD COLUMN IF NOT EXISTS estado VARCHAR(30) DEFAULT 'disponible'")
                application.log.info("Columna estado agregada")
            } catch (_: Exception) {}

            try {
                exec("ALTER TABLE chats MODIFY COLUMN propuesta_id INT NULL")
                application.log.info("Columna propuesta_id de chats ahora es nullable")
            } catch (_: Exception) {}
        }

        transaction {
            try {
                val insigniaRepository = InsigniaRepository()

                insigniaRepository.insertarSiNoExiste(
                    nombre = "Primer intercambio",
                    descripcion = "Completaste tu primer trueque",
                    imagen = "🌱",
                    requerimiento = "trueques",
                    cantidadRequerida = 1
                )

                insigniaRepository.insertarSiNoExiste(
                    nombre = "Intercambiador activo",
                    descripcion = "Completaste 5 intercambios",
                    imagen = "🔄",
                    requerimiento = "trueques",
                    cantidadRequerida = 5
                )

                insigniaRepository.insertarSiNoExiste(
                    nombre = "Recién llegado",
                    descripcion = "Publicaste tu primer artículo",
                    imagen = "📦",
                    requerimiento = "materiales",
                    cantidadRequerida = 1
                )

                insigniaRepository.insertarSiNoExiste(
                    nombre = "Gran aportador",
                    descripcion = "Publicaste 10 artículos",
                    imagen = "⭐",
                    requerimiento = "materiales",
                    cantidadRequerida = 10
                )

                insigniaRepository.insertarSiNoExiste(
                    nombre = "100 créditos verdes",
                    descripcion = "Acumulaste 100 puntos verdes",
                    imagen = "💚",
                    requerimiento = "puntos",
                    cantidadRequerida = 100
                )

                application.log.info("Insignias verificadas")
            } catch (e: Exception) {
                application.log.warn("No se pudieron crear insignias: ${e.message}")
            }
        }

        application.log.info("Base de datos conectada correctamente")
    }
}
