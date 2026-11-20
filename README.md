# Lofi 1.0.0

Premium Material 3 Android music player foundation with real:
- Saavn-compatible online search
- Local Android music scanning
- Android SAF audio picker
- Media3 playback
- Background MediaSession service
- Search / Home / Library navigation
- GitHub Actions APK build

## Build

```bash
chmod +x gradlew
./gradlew assembleRelease
```

The release APK is produced at:

`app/build/outputs/apk/release/app-release.apk`

## GitHub Actions

Push the repository to GitHub. The workflow in `.github/workflows/android.yml` builds debug and release APKs and uploads them as workflow artifacts.

## Notes

The online provider is isolated in `SaavnRepository` so the API implementation can be replaced without changing the UI.
