import 'package:avro_keyboard/avro/avro_engine.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final engine = AvroEngine();

  test('required words', () {
    expect(engine.parse('ami'), 'আমি');
    expect(engine.parse('tumi'), 'তুমি');
    expect(engine.parse('bhalo'), 'ভালো');
    expect(engine.parse('bangla'), 'বাংলা');
    expect(engine.parse('kemon acho'), 'কেমন আছো');
    expect(engine.parse('amar nam rafi'), 'আমার নাম রাফি');
    expect(engine.parse('dhonnobad'), 'ধন্যবাদ');
    expect(engine.parse('ami bangla likhi'), 'আমি বাংলা লিখি');
  });

  test('clusters', () {
    expect(engine.parse('ksh'), 'ক্ষ');
    expect(engine.parse('ggy'), 'জ্ঞ');
    expect(engine.parse('tr'), 'ত্র');
    expect(engine.parse('shr'), 'শ্র');
    expect(engine.parse('prem'), 'প্রেম');
    expect(engine.parse('swa'), 'স্বা');
  });

  test('composer backspace', () {
    final c = AvroComposer();
    c.type('a');
    c.type('m');
    c.type('i');
    expect(c.preview, 'আমি');
    c.backspace();
    expect(c.preview, 'আম');
  });
}
