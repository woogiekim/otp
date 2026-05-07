package org.auth.hotp

import org.apache.commons.codec.binary.Base32
import org.auth.DefaultSecretKeyGenerator
import org.auth.HmacOtp
import org.auth.Otp
import org.auth.SecretKeyGenerator
import org.auth.extension.encode

class HOtp(
    val issuer: String,
    val account: String,
    var counter: Long = 0L,
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
) : Otp {

    val secretKey: String = Base32().encodeToString(secretKeyGenerator.generate())

    override fun generateUrl(): String {
        return "otpauth://hotp/${"$issuer:$account".encode()}?secret=${secretKey.encode()}&issuer=${issuer.encode()}&counter=$counter"
    }

    override fun generateOtp(): String {
        val otp = computeHotp(counter)
        counter++
        return otp
    }

    fun computeHotp(counterValue: Long): String = HmacOtp.compute(secretKey, counterValue)
}
