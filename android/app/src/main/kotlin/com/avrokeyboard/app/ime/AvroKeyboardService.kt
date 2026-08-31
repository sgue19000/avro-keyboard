package com.avrokeyboard.app.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.avrokeyboard.app.ime.avro.AvroComposer

class AvroKeyboardService : InputMethodService() {

    private var panel: KeyboardPanel? = null
    private val composer = AvroComposer()

    override fun onCreateInputView(): View {
        val view = KeyboardPanel(this) { action -> handleAction(action) }
        panel = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        composer.clear()
        panel?.onHostStarted()
    }

    override fun onFinishInput() {
        finishAvro(commit = true)
        super.onFinishInput()
    }

    private fun handleAction(action: KeyAction) {
        val ic: InputConnection = currentInputConnection ?: return
        val mode = panel?.language ?: KeyboardLanguage.ENGLISH
        when (action) {
            is KeyAction.Commit -> {
                if (mode == KeyboardLanguage.AVRO) {
                    handleAvro(ic, action.text)
                } else {
                    finishAvro(commit = true)
                    ic.commitText(action.text, 1)
                }
            }
            KeyAction.Backspace -> {
                if (mode == KeyboardLanguage.AVRO && composer.isComposing) {
                    composer.backspace()
                    ic.setComposingText(composer.preview, 1)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            KeyAction.Enter -> {
                finishAvro(commit = true)
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
    }

    private fun handleAvro(ic: InputConnection, text: String) {
        if (text == " ") {
            val word = composer.commitWord()
            ic.commitText("$word ", 1)
            return
        }
        if (text.length == 1 && !text[0].isLetter() && text[0] != '`') {
            val word = composer.commitWord()
            ic.commitText(word + text, 1)
            return
        }
        composer.type(text)
        ic.setComposingText(composer.preview, 1)
    }

    private fun finishAvro(commit: Boolean) {
        val ic = currentInputConnection
        if (composer.isComposing && ic != null) {
            if (commit) ic.commitText(composer.commitWord(), 1) else {
                ic.finishComposingText()
                composer.clear()
            }
        } else {
            composer.clear()
        }
    }
}
