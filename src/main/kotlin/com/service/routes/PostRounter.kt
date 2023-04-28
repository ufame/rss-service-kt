package com.service.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*

import com.service.schemas.PostService

fun Routing.postRouter(postService: PostService) {
    route("/api/posts") {
        get {
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
            val posts = postService.readAll(limit)
            call.respond(posts)
        }

        get("/{feedId}") {
            try {
                val feedId = call.parameters["feedId"]?.toInt() ?: throw IllegalArgumentException("Invalid feed id")
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
                val posts = postService.readByFeed(feedId, limit)

                call.respond(HttpStatusCode.OK, posts)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            }
        }
    }
}
