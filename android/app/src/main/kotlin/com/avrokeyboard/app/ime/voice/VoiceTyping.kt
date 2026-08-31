package com.avrokeyboard.app.ime.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.avrokeyboard.app.ime.KeyboardLanguage
import java.util.Locale

interface VoiceCallbacks {
    fun onVoiceState(state: VoiceState, hint: String?)
    fun onVoicePartial(text: String)
    fun onVoiceFinal(text: String)
    fun onNeedPermission()
}

class VoiceTyping(
    private val context: Context,
    private val callbacks: VoiceCallbacks,
) : RecognitionListener {

    val session = VoiceSession()
    private var recognizer: SpeechRecognizer? = null
    private val main = Handler(Looper.getMainLooper())
    private var destroyed = false

    fun toggle(language: KeyboardLanguage) {
        if (destroyed) return
        if (session.isActive) {
            stop(commitPartial = true)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            session.fail("unavailable")
            callbacks.onVoiceState(VoiceState.ERROR, "unavailable")
            session.recover()
            callbacks.onVoiceState(VoiceState.IDLE, null)
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            session.requestPermission()
            callbacks.onVoiceState(VoiceState.REQUESTING_PERMISSION, null)
            callbacks.onNeedPermission()
            return
        }
        start(language)
    }

    fun startAfterPermission(language: KeyboardLanguage) {
        if (destroyed) return
        start(language)
    }

    fun stop(commitPartial: Boolean) {
        val leftover = if (commitPartial) session.cancel() else {
            session.reset()
            null
        }
        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }
        if (!leftover.isNullOrEmpty()) callbacks.onVoiceFinal(leftover)
        callbacks.onVoiceState(VoiceState.IDLE, null)
    }

    fun destroy() {
        destroyed = true
        session.reset()
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
    }

    private fun start(language: KeyboardLanguage) {
        session.startListening()
        callbacks.onVoiceState(VoiceState.LISTENING, null)
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(this)
            recognizer = it
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            val tag = localeTag(language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, tag)
        }
        try {
            r.startListening(intent)
        } catch (_: Exception) {
            session.fail("unavailable")
            callbacks.onVoiceState(VoiceState.ERROR, "unavailable")
            session.recover()
            callbacks.onVoiceState(VoiceState.IDLE, null)
        }
    }

    private fun localeTag(language: KeyboardLanguage): String = when (language) {
        KeyboardLanguage.ENGLISH -> Locale.US.toLanguageTag()
        KeyboardLanguage.BANGLA, KeyboardLanguage.AVRO -> "bn-BD"
    }

    override fun onReadyForSpeech(params: Bundle?) {
        if (destroyed) return
        session.startListening()
        callbacks.onVoiceState(VoiceState.LISTENING, null)
    }

    override fun onBeginningOfSpeech() {
        callbacks.onVoiceState(VoiceState.RECOGNIZING, null)
    }

    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        callbacks.onVoiceState(VoiceState.RECOGNIZING, null)
    }

    override fun onError(error: Int) {
        if (destroyed) return
        val code = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "no_match"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "timeout"
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "network"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "permission"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "busy"
            SpeechRecognizer.ERROR_CLIENT -> "client"
            SpeechRecognizer.ERROR_SERVER -> "server"
            SpeechRecognizer.ERROR_AUDIO -> "audio"
            else -> "error"
        }
        val leftover = session.partial
        session.fail(code)
        callbacks.onVoiceState(VoiceState.ERROR, code)
        if (leftover.isNotEmpty() && code == "no_match") {
            callbacks.onVoiceFinal(leftover)
        }
        main.post {
            session.recover()
            callbacks.onVoiceState(VoiceState.IDLE, null)
        }
    }

    override fun onResults(results: Bundle?) {
        if (destroyed) return
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        val finalText = session.onFinal(text)
        if (!finalText.isNullOrEmpty()) callbacks.onVoiceFinal(finalText)
        callbacks.onVoiceState(VoiceState.IDLE, null)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (destroyed) return
        val text = partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
        session.onPartial(text)
        if (session.partial.isNotEmpty()) callbacks.onVoicePartial(session.partial)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
