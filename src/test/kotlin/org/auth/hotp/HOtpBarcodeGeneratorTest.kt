package org.auth.hotp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HOtpBarcodeGeneratorTest {

    @Test
    fun `바코드 생성`() {
        val barcodeGenerator = HOtpBarcodeGenerator(createHOtp())

        val bitMatrix = barcodeGenerator.generate()

        assertThat(bitMatrix.width).isEqualTo(300)
        assertThat(bitMatrix.height).isEqualTo(300)
    }
}
