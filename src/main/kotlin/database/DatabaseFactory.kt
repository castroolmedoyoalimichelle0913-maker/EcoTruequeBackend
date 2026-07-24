package com.example.database

import com.example.tables.Materiales
import com.example.tables.Recompensas
import com.example.tables.Trueques
import com.example.tables.Usuarios
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
            SchemaUtils.create(
                Usuarios,
                Materiales,
                Recompensas,
                Trueques
            )
        }

        application.log.info("Base de datos conectada correctamente")
    }
}