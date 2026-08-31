/// Pure editing rules shared with the Kotlin IME (keep behavior aligned).
class ImeEdit {
  static bool composingInvalid({
    required int newSelStart,
    required int newSelEnd,
    required int candStart,
    required int candEnd,
  }) {
    if (candStart < 0 || candEnd < 0) return true;
    if (newSelStart != newSelEnd) return true;
    return newSelStart < candStart || newSelStart > candEnd;
  }

  static int clusterLen(String text) {
    if (text.isEmpty) return 0;
    var i = text.length;
    while (i > 0 && _isMark(text.codeUnitAt(i - 1))) {
      i--;
    }
    if (i == 0) return text.length;
    i--;
    while (i >= 2 &&
        text.codeUnitAt(i - 1) == 0x09CD &&
        _isBnCons(text.codeUnitAt(i - 2))) {
      i -= 2;
    }
    return text.length - i;
  }

  static bool _isMark(int c) =>
      (c >= 0x0981 && c <= 0x0983) ||
      c == 0x09BC ||
      c == 0x09BE ||
      (c >= 0x09BF && c <= 0x09C4) ||
      (c >= 0x09C7 && c <= 0x09C8) ||
      (c >= 0x09CB && c <= 0x09CD) ||
      c == 0x09D7;

  static bool _isBnCons(int c) =>
      (c >= 0x0995 && c <= 0x09B9) ||
      (c >= 0x09DC && c <= 0x09DF) ||
      c == 0x09CE;
}

/// In-memory Avro IME session used by tests.
class ImeSession {
  ImeSession(this.parse);

  final String Function(String) parse;
  String buffer = '';
  String committed = '';
  String composing = '';
  String mode = 'avro';

  void type(String ch) {
    if (mode != 'avro') {
      committed += ch;
      return;
    }
    if (ch == ' ' || (ch.length == 1 && !_isLetter(ch) && ch != '`')) {
      committed += parse(buffer) + ch;
      buffer = '';
      composing = '';
      return;
    }
    buffer += ch;
    composing = parse(buffer);
  }

  void backspace() {
    if (buffer.isNotEmpty) {
      buffer = buffer.substring(0, buffer.length - 1);
      composing = buffer.isEmpty ? '' : parse(buffer);
      return;
    }
    if (committed.isEmpty) return;
    final n = ImeEdit.clusterLen(committed);
    committed = committed.substring(0, committed.length - n);
  }

  void enter() {
    committed += parse(buffer);
    if (mode == 'avro' && buffer.isNotEmpty) {
      // word finalized; newline is a boundary
    }
    buffer = '';
    composing = '';
    committed += '\n';
  }

  void switchMode(String next) {
    committed += parse(buffer);
    buffer = '';
    composing = '';
    mode = next;
  }

  void fieldChanged() {
    buffer = '';
    composing = '';
  }

  bool get isComposing => buffer.isNotEmpty;

  static bool _isLetter(String ch) {
    final c = ch.codeUnitAt(0);
    return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
  }
}
