package com.avrokeyboard.app.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * System Input Method Service.
 *
 * This is the only class that may send text to other apps:
 *
 *     currentInputConnection.commitText(...)
 *     currentInputConnection.sendKeyEvent(...)
 *
 * Flutter integration plan (later steps — do not implement here):
 * 1. Keep this service as the IME entry point. Android will not treat a
 *    Flutter Activity as a keyboard.
 * 2. Optionally attach a FlutterView / FlutterEngine to [onCreateInputView]
 *    for rich UI. The Dart side must call back into Kotlin to commit text.
 * 3. SharedPreferences or a MethodChannel on a long-lived FlutterEngine
 *    can carry layout / Avro engine settings from the settings app.
 * 4. InputConnection is only valid while this IME is active. Guard null.
 */
class AvroKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        return MinimalKeyboardView(this) { action ->
            handleAction(action)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }

    private fun handleAction(action: KeyAction) {
        val ic: InputConnection = currentInputConnection ?: return
        when (action) {
            is KeyAction.Commit -> ic.commitText(action.text, 1)
            KeyAction.Backspace -> ic.deleteSurroundingText(1, 0)
            KeyAction.Enter -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
        }
    }
}

sealed class KeyAction {
    data class Commit(val text: String) : KeyAction()
    data object Backspace : KeyAction()
    data object Enter : KeyAction()
}
