# Avro Keyboard

Free, open-source Android system keyboard. Flutter settings UI + Kotlin `InputMethodService` IME.

**Step 1/6 — foundation only.** English QWERTY stub so the IME can be enabled from Android keyboard settings and type into other apps. Bengali and Avro phonetic typing come later.

## Enable the IME

1. Install the debug APK.
2. Open **Avro Keyboard**.
3. Tap **Enable keyboard in system settings**.
4. Turn on **Avro Keyboard**.
5. Tap **Choose current keyboard** (or the keyboard icon in a text field).

Path on stock Android:

`Settings → System → Keyboard → On-screen keyboard`

## Build

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```

Requires Flutter stable (Dart SDK `>=3.4.0 <4.0.0`). Android Gradle Plugin 8.11.1, Kotlin 2.2.20, Gradle 8.14.3.

## License

Open source. No paid APIs.
