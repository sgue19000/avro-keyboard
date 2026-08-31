package com.avrokeyboard.app.ime.voice

enum class VoiceState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    RECOGNIZING,
    FINALIZING,
    ERROR,
}

/** In-memory voice compose. Partial text replaces, never concatenates. */
class VoiceSession {
    var state: VoiceState = VoiceState.IDLE
        private set
    var partial: String = ""
        private set
    var lastError: String? = null
        private set

    val isActive: Boolean
        get() = state == VoiceState.LISTENING ||
            state == VoiceState.RECOGNIZING ||
            state == VoiceState.REQUESTING_PERMISSION

    fun requestPermission() {
        state = VoiceState.REQUESTING_PERMISSION
        lastError = null
    }

    fun startListening() {
        state = VoiceState.LISTENING
        partial = ""
        lastError = null
    }

    fun onPartial(text: String) {
        if (state != VoiceState.LISTENING && state != VoiceState.RECOGNIZING) return
        if (text.isBlank()) return
        partial = text.trim()
        state = VoiceState.RECOGNIZING
    }

    fun onFinal(text: String): String? {
        state = VoiceState.FINALIZING
        val out = text.trim().ifEmpty { partial }.trim()
        partial = ""
        state = VoiceState.IDLE
        return out.ifEmpty { null }
    }

    fun cancel(): String? {
        val leftover = partial.trim().ifEmpty { null }
        partial = ""
        state = VoiceState.IDLE
        lastError = null
        return leftover
    }

    fun fail(code: String) {
        partial = ""
        lastError = code
        state = VoiceState.ERROR
    }

    fun recover() {
        if (state == VoiceState.ERROR) {
            state = VoiceState.IDLE
            lastError = null
        }
    }

    fun reset() {
        partial = ""
        lastError = null
        state = VoiceState.IDLE
    }
}
