package com.example.composedemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private const val MIN_BOARD_SIZE = 4
private const val MAX_BOARD_SIZE = 15
private const val MIN_WIN_CONDITION = 4
private const val MAX_WIN_CONDITION = 10

private enum class Player(val label: String, val color: Color) {
    Red("Red", Color(0xFFE4572E)),
    Gold("Gold", Color(0xFFF3A712));

    fun next(): Player = if (this == Red) Gold else Red
}

private enum class GameStatus {
    InProgress,
    Draw,
    RedWon,
    GoldWon
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var rows by remember { mutableIntStateOf(6) }
        var columns by remember { mutableIntStateOf(7) }
        var connectTarget by remember { mutableIntStateOf(4) }

        var board by remember { mutableStateOf(createBoard(rows, columns)) }
        var currentPlayer by remember { mutableStateOf(Player.Red) }
        var gameStatus by remember { mutableStateOf(GameStatus.InProgress) }

        fun applySettings(newRows: Int = rows, newColumns: Int = columns, newConnectTarget: Int = connectTarget) {
            rows = newRows
            columns = newColumns
            connectTarget = newConnectTarget.coerceAtMost(min(newRows, newColumns))
            board = createBoard(rows, columns)
            currentPlayer = Player.Red
            gameStatus = GameStatus.InProgress
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF6F1E7),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .widthIn(max = 980.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Connect Four",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF12355B),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Local two-player mode with configurable board size and win condition.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF3A506B),
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ResponsiveGameLayout(
                            rows = rows,
                            columns = columns,
                            connectTarget = connectTarget,
                            board = board,
                            currentPlayer = currentPlayer,
                            gameStatus = gameStatus,
                            onRowsChange = { applySettings(newRows = it) },
                            onColumnsChange = { applySettings(newColumns = it) },
                            onConnectTargetChange = { applySettings(newConnectTarget = it) },
                            onReset = { applySettings() },
                            onColumnClick = { columnIndex ->
                                if (gameStatus != GameStatus.InProgress) return@ResponsiveGameLayout
                                val updatedBoard = dropPiece(board, columnIndex, currentPlayer) ?: return@ResponsiveGameLayout
                                board = updatedBoard
                                gameStatus = resolveGameStatus(updatedBoard, currentPlayer, connectTarget)
                                if (gameStatus == GameStatus.InProgress) {
                                    currentPlayer = currentPlayer.next()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponsiveGameLayout(
    rows: Int,
    columns: Int,
    connectTarget: Int,
    board: List<List<Player?>>,
    currentPlayer: Player,
    gameStatus: GameStatus,
    onRowsChange: (Int) -> Unit,
    onColumnsChange: (Int) -> Unit,
    onConnectTargetChange: (Int) -> Unit,
    onReset: () -> Unit,
    onColumnClick: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactLayout = maxWidth < 760.dp
        if (compactLayout) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ControlPanel(
                    rows = rows,
                    columns = columns,
                    connectTarget = connectTarget,
                    currentPlayer = currentPlayer,
                    gameStatus = gameStatus,
                    onRowsChange = onRowsChange,
                    onColumnsChange = onColumnsChange,
                    onConnectTargetChange = onConnectTargetChange,
                    onReset = onReset,
                )
                BoardCard(
                    board = board,
                    columns = columns,
                    gameStatus = gameStatus,
                    onColumnClick = onColumnClick,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(0.92f)) {
                    ControlPanel(
                        rows = rows,
                        columns = columns,
                        connectTarget = connectTarget,
                        currentPlayer = currentPlayer,
                        gameStatus = gameStatus,
                        onRowsChange = onRowsChange,
                        onColumnsChange = onColumnsChange,
                        onConnectTargetChange = onConnectTargetChange,
                        onReset = onReset,
                    )
                }
                Column(modifier = Modifier.weight(1.25f)) {
                    BoardCard(
                        board = board,
                        columns = columns,
                        gameStatus = gameStatus,
                        onColumnClick = onColumnClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlPanel(
    rows: Int,
    columns: Int,
    connectTarget: Int,
    currentPlayer: Player,
    gameStatus: GameStatus,
    onRowsChange: (Int) -> Unit,
    onColumnsChange: (Int) -> Unit,
    onConnectTargetChange: (Int) -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFCF5))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Game Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF12355B),
            )
            CounterRow(
                label = "Rows",
                value = rows,
                minValue = MIN_BOARD_SIZE,
                maxValue = MAX_BOARD_SIZE,
                onValueChange = onRowsChange,
            )
            CounterRow(
                label = "Columns",
                value = columns,
                minValue = MIN_BOARD_SIZE,
                maxValue = MAX_BOARD_SIZE,
                onValueChange = onColumnsChange,
            )
            CounterRow(
                label = "Connect",
                value = connectTarget,
                minValue = MIN_WIN_CONDITION,
                maxValue = min(MAX_WIN_CONDITION, min(rows, columns)),
                onValueChange = onConnectTargetChange,
            )
            StatusBanner(
                currentPlayer = currentPlayer,
                gameStatus = gameStatus,
            )
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start New Game")
            }
        }
    }
}

@Composable
private fun CounterRow(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF264653),
        )
        OutlinedButton(
            onClick = { onValueChange(max(minValue, value - 1)) },
            enabled = value > minValue,
        ) {
            Text("-")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF12355B),
        )
        OutlinedButton(
            onClick = { onValueChange(min(maxValue, value + 1)) },
            enabled = value < maxValue,
        ) {
            Text("+")
        }
    }
}

@Composable
private fun StatusBanner(
    currentPlayer: Player,
    gameStatus: GameStatus,
) {
    val (message, color) = when (gameStatus) {
        GameStatus.InProgress -> "${currentPlayer.label}'s turn" to currentPlayer.color
        GameStatus.Draw -> "Draw game" to Color(0xFF6C757D)
        GameStatus.RedWon -> "Red wins" to Player.Red.color
        GameStatus.GoldWon -> "Gold wins" to Player.Gold.color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                .clip(CircleShape)
                .background(color)
                .aspectRatio(1f),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF12355B),
        )
    }
}

@Composable
private fun BoardCard(
    board: List<List<Player?>>,
    columns: Int,
    gameStatus: GameStatus,
    onColumnClick: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B4F8C))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (gameStatus == GameStatus.InProgress) {
                    "Tap or click a column to drop a piece."
                } else {
                    "Start a new game or change the configuration."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF145DA0))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(board.size) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(columns) { columnIndex ->
                            val slot = board[rowIndex][columnIndex]
                            val playable = gameStatus == GameStatus.InProgress
                            BoardCell(
                                player = slot,
                                enabled = playable,
                                onClick = { onColumnClick(columnIndex) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BoardCell(
    player: Player?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF083B66))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .clip(CircleShape)
                .background(player?.color ?: Color(0xFFF4F1DE)),
        )
    }
}

private fun createBoard(rows: Int, columns: Int): List<List<Player?>> =
    List(rows) { List(columns) { null } }

private fun dropPiece(
    board: List<List<Player?>>,
    columnIndex: Int,
    player: Player,
): List<List<Player?>>? {
    for (rowIndex in board.lastIndex downTo 0) {
        if (board[rowIndex][columnIndex] == null) {
            return board.mapIndexed { currentRowIndex, row ->
                if (currentRowIndex == rowIndex) {
                    row.mapIndexed { currentColumnIndex, cell ->
                        if (currentColumnIndex == columnIndex) player else cell
                    }
                } else {
                    row
                }
            }
        }
    }
    return null
}

private fun resolveGameStatus(
    board: List<List<Player?>>,
    player: Player,
    connectTarget: Int,
): GameStatus {
    if (hasWinningLine(board, player, connectTarget)) {
        return if (player == Player.Red) GameStatus.RedWon else GameStatus.GoldWon
    }
    return if (board.all { row -> row.all { it != null } }) GameStatus.Draw else GameStatus.InProgress
}

private fun hasWinningLine(
    board: List<List<Player?>>,
    player: Player,
    connectTarget: Int,
): Boolean {
    val directions = listOf(
        0 to 1,
        1 to 0,
        1 to 1,
        1 to -1,
    )

    for (row in board.indices) {
        for (column in board[row].indices) {
            if (board[row][column] != player) continue
            for ((rowStep, columnStep) in directions) {
                var streak = 1
                while (streak < connectTarget) {
                    val nextRow = row + rowStep * streak
                    val nextColumn = column + columnStep * streak
                    if (nextRow !in board.indices || nextColumn !in board[row].indices) break
                    if (board[nextRow][nextColumn] != player) break
                    streak++
                }
                if (streak >= connectTarget) {
                    return true
                }
            }
        }
    }
    return false
}
