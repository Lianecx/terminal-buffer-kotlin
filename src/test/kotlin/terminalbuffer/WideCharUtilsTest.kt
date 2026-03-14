package terminalbuffer

import kotlin.test.*

class WideCharUtilsTest {

    @Test
    fun `ASCII characters are not wide`() {
        for (cp in 0x20..0x7E) {
            assertFalse(WideCharUtils.isWideChar(cp), "ASCII ${cp.toChar()} should not be wide")
        }
    }

    @Test
    fun `CJK ideographs are wide`() {
        // 你 (U+4F60), 好 (U+597D), 世 (U+4E16), 界 (U+754C)
        assertTrue(WideCharUtils.isWideChar(0x4F60))
        assertTrue(WideCharUtils.isWideChar(0x597D))
        assertTrue(WideCharUtils.isWideChar(0x4E16))
        assertTrue(WideCharUtils.isWideChar(0x754C))
    }

    @Test
    fun `fullwidth forms are wide`() {
        // Ｈ (U+FF28), ！ (U+FF01)
        assertTrue(WideCharUtils.isWideChar(0xFF28))
        assertTrue(WideCharUtils.isWideChar(0xFF01))
    }

    @Test
    fun `hangul syllables are wide`() {
        // 가 (U+AC00), 힣 (U+D7A3)
        assertTrue(WideCharUtils.isWideChar(0xAC00))
        assertTrue(WideCharUtils.isWideChar(0xD7A3))
    }

    @Test
    fun `emoji are wide`() {
        // 🌀 (U+1F300), 😀 (U+1F600)
        assertTrue(WideCharUtils.isWideChar(0x1F300))
        assertTrue(WideCharUtils.isWideChar(0x1F600))
    }

    @Test
    fun `latin extended characters are not wide`() {
        // é (U+00E9), ñ (U+00F1)
        assertFalse(WideCharUtils.isWideChar(0x00E9))
        assertFalse(WideCharUtils.isWideChar(0x00F1))
    }

    @Test
    fun `hangul jamo are wide`() {
        // U+1100 - U+115F
        assertTrue(WideCharUtils.isWideChar(0x1100))
        assertTrue(WideCharUtils.isWideChar(0x115F))
    }

    @Test
    fun `CJK compatibility ideographs are wide`() {
        assertTrue(WideCharUtils.isWideChar(0xF900))
        assertTrue(WideCharUtils.isWideChar(0xFAFF))
    }

    @Test
    fun `katakana are wide`() {
        // ア (U+30A2)
        assertTrue(WideCharUtils.isWideChar(0x30A2))
    }

    @Test
    fun `space and control chars are not wide`() {
        assertFalse(WideCharUtils.isWideChar(0x20))  // space
        assertFalse(WideCharUtils.isWideChar(0x00))  // null
        assertFalse(WideCharUtils.isWideChar(0x0A))  // newline
    }
}
