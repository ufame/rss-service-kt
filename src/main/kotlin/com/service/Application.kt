package com.service

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import com.service.routes.configureRouting
import com.service.schemas.configureDatabases

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabases()
    configureRouting()
}
