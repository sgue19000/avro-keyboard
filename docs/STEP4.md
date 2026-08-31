# Step 4 — IME integration

The IME is a standalone `InputMethodService`. Flutter settings are optional.

## Lifecycle

- `onCreateInputView` builds `KeyboardPanel` and restores the last mode.
- `onStartInput` clears the Avro buffer without writing into the previous field.
- `onStartInputView` reapplies mode and theme.
- `onFinishInput` / `onFinishInputView` commit any live composition.
- `onUpdateSelection` commits if the cursor leaves the composing span.
- `onConfigurationChanged` relayouts the panel.

## InputConnection

- Avro letters: `setComposingText`
- Space / punctuation / Enter / mode change: `commitText`
- Selection present: `commitText` replaces it
- Backspace: composer edit, or `ImeEdit.deleteBefore` (grapheme-aware)

## Editor actions

Multiline or `IME_FLAG_NO_ENTER_ACTION` inserts a newline.
Otherwise `performEditorAction` for Done / Next / Search / Go / Send.

## Input types

Password fields skip Avro composing and never persist keystrokes.
Other types keep the current layout; the IME does not crash on unknown types.

## Privacy

SharedPreferences stores **mode only**. No typed text, no logs, no network.

## Dual engine tables

`lib/avro/avro_engine.dart` and `ime/avro/AvroEngine.kt` must stay in sync.
