package com.avrokeyboard.app.ime

object Layouts {
    private fun c(label: String, weight: Float = 1f) =
        KeySpec(KeyKind.CHAR, label, commit = label, weight = weight)

    fun english(shift: Boolean): List<List<KeySpec>> {
        fun row(chars: String) = chars.map { ch ->
            val shown = if (shift) ch.uppercaseChar().toString() else ch.toString()
            c(shown)
        }
        return listOf(
            row("qwertyuiop"),
            row("asdfghjkl"),
            listOf(KeySpec(KeyKind.SHIFT, "shift", weight = 1.4f)) +
                row("zxcvbnm") +
                listOf(KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.4f)),
            bottom(KeyboardLanguage.ENGLISH),
        )
    }

    fun banglaConsonants(): List<List<KeySpec>> = listOf(
        listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ").map { c(it) },
        listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন").map { c(it) },
        listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ").map { c(it) },
        listOf(
            KeySpec(KeyKind.SHIFT, "স্বর", weight = 1.3f),
            c("স"), c("হ"), c("ড়"), c("ঢ়"), c("য়"),
            c("ৎ"), c("্"),
            KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.3f),
        ),
        bottom(KeyboardLanguage.BANGLA),
    )

    fun banglaVowels(): List<List<KeySpec>> = listOf(
        listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও").map { c(it) },
        listOf("ঔ", "া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো").map { c(it) },
        listOf(
            c("ৌ"), c("ং"), c("ঃ"), c("ঁ"), c("্"),
            KeySpec(KeyKind.CHAR, "্র", commit = "্র", weight = 1.1f),
            KeySpec(KeyKind.CHAR, "র্", commit = "র্", weight = 1.1f),
            KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.3f),
        ),
        listOf(
            KeySpec(KeyKind.SHIFT, "ক", weight = 1.3f),
            c("০"), c("১"), c("২"), c("৩"), c("৪"),
            c("৫"), c("৬"), c("৭"), c("৮"), c("৯"),
        ),
        bottom(KeyboardLanguage.BANGLA),
    )

    fun symbols(language: KeyboardLanguage): List<List<KeySpec>> {
        val digits = if (language == KeyboardLanguage.BANGLA) {
            "০১২৩৪৫৬৭৮৯"
        } else {
            "1234567890"
        }
        return listOf(
            digits.map { c(it.toString()) },
            listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")").map { c(it) },
            listOf("!", "?", "'", "\"", ":", ";", "/", "_").map { c(it) } +
                listOf(KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.4f)),
            bottom(language),
        )
    }

    private fun bottom(language: KeyboardLanguage): List<KeySpec> {
        val dari = if (language == KeyboardLanguage.ENGLISH) "." else "।"
        return listOf(
            KeySpec(KeyKind.PAGE_SYMBOLS, "123", weight = 1.05f),
            KeySpec(KeyKind.LANGUAGE, "mode", weight = 1.05f),
            KeySpec(KeyKind.MIC, "mic", weight = 1.05f),
            KeySpec(KeyKind.CHAR, ",", commit = ",", weight = 0.75f),
            KeySpec(KeyKind.SPACE, "space", weight = 2.9f, wide = true),
            KeySpec(KeyKind.CHAR, dari, commit = dari, weight = 0.75f),
            KeySpec(KeyKind.PAGE_LETTERS, "ABC", weight = 0.9f),
            KeySpec(KeyKind.ENTER, "enter", weight = 1.25f),
        )
    }
}
