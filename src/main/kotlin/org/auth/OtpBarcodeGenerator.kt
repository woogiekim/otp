package org.auth

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

enum class ImageFormat(val value: String) {
    PNG("PNG"),
    JPEG("JPEG"),
    BMP("BMP")
}

class OtpBarcodeGenerator(private val otp: Otp) {

    fun generateImage(width: Int = 300, height: Int = 300): BufferedImage =
        MatrixToImageWriter.toBufferedImage(toBitMatrix(width, height))

    fun generateBytes(imageFormat: ImageFormat = ImageFormat.PNG, width: Int = 300, height: Int = 300): ByteArray {
        val out = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(toBitMatrix(width, height), imageFormat.value, out)
        return out.toByteArray()
    }

    private fun toBitMatrix(width: Int, height: Int) =
        MultiFormatWriter().encode(otp.generateUrl(), BarcodeFormat.QR_CODE, width, height)
}
