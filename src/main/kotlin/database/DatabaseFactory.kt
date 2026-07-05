package com.example.database

import com.example.tables.Materiales
import com.example.tables.Recompensas
import com.example.tables.Trueques
import com.example.tables.Usuarios
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application) {

        val hikari = HikariConfig().apply {

            driverClassName = "com.mysql.cj.jdbc.Driver"

            jdbcUrl = "jdbc:mysql://thomas.proxy.rlwy.net:50978/railway"

            username = "root"

            password = "gRMjobFycbqNvwiuJgDcdlmznrpWBVaL"

            maximumPoolSize = 5

            isAutoCommit = false

            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }

        Database.connect(HikariDataSource(hikari))

        transaction {

            SchemaUtils.create(
                Usuarios,
                Materiales,
                Recompensas,
                Trueques
            )

        }

        println("✅ Base de datos conectada.")
    }
}