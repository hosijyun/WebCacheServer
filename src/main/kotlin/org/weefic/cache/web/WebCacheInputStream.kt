package org.weefic.cache.web

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.lang.Integer.min

class WebCacheInputStream(val webCache: WebCache) : InputStream() {
    companion object {
        val LOG = LoggerFactory.getLogger("web-cache-stream")
    }

    private var position = 0

    init {
        LOG.debug("[{}] start", this.webCache.identifier)
    }


    override fun read(): Int {
        if (this.webCache.finished) {
            // 无需锁
            val available = this.available()
            if (available > 0) {
                val data = this.webCache.data[this.position].toInt() and 0xFF
                this.position++
                return data
            } else {
                LOG.debug("[{}] Finished", this.webCache.identifier)
                return -1
            }
        } else {
            synchronized(this.webCache.lock) {
                while (true) {
                    if (this.position < this.webCache.available) {
                        val data = this.webCache.data[this.position].toInt() and 0xFF
                        this.position++
                        return data
                    } else if (this.webCache.finished) {
                        LOG.debug("[{}] Finished", this.webCache.identifier)
                        return -1
                    } else {
                        this.webCache.lock.wait()
                    }
                }
            }
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (this.webCache.finished) {
            // 无需锁
            val available = this.available()
            if (available > 0) {
                val lengthToCopy = min(available, len)
                this.webCache.data.copyInto(
                    destination = b,
                    destinationOffset = off,
                    startIndex = this.position,
                    endIndex = this.position + lengthToCopy
                )
                this.position += lengthToCopy
                return lengthToCopy
            } else {
                LOG.debug("[{}] Finished", this.webCache.identifier)
                return -1
            }
        } else {
            synchronized(this.webCache.lock) {
                while (true) {
                    val available = this.available()
                    if (available > 0) {
                        val lengthToCopy = min(available, len)
                        this.webCache.data.copyInto(
                            destination = b,
                            destinationOffset = off,
                            startIndex = this.position,
                            endIndex = this.position + lengthToCopy
                        )
                        this.position += lengthToCopy
                        return lengthToCopy
                    } else if (this.webCache.finished) {
                        LOG.debug("[{}] Finished", this.webCache.identifier)
                        return -1
                    } else {
                        //println("Wait")
                        this.webCache.lock.wait()
                    }
                }
            }
        }
    }

    override fun available(): Int {
        return this.webCache.available - this.position
    }
}