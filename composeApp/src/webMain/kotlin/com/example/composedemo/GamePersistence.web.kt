package com.example.composedemo

import kotlinx.browser.window

private const val GAME_STATE_KEY = "connect-four-game-state"

actual fun loadPersistedGameState(): PersistedGameState? {
    val rawState = window.localStorage.getItem(GAME_STATE_KEY) ?: return null
    val parts = rawState.split("|")
    if (parts.size != 6) return null

    val rows = parts[0].toIntOrNull() ?: return null
    val columns = parts[1].toIntOrNull() ?: return null
    val connectTarget = parts[2].toIntOrNull() ?: return null
    val currentPlayer = parts[3].toPlayerOrNull() ?: return null
    val gameStatus = parts[4].toGameStatusOrNull() ?: return null
    val board = decodeBoard(parts[5], rows, columns) ?: return null

    if (rows !in MIN_BOARD_SIZE..MAX_BOARD_SIZE) return null
    if (columns !in MIN_BOARD_SIZE..MAX_BOARD_SIZE) return null
    if (connectTarget !in MIN_WIN_CONDITION..MAX_WIN_CONDITION) return null
    if (connectTarget > minOf(rows, columns)) return null

    return PersistedGameState(
        rows = rows,
        columns = columns,
        connectTarget = connectTarget,
        board = board,
        currentPlayer = currentPlayer,
        gameStatus = gameStatus,
    )
}

actual fun savePersistedGameState(state: PersistedGameState) {
    val encodedBoard = state.board.joinToString("/") { row ->
        row.joinToString("") { player ->
            when (player) {
                Player.Red -> "R"
                Player.Gold -> "G"
                null -> "."
            }
        }
    }
    val encodedState = listOf(
        state.rows.toString(),
        state.columns.toString(),
        state.connectTarget.toString(),
        state.currentPlayer.name,
        state.gameStatus.name,
        encodedBoard,
    ).joinToString("|")

    window.localStorage.setItem(GAME_STATE_KEY, encodedState)
}

actual fun clearPersistedGameState() {
    window.localStorage.removeItem(GAME_STATE_KEY)
}

private fun String.toPlayerOrNull(): Player? =
    Player.entries.firstOrNull { it.name == this }

private fun String.toGameStatusOrNull(): GameStatus? =
    GameStatus.entries.firstOrNull { it.name == this }

private fun decodeBoard(
    encodedBoard: String,
    rows: Int,
    columns: Int,
): List<List<Player?>>? {
    val encodedRows = encodedBoard.split("/")
    if (encodedRows.size != rows) return null

    return encodedRows.map { encodedRow ->
        if (encodedRow.length != columns) return null
        encodedRow.map { token ->
            when (token) {
                'R' -> Player.Red
                'G' -> Player.Gold
                '.' -> null
                else -> return null
            }
        }
    }
}
