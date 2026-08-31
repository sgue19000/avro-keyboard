package com.avrokeyboard.app.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/**
 * Drawn keyboard used by the IME. Not a Flutter widget.
 * Layout state stays here so Step 3 can swap the BANGLA letter page
 * for an Avro-aware page without touching InputConnection plumbing.
 */
class KeyboardPanel(
    context: Context,
    private val onAction: (KeyAction) -> Unit,
) : View(context) {

    private var language = KeyboardLanguage.ENGLISH
    private var page = KeyboardPage.LETTERS
    private var shiftOn = false
    private var pressedIndex = -1
    private var rows: List<List<KeySpec>> = englishLetterRows(shiftOn)
    private val keyRects = mutableListOf<RectF>()
    private val keyMap = mutableListOf<KeySpec>()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val specialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private var night = isNight()

    init {
        applyPalette()
        isClickable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Avro Keyboard"
    }

    fun onHostStarted() {
        night = isNight()
        applyPalette()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(1)
        val rowH = dp(48f)
        val gap = dp(6f)
        val pad = dp(8f)
        val banner = if (language == KeyboardLanguage.BANGLA && page == KeyboardPage.LETTERS) dp(22f) else 0f
        val rowsCount = rows.size
        val height = (pad * 2 + banner + rowsCount * rowH + (rowsCount - 1) * gap + navInset()).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        layoutKeys()
        if (language == KeyboardLanguage.BANGLA && page == KeyboardPage.LETTERS) {
            hintPaint.textSize = sp(12f)
            canvas.drawText(
                "Bangla layout placeholder — Avro engine in Step 3",
                width / 2f,
                dp(18f),
                hintPaint,
            )
        }
        keyMap.forEachIndexed { i, spec ->
            val rect = keyRects[i]
            val paint = when {
                i == pressedIndex -> activePaint
                spec.kind == KeyKind.SHIFT && shiftOn -> activePaint
                spec.kind == KeyKind.CHAR || spec.kind == KeyKind.SPACE -> keyPaint
                else -> specialPaint
            }
            canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
            textPaint.textSize = if (spec.kind == KeyKind.CHAR) sp(20f) else sp(14f)
            textPaint.color = if (night) 0xFFF2F4F8.toInt() else 0xFF1B1D21.toInt()
            val label = displayLabel(spec)
            val cy = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, rect.centerX(), cy, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = hit(event.x, event.y)
                pressedIndex = idx
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val idx = pressedIndex
                pressedIndex = -1
                invalidate()
                if (idx in keyMap.indices) dispatch(keyMap[idx])
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedIndex = -1
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dispatch(spec: KeySpec) {
        when (spec.kind) {
            KeyKind.CHAR -> {
                val text = spec.commit ?: spec.label
                onAction(KeyAction.Commit(text))
                if (shiftOn && page == KeyboardPage.LETTERS && language == KeyboardLanguage.ENGLISH) {
                    shiftOn = false
                    rebuild()
                }
            }
            KeyKind.SPACE -> onAction(KeyAction.Commit(" "))
            KeyKind.BACKSPACE -> onAction(KeyAction.Backspace)
            KeyKind.ENTER -> onAction(KeyAction.Enter)
            KeyKind.SHIFT -> {
                shiftOn = !shiftOn
                rebuild()
            }
            KeyKind.PAGE_SYMBOLS -> {
                page = KeyboardPage.SYMBOLS
                shiftOn = false
                rebuild()
            }
            KeyKind.PAGE_LETTERS -> {
                page = KeyboardPage.LETTERS
                rebuild()
            }
            KeyKind.LANGUAGE -> {
                language = if (language == KeyboardLanguage.ENGLISH) {
                    KeyboardLanguage.BANGLA
                } else {
                    KeyboardLanguage.ENGLISH
                }
                page = KeyboardPage.LETTERS
                shiftOn = false
                rebuild()
            }
        }
    }

    private fun rebuild() {
        rows = when {
            page == KeyboardPage.SYMBOLS -> symbolRows()
            language == KeyboardLanguage.BANGLA -> banglaPlaceholderRows()
            else -> englishLetterRows(shiftOn)
        }
        requestLayout()
        invalidate()
    }

    private fun layoutKeys() {
        keyRects.clear()
        keyMap.clear()
        val pad = dp(6f)
        val gap = dp(5f)
        val rowH = dp(48f)
        var top = pad + if (language == KeyboardLanguage.BANGLA && page == KeyboardPage.LETTERS) dp(22f) else 0f
        val innerW = width - pad * 2
        rows.forEach { row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            var x = pad
            row.forEach { spec ->
                val w = innerW * (spec.weight / totalWeight)
                keyRects.add(RectF(x + gap / 2f, top, x + w - gap / 2f, top + rowH))
                keyMap.add(spec)
                x += w
            }
            top += rowH + gap
        }
    }

    private fun hit(x: Float, y: Float): Int =
        keyRects.indexOfFirst { it.contains(x, y) }

    private fun displayLabel(spec: KeySpec): String = when (spec.kind) {
        KeyKind.SHIFT -> if (shiftOn) "SHIFT" else "shift"
        KeyKind.LANGUAGE -> if (language == KeyboardLanguage.ENGLISH) "EN" else "\u09AC\u09BE"
        KeyKind.PAGE_SYMBOLS -> "123"
        KeyKind.PAGE_LETTERS -> "ABC"
        KeyKind.BACKSPACE -> "DEL"
        KeyKind.ENTER -> "enter"
        KeyKind.SPACE -> ""
        KeyKind.CHAR -> spec.label
    }

    private fun applyPalette() {
        if (night) {
            bgPaint.color = 0xFF121418.toInt()
            keyPaint.color = 0xFF2A2E36.toInt()
            specialPaint.color = 0xFF1C2026.toInt()
            activePaint.color = 0xFF3D6FD9.toInt()
            hintPaint.color = 0xFFB0B6C0.toInt()
        } else {
            bgPaint.color = 0xFFE8EAED.toInt()
            keyPaint.color = 0xFFFFFFFF.toInt()
            specialPaint.color = 0xFFD3D7DE.toInt()
            activePaint.color = 0xFFBBDEFB.toInt()
            hintPaint.color = 0xFF5F6368.toInt()
        }
    }

    private fun isNight(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun navInset(): Float {
        val insets = rootWindowInsets ?: return dp(8f)
        return if (Build.VERSION.SDK_INT >= 30) {
            insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom.toFloat()
        } else {
            @Suppress("DEPRECATION")
            insets.systemWindowInsetBottom.toFloat()
        }.coerceAtLeast(dp(6f))
    }

    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    private fun sp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

    companion object {
        private fun c(label: String, weight: Float = 1f) =
            KeySpec(KeyKind.CHAR, label, commit = label, weight = weight)

        fun englishLetterRows(shift: Boolean): List<List<KeySpec>> {
            fun row(chars: String) = chars.map { ch ->
                val shown = if (shift) ch.uppercaseChar().toString() else ch.toString()
                c(shown)
            }
            return listOf(
                row("qwertyuiop"),
                row("asdfghjkl"),
                listOf(
                    KeySpec(KeyKind.SHIFT, "shift", weight = 1.4f),
                ) + row("zxcvbnm") + listOf(
                    KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.4f),
                ),
                listOf(
                    KeySpec(KeyKind.PAGE_SYMBOLS, "123", weight = 1.3f),
                    KeySpec(KeyKind.LANGUAGE, "EN", weight = 1.1f),
                    KeySpec(KeyKind.CHAR, ",", commit = ",", weight = 0.9f),
                    KeySpec(KeyKind.SPACE, "space", weight = 3.6f, wide = true),
                    KeySpec(KeyKind.CHAR, ".", commit = ".", weight = 0.9f),
                    KeySpec(KeyKind.ENTER, "enter", weight = 1.5f),
                ),
            )
        }

        fun symbolRows(): List<List<KeySpec>> = listOf(
            "1234567890".map { c(it.toString()) },
            listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")").map { c(it) },
            listOf("!", "?", "'", "\"", ":", ";", "/", "_").map { c(it) } +
                listOf(KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.4f)),
            listOf(
                KeySpec(KeyKind.PAGE_LETTERS, "ABC", weight = 1.3f),
                KeySpec(KeyKind.LANGUAGE, "EN", weight = 1.1f),
                KeySpec(KeyKind.CHAR, ",", commit = ",", weight = 0.9f),
                KeySpec(KeyKind.SPACE, "space", weight = 3.6f, wide = true),
                KeySpec(KeyKind.CHAR, ".", commit = ".", weight = 0.9f),
                KeySpec(KeyKind.ENTER, "enter", weight = 1.5f),
            ),
        )

        /** Visible BN page so EN ↔ বাংলা can switch. Not Avro. */
        fun banglaPlaceholderRows(): List<List<KeySpec>> {
            val row1 = listOf("ক", "খ", "গ", "ঘ", "চ", "ছ", "ট", "ঠ", "ত", "থ")
            val row2 = listOf("দ", "ন", "প", "ব", "ম", "য", "র", "ল", "শ")
            val row3 = listOf("স", "হ", "আ", "ই", "উ", "এ", "ও")
            return listOf(
                row1.map { c(it) },
                row2.map { c(it) },
                listOf(KeySpec(KeyKind.SHIFT, "", weight = 1.2f)) +
                    row3.map { c(it) } +
                    listOf(KeySpec(KeyKind.BACKSPACE, "DEL", weight = 1.4f)),
                listOf(
                    KeySpec(KeyKind.PAGE_SYMBOLS, "123", weight = 1.3f),
                    KeySpec(KeyKind.LANGUAGE, "BN", weight = 1.1f),
                    KeySpec(KeyKind.CHAR, ",", commit = ",", weight = 0.9f),
                    KeySpec(KeyKind.SPACE, "space", weight = 3.6f, wide = true),
                    KeySpec(KeyKind.CHAR, "।", commit = "।", weight = 0.9f),
                    KeySpec(KeyKind.ENTER, "enter", weight = 1.5f),
                ),
            )
        }
    }

    @Suppress("unused")
    private val touchStamp = SystemClock.uptimeMillis()
}
