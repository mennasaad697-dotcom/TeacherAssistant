package com.teacherassistant.util

import java.security.MessageDigest

object SecurityUtils {
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /** توليد رمز QR فريد للطالب */
    fun generateQrCode(studentId: Long): String = "TA-${studentId}-${randomSuffix()}"

    private fun randomSuffix(): String = (1000..9999).random().toString()
}
