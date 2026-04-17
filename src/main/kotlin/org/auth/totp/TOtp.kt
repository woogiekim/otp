package org.auth.totp

import org.apache.commons.codec.binary.Base32
import org.auth.DefaultSecretKeyGenerator
import org.auth.Otp
import org.auth.SecretKeyGenerator
import org.auth.extension.encode
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TOtp(
    val issuer: String,
    val account: String,
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
) : Otp {

    companion object {
        private val base32 = Base32()
        private const val DIGITS = 6
        private const val ALGORITHM = "HmacSHA1"
        private const val TIME_STEP = 30L
    }

    val secretKey: String = secretKeyGenerator.generate().run {
        base32.encodeToString(this)
    }

    override fun generateUrl(): String {
        return "otpauth://totp/${"$issuer:$account".encode()}?secret=${secretKey.encode()}&issuer=${issuer.encode()}"
    }

    override fun generateOtp(): String {
        val timeCounter = System.currentTimeMillis() / 1000 / TIME_STEP
        return computeTotp(timeCounter)
    }

    fun computeTotp(timeCounter: Long): String {
        val keyBytes = base32.decode(secretKey)
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(keyBytes, ALGORITHM))

        val counterBytes = ByteArray(8)
        var c = timeCounter
        for (i in 7 downTo 0) {
            counterBytes[i] = (c and 0xFF).toByte()
            c = c ushr 8
        }

        val hash = mac.doFinal(counterBytes)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncated = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val otp = truncated % Math.pow(10.0, DIGITS.toDouble()).toInt()
        return otp.toString().padStart(DIGITS, '0')
    }
}
