# Shopping List Module

A modular offline-first Shopping List feature that can be integrated into any Android application.

## Features

- Add, edit, delete shopping items with name, quantity, and optional notes
- Mark items as bought with filter to show/hide bought items
- Search functionality to find items by name or notes
- Sort by date in ascending or descending order
- Offline-first architecture with background synchronization
- Complete self-contained module with a single entry point

## Architecture

This module follows Clean Architecture with MVVM pattern:

- **UI Layer**: Jetpack Compose UI components and ViewModels
- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repository implementation with local and remote data sources

## Technologies Used

- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: For modern declarative UI
- **Room**: Local database storage
- **Hilt**: Dependency injection
- **Coroutines & Flow**: Asynchronous programming
- **WorkManager**: Background synchronization
- **JUnit & Mockito**: Unit testing

## Integration Guide

### Add the Module to Your Project

1. Add the module to your project by including it in settings.gradle:
   ```groovy
   include ':cart'
   ```

2. Add the dependency in your app's build.gradle:
   ```groovy
   implementation project(':cart')
   ```

### Launch the Feature

To launch the Shopping List feature from your app:

```kotlin
import com.amr.cart.ShoppingListBuilder

// From an Activity or Fragment
ShoppingListBuilder.launch(context)

// Or get an intent to launch with custom options
val intent = ShoppingListBuilder.getIntent(context)
startActivity(intent)
```

## Build and Run Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

## Testing

The module includes:

- **Unit tests**: Testing ViewModels, UseCases, and Repository
- **UI tests**: Testing Compose UI components
- **Integration tests**: End-

## PS: Some test cases in view model test are failing and still under investigations
