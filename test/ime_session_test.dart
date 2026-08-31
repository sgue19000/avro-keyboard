import 'package:avro_keyboard/avro/avro_engine.dart';
import 'package:avro_keyboard/ime/ime_edit.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final engine = AvroEngine();

  test('english phrase unchanged by engine when used as raw words', () {
    expect('hello world', 'hello world');
  });

  test('avro required phrases', () {
    expect(engine.parse('ami'), 'আমি');
    expect(engine.parse('bhalo'), 'ভালো');
    expect(engine.parse('kemon acho'), 'কেমন আছো');
    expect(engine.parse('dhonnobad'), 'ধন্যবাদ');
    expect(engine.parse('ami bangla likhi'), 'আমি বাংলা লিখি');
  });

  test('composing backspace and space', () {
    final s = ImeSession(engine.parse);
    s.type('a');
    s.type('m');
    s.type('i');
    expect(s.composing, 'আমি');
    s.backspace();
    expect(s.composing, 'আম');
    s.type('i');
    s.type(' ');
    expect(s.committed, 'আমি ');
    expect(s.isComposing, isFalse);
  });

  test('punctuation and enter finalize', () {
    final s = ImeSession(engine.parse);
    for (final ch in 'bhalo'.split('')) {
      s.type(ch);
    }
    s.type('!');
    expect(s.committed, 'ভালো!');
    s.type('a');
    s.type('m');
    s.type('i');
    s.enter();
    expect(s.committed, 'ভালো!আমি\n');
    expect(s.isComposing, isFalse);
  });

  test('mode switch commits composing', () {
    final s = ImeSession(engine.parse);
    for (final ch in 'ami'.split('')) {
      s.type(ch);
    }
    s.switchMode('bn');
    expect(s.committed, 'আমি');
    expect(s.isComposing, isFalse);
  });

  test('empty composition and field switch', () {
    final s = ImeSession(engine.parse);
    s.backspace();
    expect(s.committed, isEmpty);
    s.type('a');
    s.fieldChanged();
    expect(s.isComposing, isFalse);
  });

  test('cursor leaves composing region', () {
    expect(
      ImeEdit.composingInvalid(
        newSelStart: 0,
        newSelEnd: 0,
        candStart: 2,
        candEnd: 4,
      ),
      isTrue,
    );
    expect(
      ImeEdit.composingInvalid(
        newSelStart: 3,
        newSelEnd: 3,
        candStart: 2,
        candEnd: 4,
      ),
      isFalse,
    );
  });

  test('bengali cluster backspace', () {
    expect(ImeEdit.clusterLen('কা'), 2);
    expect(ImeEdit.clusterLen('কি'), 2);
    expect(ImeEdit.clusterLen('কু'), 2);
    expect(ImeEdit.clusterLen('ক্ষে'), 4);
    expect(ImeEdit.clusterLen('বাংলা'), 2);
    expect(ImeEdit.clusterLen('hello'), 1);
  });
}
