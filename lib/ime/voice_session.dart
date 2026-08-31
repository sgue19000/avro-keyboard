enum VoiceState {
  idle,
  requestingPermission,
  listening,
  recognizing,
  finalizing,
  error,
}

/// Mirrors Kotlin VoiceSession. Partial results replace, never append.
class VoiceSession {
  VoiceState state = VoiceState.idle;
  String partial = '';
  String? lastError;

  bool get isActive =>
      state == VoiceState.listening ||
      state == VoiceState.recognizing ||
      state == VoiceState.requestingPermission;

  void requestPermission() {
    state = VoiceState.requestingPermission;
    lastError = null;
  }

  void startListening() {
    state = VoiceState.listening;
    partial = '';
    lastError = null;
  }

  void onPartial(String text) {
    if (state != VoiceState.listening && state != VoiceState.recognizing) {
      return;
    }
    if (text.trim().isEmpty) return;
    partial = text.trim();
    state = VoiceState.recognizing;
  }

  String? onFinal(String text) {
    state = VoiceState.finalizing;
    final out = text.trim().isEmpty ? partial : text.trim();
    partial = '';
    state = VoiceState.idle;
    return out.isEmpty ? null : out;
  }

  String? cancel() {
    final leftover = partial.trim().isEmpty ? null : partial.trim();
    partial = '';
    state = VoiceState.idle;
    lastError = null;
    return leftover;
  }

  void fail(String code) {
    partial = '';
    lastError = code;
    state = VoiceState.error;
  }

  void recover() {
    if (state == VoiceState.error) {
      state = VoiceState.idle;
      lastError = null;
    }
  }

  void reset() {
    partial = '';
    lastError = null;
    state = VoiceState.idle;
  }
}
