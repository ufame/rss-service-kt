package com.service.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.rootRouter() {
    route("/") {
        get {
            call.respond("Hello world!")
        }
    }
}