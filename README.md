# ComposeDemo

`ComposeDemo` is a Kotlin Multiplatform project built with Compose Multiplatform. The current app is a browser-playable Connect Four implementation with:

- configurable board size
- configurable win condition
- local two-player play
- responsive desktop/mobile layout
- persisted game state across browser refreshes

The shared UI and game logic live in [composeApp/src/commonMain/kotlin/com/example/composedemo](./composeApp/src/commonMain/kotlin/com/example/composedemo).

## Tech Stack

- Kotlin `2.3.20`
- Compose Multiplatform `1.10.3`
- Material 3
- Web targets: Kotlin/JS and Kotlin/Wasm

## Run In IntelliJ IDEA

Open the project in IntelliJ IDEA and use the Gradle tool window or the run configuration support built into the IDE.

For the web version, run:

- `composeApp > Tasks > kotlin browser > wasmJsBrowserDevelopmentRun`

Use `jsBrowserDevelopmentRun` only if you specifically need the JS target instead of Wasm.