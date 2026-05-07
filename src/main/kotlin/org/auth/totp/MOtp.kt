package org.auth.totp

import org.apache.commons.codec.binary.Base32
import org.auth.DefaultSecretKeyGenerator
import org.auth.HmacOtp
import org.auth.Otp
import org.auth.SecretKeyGenerator
import org.auth.extension.encode

class MOtp(
    val issuer: String,
    val account: String,
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
) : Otp {

    companion object {
        private const val TIME_STEP = 30L
        private const val ALGORITHM = "SHA1"
        private const val DIGITS = 6
        private const val PERIOD = 30
    }

    val secretKey: String = Base32().encodeToString(secretKeyGenerator.generate())

    override fun generateUrl(): String {
        return "otpauth://totp/${"$issuer:$account".encode()}?" +
            "secret=${secretKey.encode()}" +
            "&issuer=${issuer.encode()}" +
            "&algorithm=$ALGORITHM" +
            "&digits=$DIGITS" +
            "&period=$PERIOD"
    }

    override fun generateOtp(): String {
        val timeCounter = System.currentTimeMillis() / 1000 / TIME_STEP
        return computeTotp(timeCounter)
    }

    fun computeTotp(timeCounter: Long): String = HmacOtp.compute(secretKey, timeCounter)
}

fun createMicrosoftOtp(
    issuer: String = "issuer",
    account: String = "account",
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
): MOtp {
    return MOtp(issuer, account, secretKeyGenerator)
}
