import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Settings / onboarding host for the IME.
///
/// Architecture (do not invert this):
///   Flutter  →  this app (enable instructions, later themes / Avro options)
///   Kotlin   →  [AvroKeyboardService] InputMethodService
///   Android  →  InputConnection → any app receiving typed text
///
/// Flutter cannot be a system IME by itself. Typed text is committed only
/// from the native InputMethodService. Later steps may render Flutter UI
/// *inside* that service; they must still call InputConnection on the
/// Kotlin side.
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
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1565C0),
          brightness: Brightness.light,
        ),
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
        children: [
          const Text(
            'Step 1 foundation',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          const Text(
            'This app registers a real Android system keyboard '
            '(InputMethodService). Enable it, then select it as the '
            'current keyboard. The on-screen keys are a minimal QWERTY '
            'stub — Bengali and Avro come in later steps.',
          ),
          const SizedBox(height: 24),
          FilledButton.icon(
            onPressed: _openSettings,
            icon: const Icon(Icons.keyboard),
            label: const Text('Enable keyboard in system settings'),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: _openPicker,
            icon: const Icon(Icons.swap_horiz),
            label: const Text('Choose current keyboard'),
          ),
          const SizedBox(height: 24),
          const Text(
            'On most devices:\n'
            'Settings → System → Keyboard → On-screen keyboard\n'
            'Turn on “Avro Keyboard”, then pick it from the keyboard switcher.',
          ),
        ],
      ),
    );
  }
}
