# O2 Scratch Card

Android application that models a digital scratch card with three states: **Unscratched**, **Scratched**, and **Activated**. Built with Jetpack Compose, Kotlin Coroutines, and Clean Architecture.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3) |
| Concurrency | Kotlin Coroutines + StateFlow |
| Networking | Retrofit 2 + OkHttp 4 |
| DI | Hilt |
| Testing | JUnit 4, MockK, Turbine, Coroutines Test |
| Min SDK | 26 |
| Target SDK | 35 |

## Architecture

The project follows **Clean Architecture** with strict layer separation. Dependencies point inward — the domain layer has zero framework dependencies.

```
┌─────────────────────────────────────────────────────┐
│                  PRESENTATION                        │
│  ViewModels (Hilt-injected)                          │
│  Compose Screens (stateless, observe StateFlows)     │
│  Navigation (Compose Navigation)                     │
│                                                      │
│  Depends on: Domain                                  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                    DOMAIN                            │
│  ScratchCardState (sealed class — state machine)     │
│  ScratchCardRepository (interface — state holder)    │
│  ScratchDataSource (interface — local scratch op)    │
│  ActivationDataSource (interface — remote API call)  │
│  DispatcherProvider (interface)                       │
│  ScratchCardUseCase / ActivateCardUseCase            │
│                                                      │
│  Depends on: Nothing (pure Kotlin)                   │
└──────────────────────▲──────────────────────────────┘
                       │ implements
┌──────────────────────┴──────────────────────────────┐
│                     DATA                             │
│  O2Api (Retrofit)                                    │
│  VersionResponse (DTO)                               │
│  ScratchCardRepositoryImpl (Singleton state machine) │
│  ScratchDataSourceImpl (delay + UUID generation)     │
│  ActivationDataSourceImpl (Retrofit API call)        │
│  DefaultDispatcherProvider                           │
│                                                      │
│  Depends on: Domain interfaces, Retrofit, OkHttp     │
└─────────────────────────────────────────────────────┘
```

## State Machine

```
┌──────────────┐   scratch()    ┌──────────────┐   activate()   ┌──────────────┐
│  Unscratched │ ─────────────> │   Scratched  │ ─────────────> │  Activated   │
│              │   (2s delay    │  (code: UUID) │   (API call    │  (code: UUID) │
│              │    + UUID gen) │              │    + validate) │  [terminal]   │
└──────────────┘                └──────────────┘                └──────────────┘
```

**Transition rules** are enforced in `ScratchCardRepositoryImpl.validateTransition()`. Invalid transitions throw `IllegalStateException`.

## Screens

### Main Screen
- Displays the current scratch card state (Unscratched / Scratched / Activated)
- Shows the revealed code when scratched or activated
- Navigation buttons to Scratch and Activation screens
- Buttons are disabled based on state (e.g., "Go to Activation" disabled when unscratched)

### Scratch Screen
- "Scratch" button triggers a 2-second simulated heavy operation
- Shows a `CircularProgressIndicator` during scratching
- **Cancellation**: Uses `viewModelScope` — if the user presses back before 2 seconds, the operation is cancelled and the state remains `Unscratched`

### Activation Screen
- "Activate" button sends the revealed UUID to the API
- Shows a `CircularProgressIndicator` during the API call
- **Non-cancellation**: Uses `@ApplicationScope` coroutine — if the user presses back during the API call, the operation completes in the background
- Shows an `AlertDialog` on failure with a user-friendly error message

## API Contract

| Field | Value |
|---|---|
| Endpoint | `GET https://api.o2.sk/version` |
| Query param | `code` (the generated UUID) |
| Auth | None |
| Response | `{ "android": "287028" }` |

**Validation**: If `android` parsed as int > 277028, the card is activated. Otherwise, an error dialog is shown.

## Key Design Decisions

### Cancellation vs Non-Cancellation

| Operation | Coroutine Scope | Reason |
|---|---|---|
| Scratch | `viewModelScope` | Must cancel on back navigation. `delay()` is cooperative with cancellation. |
| Activation | `@ApplicationScope` (Hilt-provided) | Must NOT cancel on back navigation. The app-scope coroutine survives ViewModel destruction. |

### Separated Data Sources

Data operations are split into dedicated interfaces following the Single Responsibility Principle:

| Interface | Responsibility | Implementation |
|---|---|---|
| `ScratchCardRepository` | State machine (state + transitions + isActivating flag) | `ScratchCardRepositoryImpl` (`@Singleton`) |
| `ScratchDataSource` | Local scratch operation (2s delay + UUID) | `ScratchDataSourceImpl` (stateless) |
| `ActivationDataSource` | Remote API call to O2 | `ActivationDataSourceImpl` (stateless) |

Each use case injects only the interfaces it needs — `ScratchCardUseCase` takes `ScratchCardRepository` + `ScratchDataSource`, while `ActivateCardUseCase` takes `ScratchCardRepository` + `ActivationDataSource`. No component has access to capabilities it doesn't use.

### Singleton Repository as State Holder

The `ScratchCardRepository` is `@Singleton`-scoped. It holds a `MutableStateFlow<ScratchCardState>` that serves as the single source of truth across all screens. This means:

- When activation completes in the background, the repository state updates to `Activated`.
- When the user returns to `MainScreen`, the updated state is immediately visible.

### DispatcherProvider for Testability

All coroutine dispatchers are injected via a `DispatcherProvider` interface. This allows tests to use `TestDispatcher` for deterministic, time-controlled testing without `Dispatchers.setMain()` hacks in the data layer.

### Typed Error Hierarchy

`ActivationError` is a sealed class in the domain layer. This allows the presentation layer to map errors to user-friendly messages without inspecting exception messages or types from third-party libraries.

### Loading State for Background Operations

The `isActivating` flag lives in the `@Singleton` repository, not in the ViewModel. This ensures that if the user navigates away from `ActivationScreen` and returns (creating a new ViewModel), the loading indicator correctly reflects the ongoing background operation.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run specific test class
./gradlew testDebugUnitTest --tests "sk.o2.scratchcard.domain.usecase.ActivateCardUseCaseTest"
```

## Project Structure

```
app/src/main/java/sk/o2/scratchcard/
├── ScratchCardApp.kt                          # @HiltAndroidApp
├── MainActivity.kt                            # @AndroidEntryPoint, Compose entry
├── data/
│   ├── api/O2Api.kt                           # Retrofit interface
│   ├── dispatcher/DefaultDispatcherProvider.kt # Production dispatchers
│   ├── model/VersionResponse.kt               # API response DTO
│   └── repository/
│       ├── ActivationDataSourceImpl.kt        # Remote API call
│       ├── ScratchCardRepositoryImpl.kt       # Singleton state machine
│       └── ScratchDataSourceImpl.kt           # Local scratch (delay + UUID)
├── di/
│   ├── AppModule.kt                           # OkHttp, Retrofit, API, AppScope
│   ├── DispatcherModule.kt                    # Binds DispatcherProvider
│   ├── Qualifiers.kt                          # @ApplicationScope
│   └── RepositoryModule.kt                    # Binds Repository + DataSources
├── domain/
│   ├── dispatcher/DispatcherProvider.kt        # Interface for testability
│   ├── model/
│   │   ├── ActivationError.kt                # Typed error hierarchy
│   │   └── ScratchCardState.kt               # Sealed class: 3 states
│   ├── repository/
│   │   ├── ActivationDataSource.kt            # Interface — remote API call
│   │   ├── ScratchCardRepository.kt           # Interface — state machine
│   │   └── ScratchDataSource.kt               # Interface — local scratch op
│   └── usecase/
│       ├── ActivateCardUseCase.kt             # Validation logic (> 277028)
│       └── ScratchCardUseCase.kt              # Scratch orchestration
└── presentation/
    ├── activation/
    │   ├── ActivationScreen.kt                # Compose UI
    │   └── ActivationViewModel.kt             # AppScope coroutine
    ├── main/
    │   ├── MainScreen.kt                      # Compose UI
    │   └── MainViewModel.kt                   # State observer
    ├── navigation/
    │   ├── NavGraph.kt                        # NavHost with 3 routes
    │   └── Routes.kt                          # Route constants
    └── scratch/
        ├── ScratchScreen.kt                   # Compose UI
        └── ScratchViewModel.kt                # viewModelScope coroutine

app/src/test/java/sk/o2/scratchcard/
├── data/repository/
│   ├── ActivationDataSourceImplTest.kt
│   ├── ScratchCardRepositoryImplTest.kt
│   └── ScratchDataSourceImplTest.kt
├── domain/usecase/
│   ├── ActivateCardUseCaseTest.kt
│   └── ScratchCardUseCaseTest.kt
└── presentation/
    ├── activation/ActivationViewModelTest.kt
    └── scratch/ScratchViewModelTest.kt
```

## Test Coverage

| Test Class | Focus |
|---|---|
| `ScratchCardUseCaseTest` | State validation, delegation to data source, error handling |
| `ActivateCardUseCaseTest` | Threshold logic (>, ==, <), parse errors, network errors, state guards |
| `ScratchViewModelTest` | Loading state, idempotency, cooperative cancellation |
| `ActivationViewModelTest` | Loading state, non-cancellation in appScope, error mapping, dismiss |
| `ScratchCardRepositoryImplTest` | Initial state, valid/invalid state transitions, isActivating flag |
| `ScratchDataSourceImplTest` | UUID generation, 2-second delay timing |
| `ActivationDataSourceImplTest` | API delegation, network error propagation |
