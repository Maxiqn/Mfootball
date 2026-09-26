# Mfootball

Mfootball is an Android football companion app for browsing competitions, fixtures, standings, and match details using the Football Data API.

## Version 1.4

This release improves the app foundation and user experience:

- refreshed match and match-detail screens with clearer hierarchy
- fixed the refresh container layout so empty states and fixture lists render reliably
- added accessible labels and centralized user-facing strings
- safer match-detail rendering when API data is incomplete
- improved spacing, scrolling, and touch targets
- updated the Android version metadata to 1.4 / version code 140

## Setup

Provide a Football Data API token through either Gradle or the environment:

```bash
export FOOTBALL_DATA_API_KEY=your_token
./gradlew assembleDebug
```

You can also pass `-PfootballDataApiKey=your_token` to Gradle. Do not commit API keys to the repository.

## Download APK

The latest debug APK is available here when published:
- [downloads/app-debug.apk](downloads/app-debug.apk)
