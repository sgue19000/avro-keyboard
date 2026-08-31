/// Avro-style Banglish to Bengali engine.
/// Longest-match patterns plus prefix/suffix context rules.
class AvroEngine {
  static const vowels = 'aeiou';
  static const consonants = 'bcdfghjklmnpqrstvwxyz';

  String parse(String input) {
    if (input.isEmpty) return '';
    final out = StringBuffer();
    final buf = StringBuffer();
    void flush() {
      if (buf.isEmpty) return;
      out.write(parseWord(buf.toString()));
      buf.clear();
    }

    for (var i = 0; i < input.length; i++) {
      final ch = input[i];
      if (_isLatinLetter(ch) || ch == '`') {
        buf.write(ch);
      } else {
        flush();
        out.write(ch);
      }
    }
    flush();
    return out.toString();
  }

  String parseWord(String raw) {
    if (raw.isEmpty) return '';
    if (raw.startsWith('`')) {
      return raw.substring(1);
    }
    final word = _fixCase(raw);
    final exception = _exceptions[word];
    if (exception != null) return exception;

    final out = StringBuffer();
    var i = 0;
    while (i < word.length) {
      final hit = _match(word, i);
      out.write(hit.replace);
      i += hit.consumed;
    }
    return out.toString();
  }

  _Hit _match(String word, int i) {
    for (final p in _patterns) {
      if (i + p.find.length > word.length) continue;
      if (word.substring(i, i + p.find.length) != p.find) continue;
      if (p.rules.isEmpty) {
        return _Hit(p.find.length, p.replace);
      }
      for (final rule in p.rules) {
        if (rule.matches.every((m) => _ok(m, word, i, i + p.find.length))) {
          return _Hit(p.find.length, rule.replace);
        }
      }
      return _Hit(p.find.length, p.replace);
    }
    return _Hit(1, word[i]);
  }

  bool _ok(_Match m, String word, int start, int end) {
    if (m.prefix) {
      if (start == 0) return false;
      return _check(m, word, start - 1);
    }
    if (end >= word.length) return false;
    return _check(m, word, end);
  }

  bool _check(_Match m, String word, int index) {
    if (index < 0 || index >= word.length) return false;
    final ch = word[index].toLowerCase();
    return switch (m.kind) {
      _Kind.vowel => AvroEngine.vowels.contains(ch),
      _Kind.consonant => AvroEngine.consonants.contains(ch),
    };
  }

  String _fixCase(String text) {
    final b = StringBuffer();
    for (final ch in text.split('')) {
      if ('OITDNSRZ'.contains(ch)) {
        b.write(ch);
      } else {
        b.write(ch.toLowerCase());
      }
    }
    return b.toString();
  }

  bool _isLatinLetter(String ch) {
    final c = ch.codeUnitAt(0);
    return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
  }
}

class AvroComposer {
  AvroComposer({AvroEngine? engine}) : engine = engine ?? AvroEngine();

  final AvroEngine engine;
  String buffer = '';

  String get preview => engine.parse(buffer);

  void type(String ch) => buffer += ch;

  void backspace() {
    if (buffer.isEmpty) return;
    buffer = buffer.substring(0, buffer.length - 1);
  }

  String commitWord() {
    final text = preview;
    buffer = '';
    return text;
  }
}

class _Hit {
  const _Hit(this.consumed, this.replace);
  final int consumed;
  final String replace;
}

enum _Kind { vowel, consonant }

class _Match {
  const _Match.prefix(this.kind) : prefix = true;
  const _Match.suffix(this.kind) : prefix = false;
  final bool prefix;
  final _Kind kind;
}

class _Rule {
  const _Rule(this.replace, this.matches);
  final String replace;
  final List<_Match> matches;
}

class _Pat {
  const _Pat(this.find, this.replace, [this.rules = const []]);
  final String find;
  final String replace;
  final List<_Rule> rules;
}

const _pv = _Kind.vowel;
const _pc = _Kind.consonant;

const _patterns = <_Pat>[
  _Pat('ksh', 'ক্ষ'),
  _Pat('cch', 'ছ'),
  _Pat('chh', 'ছ'),
  _Pat('ngo', 'ঙ্গ'),
  _Pat('nno', 'ন্য'),
  _Pat('nyo', 'ন্য'),
  _Pat('kh', 'খ'),
  _Pat('gh', 'ঘ'),
  _Pat('ch', 'চ'),
  _Pat('jh', 'ঝ'),
  _Pat('Th', 'ঠ'),
  _Pat('Dh', 'ঢ'),
  _Pat('th', 'থ'),
  _Pat('dh', 'ধ'),
  _Pat('ph', 'ফ'),
  _Pat('bh', 'ভ'),
  _Pat('sh', 'শ'),
  _Pat('Ng', 'ঙ'),
  _Pat('ng', 'ং'),
  _Pat('rh', 'ড়'),
  _Pat('Rh', 'ঢ়'),
  _Pat('aa', 'আ'),
  _Pat('ee', 'ঈ'),
  _Pat('oo', 'ঊ'),
  _Pat('oi', 'ঐ', [_Rule('ৈ', [_Match.prefix(_pc)])]),
  _Pat('ou', 'ঔ', [_Rule('ৌ', [_Match.prefix(_pc)])]),
  _Pat('a', 'আ', [_Rule('া', [_Match.prefix(_pc)])]),
  _Pat('i', 'ই', [_Rule('ি', [_Match.prefix(_pc)])]),
  _Pat('I', 'ঈ', [_Rule('ী', [_Match.prefix(_pc)])]),
  _Pat('u', 'উ', [_Rule('ু', [_Match.prefix(_pc)])]),
  _Pat('e', 'এ', [_Rule('ে', [_Match.prefix(_pc)])]),
  _Pat('o', 'ও', [
    _Rule('', [_Match.prefix(_pc), _Match.suffix(_pc)]),
    _Rule('ো', [_Match.prefix(_pc)]),
  ]),
  _Pat('O', 'ও', [_Rule('ো', [_Match.prefix(_pc)])]),
  _Pat('y', 'য', [_Rule('য়', [_Match.prefix(_pv)])]),
  _Pat('r', 'র', [_Rule('্র', [_Match.prefix(_pc)])]),
  _Pat('k', 'ক'),
  _Pat('g', 'গ'),
  _Pat('c', 'চ'),
  _Pat('j', 'জ'),
  _Pat('T', 'ট'),
  _Pat('D', 'ড'),
  _Pat('t', 'ত'),
  _Pat('d', 'দ'),
  _Pat('N', 'ণ'),
  _Pat('n', 'ন'),
  _Pat('p', 'প'),
  _Pat('f', 'ফ'),
  _Pat('b', 'ব'),
  _Pat('m', 'ম'),
  _Pat('l', 'ল'),
  _Pat('S', 'ষ'),
  _Pat('s', 'স'),
  _Pat('h', 'হ'),
  _Pat('z', 'য'),
  _Pat('w', 'ও'),
  _Pat('v', 'ভ'),
  _Pat('x', 'ক্স'),
  _Pat('q', 'ক'),
];

const _exceptions = <String, String>{
  'acho': 'আছো',
  'achho': 'আছো',
  'accho': 'আছো',
  'dhonnobad': 'ধন্যবাদ',
  'dhonnyobad': 'ধন্যবাদ',
};
