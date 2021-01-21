package org.weefic.cache

import java.io.InputStream

data class Cache(
    val identifer: String,
    val inputStream: InputStream,
    val length: Long = -1,
    val contentType: String? = "application/octet-stream"
)