package com.example.composedemo

actual fun loadPersistedGameState(): PersistedGameState? = null

actual fun savePersistedGameState(state: PersistedGameState) = Unit

actual fun clearPersistedGameState() = Unit
