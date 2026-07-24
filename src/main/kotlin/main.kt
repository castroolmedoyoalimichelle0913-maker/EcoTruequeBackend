package com.example

import com.example.database.DatabaseFactory
import io.ktor.server.application.*
import com.example.plugins.configureSecurity

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init(this)

    configureSerialization()

    configureHttp()

    configureSecurity()

    configureRouting()
}