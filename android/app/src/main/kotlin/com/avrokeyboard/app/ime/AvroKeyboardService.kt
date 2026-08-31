package com.avrokeyboard.app.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * System Input Method Service. Only this class commits text into other apps.
 *
 *     currentInputConnection.commitText(...)
 *     currentInputConnection.deleteSurroundingText(...)
 *     currentInputConnection.sendKeyEvent(...)
 *
 * Step 2 owns layout + raw input. Step 3 will intercept [KeyAction.Commit]
 * when the language is BANGLA and run Avro phonetic conversion before commit.
 */
class AvroKeyboardService : InputMethodService() {

    private var panel: KeyboardPanel? = null

    override fun onCreateInputView(): View {
        val view = KeyboardPanel(this) { action -> handleAction(action) }
        panel = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        panel?.onHostStarted()
    }

    private fun handleAction(action: KeyAction) {
        val ic: InputConnection = currentInputConnection ?: return
        when (action) {
            is KeyAction.Commit -> ic.commitText(action.text, 1)
            KeyAction.Backspace -> {
                // Works with the cursor in the middle of existing text.
                val deleted = ic.deleteSurroundingText(1, 0)
                if (!deleted) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                }
            }
            KeyAction.Enter -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
    }
}
