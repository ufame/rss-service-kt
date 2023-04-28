package com.service.rss

import io.ktor.server.util.*
import io.ktor.util.*

import com.service.schemas.FeedService
import com.service.schemas.PostService
import com.service.schemas.Post
import com.service.schemas.WebhooksSubscriptionsService
import com.service.webhook.sendWebhook

@OptIn(InternalAPI::class)
suspend fun updateAllFeeds(
    feedService: FeedService,
    postService: PostService,
    webhooksSubscriptionService: WebhooksSubscriptionsService
) {
    val feeds = feedService.readAll()

    for ((feedId, feed) in feeds) {
        val rssFeed = getFeed(feed.url)
        for (entry in rssFeed.entries) {
            val post = Post(
                feedId = feedId,
                author = entry.author,
                title = entry.title,
                url = entry.uri,
                timestamp = entry.publishedDate.toLocalDateTime()
            )

            if (postService.create(post)) {
                val webhookUrls = webhooksSubscriptionService.getWebhooksByFeedId(feedId)

                for (webhookUrl in webhookUrls) {
                    sendWebhook(webhookUrl, post)
                }
            }
        }
    }
}
