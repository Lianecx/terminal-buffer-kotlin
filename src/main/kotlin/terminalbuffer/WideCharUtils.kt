package terminalbuffer

object WideCharUtils {
    /**
     * Returns true if the given Unicode code point is a wide (double-width) character
     * in terminal display. Covers CJK ideographs, fullwidth forms, and common emoji ranges.
     */
    fun isWideChar(codePoint: Int): Boolean {
        // CJK Ideographs and related blocks detected by JDK
        if (Character.isIdeographic(codePoint)) return true

        return when (codePoint) {
            // Fullwidth ASCII variants and fullwidth symbols
            in 0xFF01..0xFF60,
            in 0xFFE0..0xFFE6,
            // CJK Compatibility Ideographs
            in 0xF900..0xFAFF,
            // Hangul Jamo
            in 0x1100..0x115F,
            in 0x2329..0x232A,
            // CJK Radicals Supplement through Enclosed CJK
            in 0x2E80..0x303E,
            // Katakana, Bopomofo, Hangul Compatibility Jamo, Kanbun, etc.
            in 0x3041..0x33BF,
            // CJK Unified Ideographs Extension A
            in 0x3400..0x4DBF,
            // CJK Unified Ideographs
            in 0x4E00..0x9FFF,
            // Hangul Syllables
            in 0xAC00..0xD7A3,
            // CJK Compatibility Ideographs Supplement
            in 0x2F800..0x2FA1F,
            // CJK Unified Ideographs Extension B-G
            in 0x20000..0x2FFFF,
            // Emoji Presentation (common ranges)
            in 0x1F300..0x1F9FF,
            in 0x1FA00..0x1FA6F,
            in 0x1FA70..0x1FAFF -> true

            else -> false
        }
    }
}
