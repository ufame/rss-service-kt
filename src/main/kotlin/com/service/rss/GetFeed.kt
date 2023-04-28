package com.service.rss

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.HttpClients
import java.io.StringReader

fun getFeed(url: String): SyndFeed {
    HttpClients.createMinimal().use { client ->
        val request: HttpUriRequest = HttpGet(url)
        client.execute(request).use { response ->
            response.entity.content.use { stream ->
                val xmlContent = stream.bufferedReader().use { it.readText() }
                val input = SyndFeedInput()
                return input.build(StringReader(xmlContent))
            }
        }
    }
}