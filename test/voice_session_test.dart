import 'package:avro_keyboard/ime/voice_session.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('idle to listening to partial replace to final', () {
    final s = VoiceSession();
    expect(s.state, VoiceState.idle);
    s.startListening();
    expect(s.state, VoiceState.listening);
    s.onPartial('আমি');
    s.onPartial('আমি আজ');
    s.onPartial('আমি আজ বাজারে');
    expect(s.partial, 'আমি আজ বাজারে');
    expect(s.partial.contains('আমি আমি'), isFalse);
    final done = s.onFinal('আমি আজ বাজারে যাব');
    expect(done, 'আমি আজ বাজারে যাব');
    expect(s.state, VoiceState.idle);
    expect(s.partial, isEmpty);
  });

  test('cancel during listening', () {
    final s = VoiceSession();
    s.startListening();
    s.onPartial('hello');
    expect(s.cancel(), 'hello');
    expect(s.isActive, isFalse);
  });

  test('error recovery', () {
    final s = VoiceSession();
    s.startListening();
    s.fail('network');
    expect(s.state, VoiceState.error);
    s.recover();
    expect(s.state, VoiceState.idle);
    s.startListening();
    expect(s.state, VoiceState.listening);
  });

  test('empty final inserts nothing', () {
    final s = VoiceSession();
    s.startListening();
    expect(s.onFinal(''), isNull);
  });

  test('permission and destroy reset', () {
    final s = VoiceSession();
    s.requestPermission();
    expect(s.state, VoiceState.requestingPermission);
    s.reset();
    expect(s.state, VoiceState.idle);
    s.startListening();
    s.onPartial('x');
    s.reset();
    expect(s.partial, isEmpty);
  });

  test('partial ignored after idle', () {
    final s = VoiceSession();
    s.onPartial('ghost');
    expect(s.partial, isEmpty);
  });
}
