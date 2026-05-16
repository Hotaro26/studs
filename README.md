# Studs

Studs is a modern, lightweight Android application designed for organizing and reading study materials. It serves as a dedicated PDF viewer that integrates directly with a GitHub-hosted repository to provide a curated collection of hosts and documents.

## Core Features

- Native PDF Viewing: Powered by the official AndroidX PDF library for smooth scrolling, high-performance rendering, and tablet-optimized viewing.
- GitHub Integration: Dynamically fetches and searches for documents hosted in a specific GitHub repository.
- Material 3 Design: A clean, modern interface featuring dynamic theming and a personalized user experience.
- Study Management: Track your recently read documents and bookmark important files for quick access.
- Dark Mode Support: Includes several custom color schemes like Dracula and Mocha to reduce eye strain during long study sessions.

## Technical Details

The application is built using a modern Android stack:
- Language: 100% Kotlin
- UI Framework: Jetpack Compose
- Architecture: MVVM with a clean separation of data and UI layers
- Database: Room for local persistence of history and bookmarks
- Networking: Retrofit and OkHttp for API communication

## Installation

You can find the latest signed release APK in the build outputs directory.

## Development

To build the project locally, clone the repository and use the provided Gradle wrapper:

```bash
./gradlew assembleDebug
```

The project requires a minimum SDK level of 31 and targets SDK 36.
