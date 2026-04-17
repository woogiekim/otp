package org.auth.hotp

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.auth.OtpBarcodeGenerator

class HOtpBarcodeGenerator(
    private val hotp: HOtp
) : OtpBarcodeGenerator {

    override fun generate(format: BarcodeFormat, width: Int, height: Int): BitMatrix {
        return MultiFormatWriter().encode(hotp.generateUrl(), format, width, height)
    }
}
