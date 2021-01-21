package org.weefic.cache

import org.weefic.cache.web.WebCacheProvider
import org.weefic.cache.web.WebException
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.net.URL

class App(port: Int, dir: File? = null) : NanoHTTPD(port) {
    private val webCacheProvider = WebCacheProvider(dir?.let { FileCacheProvider(it) })

    init {
        this.start(SOCKET_READ_TIMEOUT, false)
    }

    override fun serve(session: IHTTPSession): Response? {
        when (session.uri) {
            "/request" -> {
                return serveRequest(session)
            }
            else -> {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Resource not found")
            }
        }
    }

    private fun serveRequest(session: IHTTPSession): Response? {
        val resourceURL = session.parameters["url"]?.firstOrNull()
        if (resourceURL == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Missing parameter : 'url'")
        } else {
            try {
                val cache = this.webCacheProvider.load(URL(resourceURL))
                var stream = cache.inputStream
                if (cache.length >= 0) {
                    stream = ProgressInputStream(stream, cache.length, cache.identifer)
                }
                return newFixedLengthResponse(Response.Status.OK, cache.contentType, stream, cache.length)
            } catch (e: WebException) {
                return newFixedLengthResponse(Response.Status.lookup(e.code), null, null)
            }
        }
    }
}