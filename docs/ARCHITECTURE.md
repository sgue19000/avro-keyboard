# Architecture

```
Flutter settings (optional)
        |
AvroKeyboardService
  KeyboardPanel + Layouts
  AvroComposer / AvroEngine     // typed Banglish only
  VoiceTyping / SpeechRecognizer // speech, no Avro
  ImeEdit / ImePrefs
        |
InputConnection
```
