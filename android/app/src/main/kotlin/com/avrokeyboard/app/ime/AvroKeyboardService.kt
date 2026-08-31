package com.avrokeyboard.app.ime

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.avrokeyboard.app.ime.avro.AvroComposer

/**
 * System IME. Never logs or stores typed text.
 * Flutter settings can stay closed; this service is self-contained.
 */
class AvroKeyboardService : InputMethodService() {

    private var panel: KeyboardPanel? = null
    private val composer = AvroComposer()
    private lateinit var prefs: ImePrefs
    private var editorInfo: EditorInfo? = null
    private var composingActive = false

    override fun onCreate() {
        super.onCreate()
        prefs = ImePrefs(this)
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
        // New field: drop in-memory compose. Do not write into the old view.
        composer.clear()
        composingActive = false
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info
        panel?.setMode(prefs.loadMode(), notify = false)
        panel?.onHostStarted()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        finishAvro(commit = true)
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        finishAvro(commit = true)
        editorInfo = null
        super.onFinishInput()
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
        }
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
            if (commit) {
                ic.commitText(composer.commitWord(), 1)
            } else {
                ic.finishComposingText()
                composer.clear()
            }
        } else {
            composer.clear()
        }
        composingActive = false
    }
}
