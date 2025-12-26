package com.gabriel.controlfinanciero.data.security

import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest

fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

fun verifyPassword(stored: String, username: String, password: String): Boolean {
    return when {
        isBcryptHash(stored) -> BCrypt.checkpw(password, stored)
        isSha256Hash(stored) -> stored == hashLegacyPassword(username, password)
        else -> stored == password
    }
}

fun isBcryptHash(value: String): Boolean {
    return value.startsWith("\$2a\$") || value.startsWith("\$2b\$") || value.startsWith("\$2y\$")
}

private fun isSha256Hash(value: String): Boolean {
    return value.length == 64 && value.all { it in '0'..'9' || it in 'a'..'f' }
}

private fun hashLegacyPassword(username: String, password: String): String {
    val normalized = "$username:$password"
    val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
    return digest.joinToString("") { byte -> "%02x".format(byte) }
}
