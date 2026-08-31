package com.avrokeyboard.app.ime

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

/**
 * Temporary QWERTY grid so the IME can be enabled and type into any app.
 * Full English / Bengali / Avro layouts replace this in later steps.
 */
class MinimalKeyboardView(
    context: Context,
    private val onAction: (KeyAction) -> Unit,
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setBackgroundColor(0xFFECEFF1.toInt())
        val pad = dp(6)
        setPadding(pad, pad, pad, pad)

        addRow(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"))
        addRow(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"))
        addRow(listOf("z", "x", "c", "v", "b", "n", "m"))
        addActionRow()
    }

    private fun addRow(keys: List<String>) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(48)).apply {
                topMargin = dp(4)
            }
        }
        keys.forEach { label ->
            row.addView(keyButton(label) { onAction(KeyAction.Commit(label)) })
        }
        addView(row)
    }

    private fun addActionRow() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(48)).apply {
                topMargin = dp(4)
            }
        }
        row.addView(keyButton("DEL", weight = 1.2f) { onAction(KeyAction.Backspace) })
        row.addView(keyButton("space", weight = 3f) { onAction(KeyAction.Commit(" ")) })
        row.addView(keyButton("enter", weight = 1.4f) { onAction(KeyAction.Enter) })
        addView(row)
    }

    private fun keyButton(
        label: String,
        weight: Float = 1f,
        onClick: () -> Unit,
    ): View {
        return Button(context).apply {
            text = label
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT
            setTextColor(0xFF212121.toInt())
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                marginStart = dp(2)
                marginEnd = dp(2)
            }
            gravity = Gravity.CENTER
            setOnClickListener { onClick() }
            contentDescription = label
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()
}
