package org.weefic.cache.web

import org.weefic.cache.Cache
import java.io.ByteArrayInputStream

class WebCache(val identifier: String, val contentLength: Long) {
    val lock = Object()
    @Volatile
    var data = ByteArray(if (contentLength > 0) contentLength.toInt() else 1024)
        private set
    @Volatile
    var available = 0
        private set
    @Volatile
    var finished = false
        private set


    fun getCache(): Cache {
        return if (this.finished) {
            Cache(this.identifier, ByteArrayInputStream(this.data), this.contentLength)
        } else {
            Cache(this.identifier, WebCacheInputStream(this), this.contentLength)
        }
    }

    fun finish() {
        synchronized(this.lock) {
            this.finished = true
            this.lock.notifyAll()
        }
    }

    fun append(data: ByteArray, length: Int) {
        synchronized(this.lock) {
            val requestSize = this.available + length
            if (requestSize > this.data.size) {
                var newCapacity = this.data.size
                while (newCapacity <= requestSize) {
                    newCapacity *= 2
                }
                val newData = ByteArray(newCapacity)
                this.data.copyInto(newData)
                this.data = newData
            }

            data.copyInto(this.data, this.available, endIndex = length)
            this.available = requestSize
            this.lock.notifyAll()
        }
    }
}