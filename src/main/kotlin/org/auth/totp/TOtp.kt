package org.auth.totp

import org.apache.commons.codec.binary.Base32
import org.auth.DefaultSecretKeyGenerator
import org.auth.HmacOtp
import org.auth.Otp
import org.auth.SecretKeyGenerator
import org.auth.extension.encode

class TOtp(
    val issuer: String,
    val account: String,
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
) : Otp {

    companion object {
        private val base32 = Base32()
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

    fun computeTotp(timeCounter: Long): String = HmacOtp.compute(secretKey, timeCounter)
}
