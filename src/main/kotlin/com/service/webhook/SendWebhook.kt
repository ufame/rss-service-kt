package com.service.webhook

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import com.service.schemas.Post
import io.ktor.http.*
import io.ktor.util.*

@OptIn(InternalAPI::class)
suspend fun sendWebhook(webhookUrl: String, post: Post) {
    val client = HttpClient()
    val json = Json.encodeToString(post)
    client.post(webhookUrl) {
        contentType(ContentType.Application.Json)
        body = json
    }
}