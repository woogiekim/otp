package org.auth.hotp

import org.auth.OtpChecker

class HOtpChecker(
    private val hotp: HOtp,
    var serverCounter: Long = 0L,
    private val lookAheadWindow: Int = 5
) : OtpChecker {

    override fun check(otp: String): Boolean {
        for (i in 0 until lookAheadWindow) {
            if (hotp.computeHotp(serverCounter + i) == otp) {
                serverCounter += i + 1
                return true
            }
        }
        return false
    }
}
