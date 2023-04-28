package com.service.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.response.*

import com.service.schemas.Feed
import com.service.schemas.FeedService

fun Routing.feedRouter(feedService: FeedService) {
    route("/api/feeds") {
        post {
            val feed = call.receive<Feed>()
            feedService.create(feed)
            call.respond(HttpStatusCode.Created, "Feed successfully added")
        }

        get("/{id}") {
            try {
                val feedId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid feed id")
                val feed = feedService.read(feedId)

                call.respond(HttpStatusCode.OK, feed)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            }
        }

        delete("/{id}") {
            try {
                val feedId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid feed id")
                feedService.delete(feedId)
                call.respond(HttpStatusCode.OK, "Feed successfully deleted")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid feed id")
            }
        }
    }
}