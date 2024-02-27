package com.example.mms.Utils

import com.example.mms.database.inApp.AppDatabase
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OCRTest {
    private val mockAppDatabase = mockk<AppDatabase>()
    private val ocr = OCR(mockAppDatabase)

    @Test
    fun doctorInfoRandomOrderCode() {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        val sideString = {(0..10).map { allowedChars.random() }.joinToString("")}
        val codeString = (0..11).map { ('0'..'9').random() }.joinToString("")
        val testString = sideString() + codeString + sideString()
        assertArrayEquals(arrayOf(codeString), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun doctorInfoOneElement11LengthTest() {
        val testString = "iuuhzfe12345678901kljdqssd"
        assertArrayEquals(arrayOf("12345678901"), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun doctorInfoOneElement12LengthTest() {
        val testString = "podflisdfib123456789012ylsdfkhbsddfbkj,bsd"
        assertArrayEquals(arrayOf("123456789012"), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun doctorInfoOneElement10LengthTest() {
        val testString = "lkdhfqshf1234567890oiyhkjdqskbdq"
        assertTrue(this.ocr.getDoctorInfo(testString).isEmpty())
    }

    @Test
    fun doctorInfo2ElementsTest() {
        val testString = "kjqsdqjb12345678901kldjlsjknqf09876543210qsjkbdq;nsd"
        assertArrayEquals(arrayOf("12345678901", "09876543210"), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun doctorInfo2Elements1DuplicateTest() {
        val testString = "liqhdkqj12345678901flkjnsdfknqf12345678901lkjl<sk"
        assertArrayEquals(arrayOf("12345678901"), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun doctorInfo3Elements2DuplicatesTest() {
        val testString = "lhddkjqn12345678901kjsdlknfq098765432109khqfkj,fs12345678901ljnfns;df"
        assertArrayEquals(arrayOf("12345678901", "098765432109"), this.ocr.getDoctorInfo(testString))
    }

    @Test
    fun removeSpaceBetweenNumberTest() {
        val testString = "Hello12 342 2world 1 313 0121 kotlin 12 12 1cool"
        assertEquals("Hello123422world 13130121 kotlin 12121cool", this.ocr.removeSpaceBetweenNumbers(testString))
    }
}