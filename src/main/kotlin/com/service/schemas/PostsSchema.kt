package com.service.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@Serializable
data class Post(
    val feedId: Int,
    val title: String,
    val author: String,
    val url: String,
    @Contextual
    val timestamp: LocalDateTime
)
class PostService(database: Database) {
    object Posts : Table() {
        private val id = integer("id").autoIncrement()
        val feedId = integer("feedId").references(FeedService.Feeds.id)
        val title = varchar("title", length = 64)
        val author = varchar("author", length = 64)
        val url = varchar("url", length = 128)
        val timestamp = datetime("timestamp")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * Creates a new Post object in the database, if it doesn't exist already.
     *
     * @param post The Post object to be created.
     * @return true if a new Post object was created, false otherwise.
     */
    suspend fun create(post: Post) = dbQuery {
        Posts.insertIgnore {
            it[feedId] = post.feedId
            it[title] = post.title
            it[author] = post.author
            it[url] = post.url
            it[timestamp] = post.timestamp
        }.insertedCount > 0
    }

    /**
     * Retrieves a list of Post objects from the database with a given feed ID and a maximum number of rows to retrieve.
     *
     * @param id The feed ID of the Post objects to retrieve.
     * @param limit The maximum number of rows to retrieve.
     * @return A list of Post objects retrieved from the database.
     */
    suspend fun readByFeed(id: Int, limit: Int) = dbQuery {
        Posts.select { Posts.feedId eq id }
            .limit(limit)
            .orderBy(Posts.timestamp to SortOrder.DESC)
            .map {
                Post(
                    feedId = it[Posts.feedId],
                    title = it[Posts.title],
                    author = it[Posts.author],
                    url = it[Posts.url],
                    timestamp = it[Posts.timestamp]
                )
            }
    }

    /**
     * Retrieves a list of all Post objects from the database with a maximum number of rows to retrieve.
     *
     * @param limit The maximum number of rows to retrieve.
     * @return A list of all Post objects retrieved from the database.
     */
    suspend fun readAll(limit: Int) = dbQuery {
        Posts.selectAll()
            .limit(limit)
            .orderBy(Posts.timestamp to SortOrder.DESC)
            .map {
                Post(
                    feedId = it[Posts.feedId],
                    title = it[Posts.title],
                    author = it[Posts.author],
                    url = it[Posts.url],
                    timestamp = it[Posts.timestamp]
                )
            }
    }
}