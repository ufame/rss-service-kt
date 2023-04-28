package com.service.routes

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

import com.service.schemas.WebhooksSubscriptionsService

fun Routing.webhookRouter(webhookService: WebhooksSubscriptionsService) {
    route("/api/subscribe") {
        post {
            try {
                val parameters = call.receiveParameters()
                val webhookUrl = parameters["webhookUrl"] ?: throw IllegalArgumentException("webhookUrl parameter missing")
                val feedId = parameters["feedId"]?.toInt() ?: throw IllegalArgumentException("feedId parameter missing or invalid")

                webhookService.addWebhookSubscription(webhookUrl, feedId)

                call.respond(HttpStatusCode.Created, "Subscription successfully added")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Failed to add subscription: ${e.message}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to add subscription")
            }
        }

        delete("/{webhookId}") {
            try {
                val webhookId = call.parameters["webhookId"]?.toInt() ?: throw IllegalArgumentException("Invalid webhook id")

                webhookService.delete(webhookId)

                call.respond(HttpStatusCode.OK, "Subscription successfully removed")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid webhook id")
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid webhook id")
            }
        }
    }
}