package com.avrokeyboard.app.ime

enum class KeyboardLanguage {
    ENGLISH,
    BANGLA,
    AVRO,
}

enum class KeyboardPage {
    LETTERS,
    SYMBOLS,
}

enum class KeyKind {
    CHAR,
    SPACE,
    BACKSPACE,
    ENTER,
    SHIFT,
    PAGE_LETTERS,
    PAGE_SYMBOLS,
    LANGUAGE,
}

data class KeySpec(
    val kind: KeyKind,
    val label: String,
    val commit: String? = null,
    val weight: Float = 1f,
    val wide: Boolean = false,
)

sealed class KeyAction {
    data class Commit(val text: String) : KeyAction()
    data object Backspace : KeyAction()
    data object Enter : KeyAction()
}
