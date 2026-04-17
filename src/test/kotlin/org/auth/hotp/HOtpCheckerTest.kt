package org.auth.hotp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HOtpCheckerTest {

    @Test
    fun `HOTP 검증 - 맞는 OTP인 경우 true`() {
        val hotp = createHOtp()
        val checker = HOtpChecker(hotp)

        val otp = hotp.computeHotp(hotp.counter)

        assertThat(checker.check(otp)).isTrue
    }

    @Test
    fun `HOTP 검증 - 다른 OTP인 경우 false`() {
        val checker = HOtpChecker(createHOtp())

        assertThat(checker.check("000000")).isFalse
    }
}
