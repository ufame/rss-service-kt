package com.service.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class WebhookSubscription(
    val webhookId: Int,
    val feedId: Int
)
class WebhooksSubscriptionsService(database: Database) {
    object WebhooksSubscription : Table() {
        val webhookId = integer("webhookId").references(WebhookService.Webhooks.id)
        val feedId = integer("feedId").references(FeedService.Feeds.id)

        override val primaryKey = PrimaryKey(webhookId, feedId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(WebhooksSubscription)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun addWebhookSubscription(webhookUrl: String, feedId: Int) {
        transaction {
            val existingWebhook = WebhookService.Webhooks
                .select { WebhookService.Webhooks.url eq webhookUrl }
                .singleOrNull()

            val webhookId = if (existingWebhook == null) {
                val newWebhook = WebhookService.Webhooks.insert {
                    it[url] = webhookUrl
                }

                newWebhook[WebhookService.Webhooks.id]
            } else {
                existingWebhook[WebhookService.Webhooks.id]
            }

            WebhooksSubscription.insert {
                it[this.webhookId] = webhookId
                it[this.feedId] = feedId
            }
        }
    }

    /**
     * Retrieves all webhooks subscribed to a feed by its ID.
     *
     * @param feedId The ID of the feed to retrieve subscribed webhooks for.
     * @return A list of URLs subscribed to the feed, or an empty list if none are found.
     */
    suspend fun getWebhooksByFeedId(feedId: Int) = dbQuery {
        WebhooksSubscription
            .innerJoin(WebhookService.Webhooks)
            .select { WebhooksSubscription.feedId eq feedId }
            .map { it[WebhookService.Webhooks.url] }
    }

    /**
     * Deletes a webhook subscription from the database by feed ID.
     * @param id the webhook ID of the webhook subscription to delete.
     */
    suspend fun delete(id: Int) {
        dbQuery {
            WebhooksSubscription.deleteWhere { webhookId.eq(id) }
        }
    }
}
