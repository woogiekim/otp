package org.auth.hotp

import org.assertj.core.api.Assertions.assertThat
import org.auth.DefaultSecretKeyGenerator
import org.auth.SecretKeyGenerator
import org.auth.extension.encode
import org.junit.jupiter.api.Test

class HOtpTest {

    @Test
    fun `HOTP 생성`() {
        val hotp = HOtp("issuer", "account")

        assertThat(hotp.issuer).isEqualTo("issuer")
        assertThat(hotp.account).isEqualTo("account")
        assertThat(hotp.secretKey).isNotBlank
    }

    @Test
    fun `HOTP URL 생성`() {
        val hotp = createHOtp()

        val issuer = hotp.issuer
        val account = hotp.account
        val secretKey = hotp.secretKey
        val counter = hotp.counter

        assertThat(hotp.generateUrl()).isEqualTo(
            "otpauth://hotp/${"$issuer:$account".encode()}?secret=${secretKey.encode()}&issuer=${issuer.encode()}&counter=$counter"
        )
    }

    @Test
    fun `HOTP 숫자 생성`() {
        val hotp = createHOtp()

        val generatedOtp = hotp.generateOtp()

        assertThat(generatedOtp)
            .hasSize(6)
            .containsPattern("\\d+")
    }

    @Test
    fun `HOTP 생성 시 카운터 증가`() {
        val hotp = createHOtp()

        assertThat(hotp.counter).isEqualTo(0L)
        hotp.generateOtp()
        assertThat(hotp.counter).isEqualTo(1L)
    }
}

fun createHOtp(
    issuer: String = "issuer",
    account: String = "account",
    counter: Long = 0L,
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
): HOtp {
    return HOtp(issuer, account, counter, secretKeyGenerator)
}
