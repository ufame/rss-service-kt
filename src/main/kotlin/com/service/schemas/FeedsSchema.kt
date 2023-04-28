package com.service.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Feed(
    val name: String,
    val url: String,
)
class FeedService(database: Database) {
    object Feeds : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 64)
        val url = varchar("url", length = 128)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Feeds)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * Inserts a new feed into the database.
     * @param feed the feed object to insert into the database
     * @return the ID of the newly inserted feed
     */
    suspend fun create(feed: Feed) = dbQuery {
        Feeds.insertIgnore {
            it[name] = feed.name
            it[url] = feed.url
        }[Feeds.id]
    }

    /**
     * Retrieves a feed with the given ID from the database.
     * @param id the ID of the feed to retrieve
     * @return the feed object with the given ID, or null if no such feed exists
     */
    suspend fun read(id: Int) = dbQuery {
        Feeds.select { Feeds.id eq id }
            .map {
                Feed(
                    it[Feeds.name],
                    it[Feeds.url],
                )
            }
    }

    suspend fun readAll(): List<Pair<Int, Feed>> = dbQuery {
        Feeds.selectAll()
            .map {
                Pair(it[Feeds.id], Feed(
                    it[Feeds.name],
                    it[Feeds.url],
                ))
            }
    }

    /**
     * Updates a feed with the given ID in the database.
     * @param id the ID of the feed to update
     * @param feed the new feed object to update in the database
     */
    suspend fun update(id: Int, feed: Feed) {
        dbQuery {
            Feeds.update({ Feeds.id eq id }) {
                it[name] = feed.name
                it[url] = feed.url
            }
        }
    }

    /**
     * Deletes a feed with the given ID from the database.
     * @param id the ID of the feed to delete
     */
    suspend fun delete(id: Int) {
        dbQuery {
            Feeds.deleteWhere { Feeds.id.eq(id) }
        }
    }
}