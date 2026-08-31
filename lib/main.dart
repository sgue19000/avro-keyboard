import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const AvroKeyboardApp());
}

class AvroKeyboardApp extends StatelessWidget {
  const AvroKeyboardApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Avro Keyboard',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF1565C0)),
        useMaterial3: true,
      ),
      home: const SetupPage(),
    );
  }
}

class SetupPage extends StatelessWidget {
  const SetupPage({super.key});

  static const _ime = MethodChannel('com.avrokeyboard.app/ime');

  Future<void> _openSettings() async {
    await _ime.invokeMethod<void>('openInputMethodSettings');
  }

  Future<void> _openPicker() async {
    await _ime.invokeMethod<void>('openInputMethodPicker');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Avro Keyboard')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: const [
          Text('Step 3 — বাংলা, English, অভ্র',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w600)),
          SizedBox(height: 8),
          Text(
            'Enable the IME, then cycle the mode key on the keyboard: '
            'বাংলা (direct), English, and অভ্র (Banglish). '
            'In অভ্র mode type ami for আমি. Prefix ` to keep English.',
          ),
          SizedBox(height: 24),
        ],
      ),
    );
  }
}
