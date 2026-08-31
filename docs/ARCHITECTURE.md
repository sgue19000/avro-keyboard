# Avro Keyboard architecture (Step 1)

Flutter cannot act as an Android system IME on its own. Android only treats a service that extends `InputMethodService` and is advertised with `android.view.InputMethod` as a keyboard.

```
Flutter application (lib/)
  settings, later themes / Avro options
        |  MethodChannel com.avrokeyboard.app/ime
        v
Kotlin MainActivity
        |  (does not type into other apps)
        v
Kotlin AvroKeyboardService : InputMethodService
        |  currentInputConnection
        v
Any Android app (WhatsApp, Chrome, Notes, ...)
```

## How the IME is registered

1. `android/app/src/main/AndroidManifest.xml` declares `com.avrokeyboard.app.ime.AvroKeyboardService` with `android.permission.BIND_INPUT_METHOD` and the `android.view.InputMethod` intent filter.
2. Meta-data `android.view.im` points at `@xml/method`.
3. After install, Android lists "Avro Keyboard" under Settings → System → Keyboard → On-screen keyboard.

## Future Flutter to IME integration

- Keep `AvroKeyboardService` as the IME process entry.
- Host Flutter UI inside `onCreateInputView()` or keep a native view and drive it from settings stored by the Flutter app.
- Dart must never call `commitText` directly. It asks Kotlin to do so while `currentInputConnection` is non-null.

Step 1 stops here: enable-able IME + minimal QWERTY + settings host.
