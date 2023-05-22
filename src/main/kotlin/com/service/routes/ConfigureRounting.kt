package com.service.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import com.service.rss.updateAllFeeds
import com.service.schemas.ServiceLocator
import com.service.schemas.FeedService
import com.service.schemas.PostService
import com.service.schemas.WebhooksSubscriptionsService

fun Application.configureRouting() {
    val database = ServiceLocator.database

    val feedService = FeedService(database)
    val postService = PostService(database)
    val webhookSubService = WebhooksSubscriptionsService(database)

    install(Routing) {
        rootRouter()
        feedRouter(feedService)
        postRouter(postService)
        webhookRouter(webhookSubService)
    }

    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            updateAllFeeds(feedService, postService, webhookSubService)
            delay(5 * 60 * 1000)
        }
    }
}