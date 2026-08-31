# Architecture

```
Flutter settings (optional)
  MethodChannel openInputMethodSettings / openInputMethodPicker
        |
Kotlin AvroKeyboardService   // required for typing
  ImePrefs                   // mode only
  KeyboardPanel
  AvroComposer + AvroEngine
  ImeEdit                    // enter, grapheme delete, selection
        |
InputConnection
```

The keyboard works with the Flutter activity closed.
