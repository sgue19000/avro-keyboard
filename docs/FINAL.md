# Final scope (6/6)

Implemented

- System IME (`com.avrokeyboard.app`)
- বাংলা direct layout
- English QWERTY
- অভ্র rule-based Banglish (not full desktop Avro)
- Mic key using Android `SpeechRecognizer`
- Mode remembered locally

Not implemented

- Cloud sync, accounts, ads, analytics, AI suggestions
- Perfect Avro parity
- Guaranteed offline or bilingual speech
- Play-signed release keystore (CI uses debug signing for the release APK/AAB)

Privacy

- No keystroke or speech logs in this app
- Device speech provider may process audio under its own policy
