package com.service.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Webhook(
    val url: String,
)
class WebhookService(database: Database) {
    object Webhooks : Table() {
        val id = integer("id").autoIncrement()
        val url = varchar("url", length = 128).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Webhooks)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * Retrieves all webhooks subscribed to a feed.
     * @param feedId The ID of the feed to retrieve subscribed webhooks for.
     * @return A list of webhooks subscribed to the feed, or an empty list if none are found.
     */
    suspend fun getWebhooksByFeedId(feedId: Int) = dbQuery {
            (WebhooksSubscriptionsService.WebhooksSubscription innerJoin Webhooks)
                .select { WebhooksSubscriptionsService.WebhooksSubscription.feedId eq feedId }
                .map {
                    Webhook(
                        it[Webhooks.url],
                    )
                }
    }

    /**
     * Deletes a webhook by its url.
     * @param url The URL of the webhook to delete.
     */
    suspend fun delete(url: String) {
        dbQuery {
            Webhooks.deleteWhere { Webhooks.url.eq(url) }
        }
    }
}
