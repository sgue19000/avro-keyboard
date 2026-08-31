import 'package:avro_keyboard/avro/avro_engine.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final engine = AvroEngine();

  test('empty input', () {
    expect(engine.parse(''), '');
    expect(engine.parseWord(''), '');
  });

  test('required words', () {
    expect(engine.parse('ami'), 'আমি');
    expect(engine.parse('tumi'), 'তুমি');
    expect(engine.parse('bhalo'), 'ভালো');
    expect(engine.parse('bangla'), 'বাংলা');
    expect(engine.parse('kemon'), 'কেমন');
    expect(engine.parse('acho'), 'আছো');
    expect(engine.parse('amar'), 'আমার');
    expect(engine.parse('nam'), 'নাম');
    expect(engine.parse('rafi'), 'রাফি');
    expect(engine.parse('dhonnobad'), 'ধন্যবাদ');
  });

  test('phrases and spaces', () {
    expect(engine.parse('kemon acho'), 'কেমন আছো');
    expect(engine.parse('amar nam rafi'), 'আমার নাম রাফি');
    expect(engine.parse('ami banglay likhi'), 'আমি বাংলায় লিখি');
    expect(engine.parse('ami bangla likhi'), 'আমি বাংলা লিখি');
  });

  test('english raw fallback with backtick', () {
    expect(engine.parse('`hello'), 'hello');
    expect(engine.parse('`ami'), 'ami');
  });

  test('punctuation finalizes word boundaries', () {
    expect(engine.parse('ami,'), 'আমি,');
    expect(engine.parse('bhalo!'), 'ভালো!');
    expect(engine.parse('nam.'), 'নাম.');
  });

  test('vowel signs and conjuncts', () {
    expect(engine.parse('ka'), 'কা');
    expect(engine.parse('ki'), 'কি');
    expect(engine.parse('ku'), 'কু');
    expect(engine.parse('ksh'), 'ক্ষ');
    expect(engine.parse('kho'), 'খো');
    expect(engine.parse('bhai'), 'ভাই');
  });

  test('repeated letters', () {
    expect(engine.parse('aa'), 'আ');
    expect(engine.parse('ee'), 'ঈ');
    expect(engine.parse('oo'), 'ঊ');
  });

  test('composer backspace', () {
    final c = AvroComposer();
    c.type('a');
    c.type('m');
    c.type('i');
    expect(c.preview, 'আমি');
    c.backspace();
    expect(c.preview, 'আম');
    c.backspace();
    c.backspace();
    expect(c.preview, '');
    expect(c.commitWord(), '');
  });

  test('composer space commit', () {
    final c = AvroComposer();
    for (final ch in 'ami'.split('')) {
      c.type(ch);
    }
    expect(c.commitWord(), 'আমি');
    expect(c.buffer, isEmpty);
  });

  test('unknown characters pass through', () {
    expect(engine.parse('123'), '123');
    expect(engine.parse('ami 2'), 'আমি 2');
  });
}
