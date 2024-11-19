package com.zeljko.abstractive.zsv.manager.core.objects

import java.nio.charset.StandardCharsets

data class Blob(
    val content: ByteArray,
    val blobSha: String
) {
    override fun toString(): String {
        return content.toString(StandardCharsets.UTF_8).substringAfter("\u0000")
    }

    fun getContentWithoutHeader(): ByteArray {
        val nullByte = content.indexOf(0)
        return content.slice(nullByte + 1 until content.size).toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Blob
        return this.blobSha == other.blobSha
    }

    override fun hashCode(): Int {
        return blobSha.take(8).toInt(16)

    }

}

