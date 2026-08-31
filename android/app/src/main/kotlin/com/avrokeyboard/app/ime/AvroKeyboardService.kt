package com.avrokeyboard.app.ime

import android.content.Intent
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.avrokeyboard.app.ime.avro.AvroComposer
import com.avrokeyboard.app.ime.voice.VoiceCallbacks
import com.avrokeyboard.app.ime.voice.VoicePermissionActivity
import com.avrokeyboard.app.ime.voice.VoiceState
import com.avrokeyboard.app.ime.voice.VoiceTyping

class AvroKeyboardService : InputMethodService(), VoiceCallbacks {

    private var panel: KeyboardPanel? = null
    private val composer = AvroComposer()
    private lateinit var prefs: ImePrefs
    private var editorInfo: EditorInfo? = null
    private var composingActive = false
    private var voiceComposing = false
    private var voice: VoiceTyping? = null

    override fun onCreate() {
        super.onCreate()
        prefs = ImePrefs(this)
        voice = VoiceTyping(applicationContext, this)
        VoicePermissionActivity.onResult = { granted ->
            if (granted) {
                voice?.startAfterPermission(panel?.language ?: KeyboardLanguage.ENGLISH)
            } else {
                panel?.setVoiceState(VoiceState.ERROR, "permission")
                panel?.setVoiceState(VoiceState.IDLE, null)
            }
        }
    }

    override fun onCreateInputView(): View {
        val view = KeyboardPanel(this) { action -> handleAction(action) }
        view.setMode(prefs.loadMode(), notify = false)
        panel = view
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        editorInfo = attribute
        composer.clear()
        composingActive = false
        voiceComposing = false
        voice?.stop(commitPartial = false)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info
        panel?.setMode(prefs.loadMode(), notify = false)
        panel?.onHostStarted()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        voice?.stop(commitPartial = true)
        finishAvro(commit = true)
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        voice?.stop(commitPartial = false)
        finishAvro(commit = true)
        editorInfo = null
        super.onFinishInput()
    }

    override fun onDestroy() {
        VoicePermissionActivity.onResult = null
        voice?.destroy()
        voice = null
        super.onDestroy()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd,
        )
        if (composingActive && ImeEdit.composingInvalid(newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
            finishAvro(commit = true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        panel?.onHostStarted()
    }

    private fun handleAction(action: KeyAction) {
        if (action !is KeyAction.Mic && voice?.session?.isActive == true) {
            voice?.stop(commitPartial = true)
        }
        val ic: InputConnection = currentInputConnection ?: return
        val mode = panel?.language ?: KeyboardLanguage.ENGLISH
        when (action) {
            is KeyAction.Commit -> {
                if (mode == KeyboardLanguage.AVRO && !ImeEdit.isPassword(editorInfo)) {
                    handleAvro(ic, action.text)
                } else {
                    finishAvro(commit = true)
                    ic.commitText(action.text, 1)
                }
            }
            KeyAction.Backspace -> {
                if (mode == KeyboardLanguage.AVRO && composer.isComposing) {
                    composer.backspace()
                    if (composer.isComposing) {
                        ic.setComposingText(composer.preview, 1)
                        composingActive = true
                    } else {
                        ic.commitText("", 1)
                        composingActive = false
                    }
                } else {
                    ImeEdit.deleteBefore(ic)
                }
            }
            KeyAction.Enter -> {
                finishAvro(commit = true)
                ImeEdit.enter(ic, editorInfo)
            }
            is KeyAction.ModeChanged -> {
                finishAvro(commit = true)
                prefs.saveMode(action.to)
            }
            KeyAction.Mic -> {
                finishAvro(commit = true)
                if (ImeEdit.isPassword(editorInfo)) {
                    panel?.setVoiceState(VoiceState.ERROR, "password")
                    panel?.setVoiceState(VoiceState.IDLE, null)
                    return
                }
                voice?.toggle(mode)
            }
        }
    }

    override fun onVoiceState(state: VoiceState, hint: String?) {
        panel?.setVoiceState(state, hint)
    }

    override fun onVoicePartial(text: String) {
        val ic = currentInputConnection ?: return
        ic.setComposingText(text, 1)
        voiceComposing = true
    }

    override fun onVoiceFinal(text: String) {
        val ic = currentInputConnection ?: return
        ic.commitText("$text ", 1)
        voiceComposing = false
    }

    override fun onNeedPermission() {
        val intent = Intent(this, VoicePermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun handleAvro(ic: InputConnection, text: String) {
        if (text == " ") {
            val word = composer.commitWord()
            ic.commitText("$word ", 1)
            composingActive = false
            return
        }
        if (text.length == 1 && !text[0].isLetter() && text[0] != '`') {
            val word = composer.commitWord()
            ic.commitText(word + text, 1)
            composingActive = false
            return
        }
        composer.type(text)
        ic.setComposingText(composer.preview, 1)
        composingActive = composer.isComposing
    }

    private fun finishAvro(commit: Boolean) {
        val ic = currentInputConnection
        if (composer.isComposing && ic != null) {
            if (commit) ic.commitText(composer.commitWord(), 1)
            else {
                ic.finishComposingText()
                composer.clear()
            }
        } else {
            composer.clear()
        }
        composingActive = false
        if (voiceComposing && ic != null && commit) {
            ic.finishComposingText()
            voiceComposing = false
        }
    }
}
