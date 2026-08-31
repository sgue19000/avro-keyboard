# Architecture

```
Flutter settings
        |
Kotlin AvroKeyboardService
  KeyboardPanel (layouts + mode)
  AvroComposer + AvroEngine     // অভ্র only
        |
InputConnection.setComposingText / commitText / deleteSurroundingText
```

Step 4+ should not fold suggestion or STT into AvroEngine.
