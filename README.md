# Studs

Studs is a modern, lightweight Android application designed for organizing and reading study materials. It serves as a dedicated PDF viewer that integrates directly with a GitHub-hosted repository to provide a curated collection of hosts and documents.

## Core Features

- Native PDF Viewing: Powered by the official AndroidX PDF library for smooth scrolling, high-performance rendering, and tablet-optimized viewing.
- GitHub Integration: Dynamically fetches and searches for documents hosted in a specific GitHub repository.
- Material 3 Design: A clean, modern interface featuring dynamic theming and a personalized user experience.
- Study Management: Track your recently read documents and bookmark important files for quick access.
- Dark Mode Support: Includes several custom color schemes like Dracula and Mocha to reduce eye strain during long study sessions.
## Screenshots
## Screenshots

<table>
  <tr>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-150204_studs" src="https://github.com/user-attachments/assets/cd365145-5837-464f-8b83-11a2556bfec9" />
    </td>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-150209_studs" src="https://github.com/user-attachments/assets/ae434889-849e-4360-a69a-812acc8bf1c2" />
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-144755_studs" src="https://github.com/user-attachments/assets/11c19aac-904b-4463-a90a-f1c8d5b9311e" />
    </td>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-144727_studs" src="https://github.com/user-attachments/assets/a8ca472f-3c2a-4879-af79-ddad6813ba3e" />
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-144733_studs" src="https://github.com/user-attachments/assets/caade14a-bdd5-4656-9c9d-083d25d66edf" />
    </td>
    <td align="center" width="50%">
      <img width="100%" alt="Screenshot_20260516-144745_studs" src="https://github.com/user-attachments/assets/1d6bb41e-cf49-4d0e-aec3-1ef309df84d5" />
    </td>
  </tr>
</table>

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
