package org.auth.totp

import org.apache.commons.codec.binary.Base32
import org.assertj.core.api.Assertions.assertThat
import org.auth.DefaultSecretKeyGenerator
import org.auth.SecretKeyGenerator
import org.auth.extension.encode
import org.junit.jupiter.api.Test

class MOtpTest {

    @Test
    fun `Microsoft OTP 생성`() {
        val motp = MOtp("issuer", "account")

        assertThat(motp.issuer).isEqualTo("issuer")
        assertThat(motp.account).isEqualTo("account")
        assertThat(motp.secretKey).isNotBlank
    }

    @Test
    fun `Microsoft OTP URL 생성 - algorithm, digits, period 파라미터 포함`() {
        val motp = createMicrosoftOtp()

        val issuer = motp.issuer
        val account = motp.account
        val secretKey = motp.secretKey

        assertThat(motp.generateUrl()).isEqualTo(
            "otpauth://totp/${"$issuer:$account".encode()}?" +
                "secret=${secretKey.encode()}" +
                "&issuer=${issuer.encode()}" +
                "&algorithm=SHA1" +
                "&digits=6" +
                "&period=30"
        )
    }

    @Test
    fun `Microsoft OTP URL에 algorithm 파라미터가 포함된다`() {
        val motp = createMicrosoftOtp()

        assertThat(motp.generateUrl()).contains("algorithm=SHA1")
    }

    @Test
    fun `Microsoft OTP URL에 digits 파라미터가 포함된다`() {
        val motp = createMicrosoftOtp()

        assertThat(motp.generateUrl()).contains("digits=6")
    }

    @Test
    fun `Microsoft OTP URL에 period 파라미터가 포함된다`() {
        val motp = createMicrosoftOtp()

        assertThat(motp.generateUrl()).contains("period=30")
    }

    @Test
    fun `Microsoft OTP 숫자 생성`() {
        val motp = createMicrosoftOtp()

        val generatedOtp = motp.generateOtp()

        assertThat(generatedOtp)
            .hasSize(6)
            .containsPattern("\\d+")
    }

    @Test
    fun `동일한 시각 카운터에서 Google TOTP와 동일한 OTP 생성`() {
        val secretKeyBytes = "12345678901234567890".toByteArray()
        val fixedGenerator = object : SecretKeyGenerator {
            override fun generate(): ByteArray = secretKeyBytes
        }

        val totp = TOtp("issuer", "account", fixedGenerator)
        val motp = MOtp("issuer", "account", fixedGenerator)

        val timeCounter = System.currentTimeMillis() / 1000 / 30

        assertThat(motp.computeTotp(timeCounter)).isEqualTo(totp.computeTotp(timeCounter))
    }

    @Test
    fun `RFC 6238 테스트 벡터 검증`() {
        // RFC 6238 test secret: "12345678901234567890" (SHA1)
        val secretKeyBytes = "12345678901234567890".toByteArray()
        val secretKey = Base32().encodeToString(secretKeyBytes)
        val fixedGenerator = object : SecretKeyGenerator {
            override fun generate(): ByteArray = secretKeyBytes
        }

        val motp = MOtp("issuer", "account", fixedGenerator)

        assertThat(motp.secretKey).isEqualTo(secretKey)

        // RFC 6238 Appendix B test vectors (SHA1)
        assertThat(motp.computeTotp(59L / 30)).isEqualTo("287082")
        assertThat(motp.computeTotp(1111111109L / 30)).isEqualTo("081804")
        assertThat(motp.computeTotp(1111111111L / 30)).isEqualTo("050471")
        assertThat(motp.computeTotp(1234567890L / 30)).isEqualTo("005924")
        assertThat(motp.computeTotp(2000000000L / 30)).isEqualTo("279037")
    }
}

fun createMicrosoftOtp(
    issuer: String = "issuer",
    account: String = "account",
    secretKeyGenerator: SecretKeyGenerator = DefaultSecretKeyGenerator()
): MOtp {
    return MOtp(issuer, account, secretKeyGenerator)
}
