# CoC TH10 Prototype (Android)

This repository contains an **experimental tap-sequence runner** and TH10 upgrade checklist, **not** an autonomous Clash of Clans bot.

## Build without a PC
1. Open the **Actions** tab on GitHub.
2. Select **Build Android APK**, then **Run workflow** (or wait for the push build).
3. Open the successful run and download **CoC-TH10-debug-APK** under Artifacts.
4. Extract the downloaded ZIP on Android, then install `app-debug.apk`.

## Use
Enable the application's accessibility service in Android settings, enter screen coordinates as one `x,y` pair per line, choose a finite cycle count, and tap Start. Switch to Clash of Clans within five seconds. Tap actions stop when the game is no longer foreground. Screen dimensions and game layout vary by device.

## Limitations and risks
No screen recognition, autonomous attack targeting, resource detection, or automatic building decisions are implemented. This prototype may malfunction or trigger unwanted taps. Game automation may violate Supercell's terms and risk account sanctions. Only grant Accessibility permission if you trust the code.
