package org.auth

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.assertj.core.api.Assertions.assertThat
import org.auth.hotp.createHOtp
import org.auth.totp.createGoogleOtp
import org.auth.totp.createMicrosoftOtp
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage

class OtpBarcodeGeneratorTest {

    @Test
    fun `TOTP QR 이미지는 OTP URL을 인코딩한다`() {
        val totp = createGoogleOtp()

        val decoded = decodeQr(OtpBarcodeGenerator(totp).generateImage())

        assertThat(decoded).isEqualTo(totp.generateUrl())
    }

    @Test
    fun `HOTP QR 이미지는 OTP URL을 인코딩한다`() {
        val hotp = createHOtp()

        val decoded = decodeQr(OtpBarcodeGenerator(hotp).generateImage())

        assertThat(decoded).isEqualTo(hotp.generateUrl())
    }

    @Test
    fun `Microsoft OTP QR 이미지는 OTP URL을 인코딩한다`() {
        val motp = createMicrosoftOtp()

        val decoded = decodeQr(OtpBarcodeGenerator(motp).generateImage())

        assertThat(decoded).isEqualTo(motp.generateUrl())
    }

    @Test
    fun `PNG 포맷으로 바이트 생성`() {
        val bytes = OtpBarcodeGenerator(createGoogleOtp()).generateBytes(ImageFormat.PNG)

        assertThat(bytes.take(4)).containsExactly(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())
    }

    @Test
    fun `JPEG 포맷으로 바이트 생성`() {
        val bytes = OtpBarcodeGenerator(createGoogleOtp()).generateBytes(ImageFormat.JPEG)

        assertThat(bytes.take(3)).containsExactly(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    }

    private fun decodeQr(image: BufferedImage): String {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        return MultiFormatReader().decode(bitmap).text
    }
}
