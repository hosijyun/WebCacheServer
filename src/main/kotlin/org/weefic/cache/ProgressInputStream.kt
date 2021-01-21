package org.weefic.cache

import org.slf4j.LoggerFactory
import java.io.FilterInputStream
import java.io.InputStream
import kotlin.math.min

class ProgressInputStream(source: InputStream, val size: Long, val identifier: String) : FilterInputStream(source) {
    companion object {
        val LOG = LoggerFactory.getLogger("progress")
    }

    private var read = 0
    private var nextProgress = 0

    override fun read(): Int {
        val data = super.read()
        if (data >= 0) {
            this.read++
            this.logProgress()
        }
        return data
    }

    override fun read(b: ByteArray): Int {
        val result = super.read(b)
        if (result > 0) {
            this.read += result
            this.logProgress()
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = super.read(b, off, len)
        if (result > 0) {
            this.read += result
            this.logProgress()
        }
        return result
    }

    private fun logProgress() {
        val progress = (this.read.toLong() * 100 / this.size).toInt()
        if (progress >= this.nextProgress) {
            LOG.info("[{}] {}%", this.identifier, progress)
            this.nextProgress = min(progress + 10, 100)
        }
    }
}