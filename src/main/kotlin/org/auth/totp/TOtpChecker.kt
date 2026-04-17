package org.auth.totp

import org.auth.OtpChecker

class TOtpChecker(
    private val totp: TOtp
) : OtpChecker {

    override fun check(otp: String): Boolean {
        val currentStep = System.currentTimeMillis() / 1000 / 30
        return (-1..1).any { delta -> totp.computeTotp(currentStep + delta) == otp }
    }
}
