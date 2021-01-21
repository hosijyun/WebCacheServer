package org.weefic.cache

import java.nio.charset.StandardCharsets
import java.security.MessageDigest


val ByteArray.hex: String
    get() {
        val buffer = StringBuilder(this.size + 2)
        this.forEach { b ->
            val hex = (b.toInt() and 0xFF).toString(16)
            if (hex.length == 1) {
                buffer.append('0').append(hex)
            } else {
                buffer.append(hex)
            }
        }
        return buffer.toString()
    }

val String.sha1: String
    get() {
        val md = MessageDigest.getInstance("sha1")
        val result = md.digest(this.toByteArray(StandardCharsets.UTF_8))
        return result.hex
    }