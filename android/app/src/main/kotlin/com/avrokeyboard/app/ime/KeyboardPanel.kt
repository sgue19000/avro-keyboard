package com.avrokeyboard.app.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.avrokeyboard.app.ime.voice.VoiceState

class KeyboardPanel(
    context: Context,
    private val onAction: (KeyAction) -> Unit,
) : View(context) {

    var language = KeyboardLanguage.ENGLISH
        private set
    private var page = KeyboardPage.LETTERS
    private var shiftOn = false
    private var pressedIndex = -1
    private var voiceState = VoiceState.IDLE
    private var voiceHint: String? = null
    private var rows: List<List<KeySpec>> = Layouts.english(false)
    private val keyRects = mutableListOf<RectF>()
    private val keyMap = mutableListOf<KeySpec>()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val specialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val micPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private var night = isNight()

    init {
        applyPalette()
        isClickable = true
        contentDescription = "Avro Keyboard"
    }

    fun onHostStarted() {
        night = isNight()
        applyPalette()
        requestLayout()
        invalidate()
    }

    fun setVoiceState(state: VoiceState, hint: String?) {
        voiceState = state
        voiceHint = hint
        invalidate()
    }

    fun setMode(mode: KeyboardLanguage, notify: Boolean) {
        if (language == mode) {
            rebuild()
            return
        }
        val from = language
        language = mode
        page = KeyboardPage.LETTERS
        shiftOn = false
        rebuild()
        if (notify) onAction(KeyAction.ModeChanged(from, mode))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(1)
        val rowH = dp(48f)
        val gap = dp(6f)
        val pad = dp(8f)
        val banner = dp(22f)
        val height = (pad * 2 + banner + rows.size * rowH + (rows.size - 1) * gap + navInset()).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        layoutKeys()
        hintPaint.textSize = sp(13f)
        hintPaint.color = if (night) 0xFFB0B6C0.toInt() else 0xFF3C4043.toInt()
        canvas.drawText(bannerText(), width / 2f, dp(16f), hintPaint)
        keyMap.forEachIndexed { i, spec ->
            val rect = keyRects[i]
            val listening = spec.kind == KeyKind.MIC && voiceState != VoiceState.IDLE && voiceState != VoiceState.ERROR
            val paint = when {
                i == pressedIndex -> activePaint
                listening -> micPaint
                spec.kind == KeyKind.SHIFT && shiftOn -> activePaint
                spec.kind == KeyKind.LANGUAGE -> activePaint
                spec.kind == KeyKind.CHAR || spec.kind == KeyKind.SPACE -> keyPaint
                else -> specialPaint
            }
            canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
            val label = displayLabel(spec)
            textPaint.textSize = if (label.length <= 2) sp(18f) else sp(12f)
            textPaint.color = if (night) 0xFFF2F4F8.toInt() else 0xFF1B1D21.toInt()
            val cy = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, rect.centerX(), cy, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedIndex = hit(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
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
                onAction(KeyAction.Commit(spec.commit ?: spec.label))
                if (shiftOn && language == KeyboardLanguage.ENGLISH && page == KeyboardPage.LETTERS) {
                    shiftOn = false
                    rebuild()
                }
            }
            KeyKind.SPACE -> onAction(KeyAction.Commit(" "))
            KeyKind.BACKSPACE -> onAction(KeyAction.Backspace)
            KeyKind.ENTER -> onAction(KeyAction.Enter)
            KeyKind.MIC -> onAction(KeyAction.Mic)
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
                val next = when (language) {
                    KeyboardLanguage.ENGLISH -> KeyboardLanguage.BANGLA
                    KeyboardLanguage.BANGLA -> KeyboardLanguage.AVRO
                    KeyboardLanguage.AVRO -> KeyboardLanguage.ENGLISH
                }
                setMode(next, notify = true)
            }
        }
    }

    private fun rebuild() {
        rows = when {
            page == KeyboardPage.SYMBOLS -> Layouts.symbols(language)
            language == KeyboardLanguage.BANGLA && shiftOn -> Layouts.banglaVowels()
            language == KeyboardLanguage.BANGLA -> Layouts.banglaConsonants()
            else -> Layouts.english(shiftOn)
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
        var top = pad + dp(22f)
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

    private fun hit(x: Float, y: Float): Int = keyRects.indexOfFirst { it.contains(x, y) }

    private fun bannerText(): String {
        val base = when (language) {
            KeyboardLanguage.ENGLISH -> "বাংলা  |  English  |  অভ্র"
            KeyboardLanguage.BANGLA -> "[বাংলা]  English  অভ্র"
            KeyboardLanguage.AVRO -> "বাংলা  English  [অভ্র]"
        }
        return when (voiceState) {
            VoiceState.LISTENING -> "listening"
            VoiceState.RECOGNIZING -> "recognizing"
            VoiceState.REQUESTING_PERMISSION -> "mic permission"
            VoiceState.ERROR -> voiceHint ?: "mic error"
            else -> base
        }
    }

    private fun displayLabel(spec: KeySpec): String = when (spec.kind) {
        KeyKind.SHIFT -> when {
            language == KeyboardLanguage.BANGLA && shiftOn -> "ক"
            language == KeyboardLanguage.BANGLA -> "স্বর"
            shiftOn -> "SHIFT"
            else -> "shift"
        }
        KeyKind.LANGUAGE -> when (language) {
            KeyboardLanguage.ENGLISH -> "EN"
            KeyboardLanguage.BANGLA -> "বা"
            KeyboardLanguage.AVRO -> "অভ্র"
        }
        KeyKind.MIC -> if (voiceState == VoiceState.IDLE) "\uD83C\uDFA4" else "\u25A0"
        KeyKind.PAGE_SYMBOLS -> "123"
        KeyKind.PAGE_LETTERS -> if (language == KeyboardLanguage.BANGLA) "কখ" else "ABC"
        KeyKind.BACKSPACE -> "DEL"
        KeyKind.ENTER -> "enter"
        KeyKind.SPACE -> ""
        KeyKind.CHAR -> spec.label
    }

    private fun applyPalette() {
        micPaint.color = 0xFFE53935.toInt()
        if (night) {
            bgPaint.color = 0xFF121418.toInt()
            keyPaint.color = 0xFF2A2E36.toInt()
            specialPaint.color = 0xFF1C2026.toInt()
            activePaint.color = 0xFF3D6FD9.toInt()
        } else {
            bgPaint.color = 0xFFE8EAED.toInt()
            keyPaint.color = 0xFFFFFFFF.toInt()
            specialPaint.color = 0xFFD3D7DE.toInt()
            activePaint.color = 0xFFBBDEFB.toInt()
        }
    }

    private fun isNight(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    private fun navInset(): Float {
        val insets = rootWindowInsets ?: return dp(8f)
        return if (Build.VERSION.SDK_INT >= 30) {
            insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom.toFloat()
        } else {
            @Suppress("DEPRECATION")
            insets.systemWindowInsetBottom.toFloat()
        }.coerceAtLeast(dp(6f))
    }

    private fun dp(v: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)

    private fun sp(v: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}
