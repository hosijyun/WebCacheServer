package org.weefic.cache.web

import org.weefic.cache.Cache
import org.weefic.cache.FileCacheProvider
import org.weefic.cache.sha1
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class WebCacheProvider(val fileCache: FileCacheProvider?) {
    companion object {
        val LOG = LoggerFactory.getLogger("web-provider")
    }

    private val caches = mutableMapOf<String, WebCache>()
    private val executor = Executors.newCachedThreadPool()

    @Throws(WebException::class)
    fun load(url: URL): Cache {
        val identifier = url.path.sha1
        LOG.info("[{}] Find cache : {}", identifier, url)
        // 尝试从文件加载
        this.fileCache?.load(identifier)?.let {
            LOG.info("[{}] Load from file cache", identifier)
            return it
        }
        // 从内存读取
        synchronized(this.caches) {
            this.caches[identifier]
        }?.let {
            LOG.info("[{}] Load from memory cache", identifier)
            return it.getCache()
        }
        // 从网络请求
        return this.webLoad(url, identifier)
    }


    @Throws
    private fun webLoad(url: URL, identifier: String): Cache {
        // 先向网络发送请求
        val httpClient = OkHttpClient.Builder().build()
        val request = Request.Builder().url(url).build()
        try {
            val response = httpClient.newCall(request).execute()
            // 请求返回200
            if (response.code() == 200) {
                val body = response.body()
                if (body == null) {
                    // 没有内容
                    return Cache(identifier, ByteArrayInputStream(ByteArray(0)), 0)
                } else {
                    val contentSize = body.contentLength()
                    val stream = body.byteStream()
                    synchronized(this.caches) {
                        var webCache = this.caches[identifier]
                        if (webCache == null) {
                            webCache = WebCache(identifier, contentSize)
                            this.caches[identifier] = webCache
                            this.asyncLoadContent(identifier, webCache, stream)
                        } else {
                            this.closeQuietly(stream)
                        }
                        return webCache.getCache()
                    }
                }
            } else {
                // 请求返回其他值
                throw WebException(response.code())
            }
        } catch (e: Exception) {
            if (e is WebException) {
                throw  e
            }
            LOG.warn(null, e)
            // 网络异常, 抛出503
            throw WebException(503, e)
        }
    }

    private fun asyncLoadContent(identifier: String, webCache: WebCache, stream: InputStream) {
        this.executor.submit {
            LOG.info("[{}] Start load", identifier)
            val buffer = ByteArray(102400)
            try {
                while (true) {
                    val read = stream.read(buffer)
                    if (read < 0) {
                        break
                    } else {
                        webCache.append(buffer, read)
                    }
                }
                LOG.info("[{}] Cache loaded", identifier)
                // 全部内容已读取, 尝试缓存
                this.fileCache?.let {
                    it.store(identifier, ByteArrayInputStream(webCache.data))
                    // 已经存储到文件, 从内存移除
                    synchronized(this.caches) {
                        this.caches.remove(identifier)
                    }
                }
            } catch (e: Exception) {
                LOG.warn("[{}] Web exception", identifier, e)
                // 网络异常
                synchronized(this.caches) {
                    this.caches.remove(identifier)
                }
            } finally {
                webCache.finish()
            }
            this.closeQuietly(stream)
        }
    }

    private fun closeQuietly(stream: Closeable) {
        try {
            stream.close()
        } catch (e: Exception) {
        }
    }
}