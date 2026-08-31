package com.avrokeyboard.app

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Flutter settings host.
 *
 * Channel `com.avrokeyboard.app/ime` is the safe seam between Flutter and
 * Android. Later steps can add methods such as `setLayout` or `setEngine`
 * here. Never commit text from this Activity — only [ime.AvroKeyboardService]
 * owns the InputConnection.
 */
class MainActivity : FlutterActivity() {
    private val channelName = "com.avrokeyboard.app/ime"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "openInputMethodSettings" -> {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        result.success(null)
                    }
                    "openInputMethodPicker" -> {
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }
}
