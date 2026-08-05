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
                exec("ALTER TABLE chats MODIFY COLUMN propuesta_id INT NULL")
                application.log.info("Columna propuesta_id de chats ahora es nullable")
            } catch (_: Exception) {}

            fun asegurarColumna(tabla: String, columna: String, definicion: String) {
                try {
                    val existe = exec(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = '$tabla' " +
                            "AND COLUMN_NAME = '$columna'"
                    ) { rs -> rs.next() && rs.getInt(1) > 0 }

                    if (existe != true) {
                        exec("ALTER TABLE $tabla ADD COLUMN $definicion")
                        application.log.info("Columna $tabla.$columna agregada")
                    }
                } catch (e: Exception) {
                    application.log.warn("No se pudo asegurar columna $tabla.$columna: ${e.message}")
                }
            }

            asegurarColumna("materiales", "latitud", "DOUBLE NULL")
            asegurarColumna("materiales", "longitud", "DOUBLE NULL")
            asegurarColumna("materiales", "usuario_id", "INT NULL")
            asegurarColumna("materiales", "fecha_publicacion", "VARCHAR(30) DEFAULT ''")
            asegurarColumna("materiales", "etiquetas", "VARCHAR(255) DEFAULT ''")
            asegurarColumna("materiales", "estado", "VARCHAR(30) DEFAULT 'disponible'")
            asegurarColumna("usuarios", "foto_perfil", "TEXT NULL")
            asegurarColumna("usuarios", "rol", "VARCHAR(20) DEFAULT 'usuario'")
            asegurarColumna("usuarios", "activo", "BOOLEAN DEFAULT TRUE")
            asegurarColumna("usuarios", "notificaciones", "BOOLEAN DEFAULT TRUE")
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

        try {
            val adminCorreo = System.getenv("ADMIN_CORREO")
            if (!adminCorreo.isNullOrBlank()) {
                transaction {
                    exec("UPDATE usuarios SET rol = 'admin' WHERE correo = '$adminCorreo'")
                }
                application.log.info("Rol admin asegurado para $adminCorreo")
            }
        } catch (e: Exception) {
            application.log.warn("No se pudo asegurar rol admin: ${e.message}")
        }

        application.log.info("Base de datos conectada correctamente")
    }
}
