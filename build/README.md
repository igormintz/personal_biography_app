# Personal Biography (Android)

A native Android app that records Hebrew voice notes, transcribes them with
OpenAI Whisper, structures the transcript with GPT-4o-mini, and stores
everything locally in Room (SQLite). The UI is a single Telegram-style chat
screen.

This is a self-contained on-device port of the Telegram bot at
`/Users/igor/Documents/personal_biography/` — no backend, no Postgres, no
Vercel. The app calls OpenAI directly using a key stored in
`EncryptedSharedPreferences`.

## Stack

- Kotlin 2.0 + Jetpack Compose (Material3)
- Room (SQLite) for entries + usage events
- Retrofit + OkHttp + kotlinx.serialization for OpenAI
- AndroidX Security (`EncryptedSharedPreferences`) for the OpenAI key
- MediaRecorder for audio capture (m4a / AAC)

## Prerequisites (macOS)

```bash
brew install openjdk@17 kotlin ktlint gradle
brew install --cask android-studio
```

Then in `~/.zshrc`:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

Open Android Studio once, accept SDK licenses, install Android 35 platform
+ a system image, and create a Pixel emulator.

## Build & test

```bash
./gradlew :app:testDebugUnitTest      # JVM unit tests
./gradlew :app:assembleDebug          # build APK
./gradlew ktlintCheck                 # lint/format gate
```

## Layout

```
app/src/main/kotlin/com/personalbiography/
  data/db/        Room entities, DAOs, database
  data/remote/    OpenAI Retrofit client + DTOs
  data/repo/      EntryRepository, UsageRepository
  data/secure/    EncryptedSharedPreferences wrapper
  domain/         Pure business logic (no Android deps): Prompts,
                  Structuring, ShortId, Replies, Commands, Pipeline,
                  AudioRecorder
  ui/chat/        ChatScreen, ChatViewModel, bubbles, input bar
  ui/settings/    SettingsScreen
  ui/theme/       Material3 theme + RTL locale
```
