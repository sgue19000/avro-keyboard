import 'package:avro_keyboard/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('setup page shows enable action', (tester) async {
    await tester.pumpWidget(const AvroKeyboardApp());
    expect(find.text('Avro Keyboard'), findsWidgets);
    expect(find.text('Enable keyboard in system settings'), findsOneWidget);
  });
}
