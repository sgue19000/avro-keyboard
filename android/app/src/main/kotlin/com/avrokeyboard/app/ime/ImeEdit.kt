package com.avrokeyboard.app.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

object ImeEdit {
    fun isPassword(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val klass = info.inputType and InputType.TYPE_MASK_CLASS
        if (klass == InputType.TYPE_CLASS_TEXT) {
            return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        if (klass == InputType.TYPE_CLASS_NUMBER) {
            return variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        return false
    }

    fun isMultiline(info: EditorInfo?): Boolean {
        if (info == null) return false
        return info.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0
    }

    fun enter(ic: InputConnection, info: EditorInfo?) {
        val options = info?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED
        val action = options and EditorInfo.IME_MASK_ACTION
        val noEnterAction = options and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0
        if (isMultiline(info) || noEnterAction) {
            ic.commitText("\n", 1)
            return
        }
        when (action) {
            EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_UNSPECIFIED ->
                ic.commitText("\n", 1)
            else -> ic.performEditorAction(action)
        }
    }

    fun deleteBefore(ic: InputConnection) {
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }
        val before = ic.getTextBeforeCursor(12, 0)?.toString().orEmpty()
        if (before.isEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }
        ic.deleteSurroundingText(clusterLen(before), 0)
    }

    fun clusterLen(text: String): Int {
        if (text.isEmpty()) return 0
        var i = text.length
        while (i > 0 && isMark(text[i - 1])) i--
        if (i == 0) return text.length
        i--
        while (i >= 2 && text[i - 1] == HASANTA && isBnCons(text[i - 2])) {
            i -= 2
        }
        return text.length - i
    }

    fun composingInvalid(newSelStart: Int, newSelEnd: Int, candStart: Int, candEnd: Int): Boolean {
        if (candStart < 0 || candEnd < 0) return true
        if (newSelStart != newSelEnd) return true
        return newSelStart < candStart || newSelStart > candEnd
    }

    private const val HASANTA = '\u09CD'

    private fun isMark(ch: Char): Boolean {
        val c = ch.code
        return c in 0x0981..0x0983 ||
            c == 0x09BC ||
            c == 0x09BE ||
            c in 0x09BF..0x09C4 ||
            c in 0x09C7..0x09C8 ||
            c in 0x09CB..0x09CD ||
            c == 0x09D7
    }

    private fun isBnCons(ch: Char): Boolean {
        val c = ch.code
        return c in 0x0995..0x09B9 || c in 0x09DC..0x09DF || c == 0x09CE
    }
}
