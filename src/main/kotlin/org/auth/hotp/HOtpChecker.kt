package org.auth.hotp

import org.auth.OtpChecker

class HOtpChecker(
    private val hotp: HOtp
) : OtpChecker {

    override fun check(otp: String): Boolean {
        return hotp.computeHotp(hotp.counter) == otp
    }
}
