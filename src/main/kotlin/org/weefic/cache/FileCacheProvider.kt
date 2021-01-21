package org.weefic.cache

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class FileCacheProvider(val root: File) {
    fun load(identifier: String): Cache? {
        val file = File(this.root, identifier)
        try {
            val stream = FileInputStream(file)
            val size = file.length()
            return Cache(identifier, stream, size)
        } catch (e: Exception) {
            return null
        }
    }

    fun store(identifier: String, data: InputStream) {
        val file = File(this.root, identifier)
        try {
            file.parentFile.mkdirs()
            FileOutputStream(file).use {
                data.copyTo(it)
            }
        } catch (e: Exception) {
        }
    }
}