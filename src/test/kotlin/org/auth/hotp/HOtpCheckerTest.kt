package org.auth.hotp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HOtpCheckerTest {

    @Test
    fun `HOTP 검증 - 맞는 OTP인 경우 true`() {
        val hotp = createHOtp()
        val otp = hotp.generateOtp() // counter=0 으로 OTP 생성, hotp.counter → 1

        val checker = HOtpChecker(hotp) // serverCounter=0
        assertThat(checker.check(otp)).isTrue
    }

    @Test
    fun `HOTP 검증 - 다른 OTP인 경우 false`() {
        val checker = HOtpChecker(createHOtp())

        assertThat(checker.check("000000")).isFalse
    }

    @Test
    fun `HOTP 검증 - look-ahead 범위 내 카운터 OTP는 true`() {
        val hotp = createHOtp()
        hotp.generateOtp() // counter=0
        hotp.generateOtp() // counter=1
        val futureOtp = hotp.generateOtp() // counter=2 의 OTP

        // serverCounter=0, look-ahead=5 → 0~4 범위에서 counter=2 탐색
        val checker = HOtpChecker(hotp, lookAheadWindow = 5)
        assertThat(checker.check(futureOtp)).isTrue
    }

    @Test
    fun `HOTP 검증 성공 시 서버 카운터가 동기화됨`() {
        val hotp = createHOtp()
        hotp.generateOtp() // counter=0
        val otp = hotp.generateOtp() // counter=1 의 OTP

        // serverCounter=0 에서 시작, counter=1 위치에서 일치 → serverCounter = 2
        val checker = HOtpChecker(hotp, lookAheadWindow = 5)
        checker.check(otp)

        assertThat(checker.serverCounter).isEqualTo(2L)
    }
}
