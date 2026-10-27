package vn.edu.hust.bk2048.domain

import kotlin.random.Random

const val WIN_TILE = 2048

// [KT Ch3.1] lớp có constructor và property. Truyền Random vào để test dùng seed cố định
class Game(
    private val size: Int = 4,
    private val random: Random = Random.Default
) {
    var board: Board = Board.empty(size)
        private set
    var score: Int = 0
        private set
    var state: GameState = GameState.Playing
        private set

    // Đã báo thắng rồi thì không báo lại (chỉ báo 1 lần mỗi ván)
    var hasWon: Boolean = false
        private set

    // Trạng thái trước lượt đi gần nhất, dùng cho Undo (chỉ nhớ 1 bước)
    private var previous: Snapshot? = null

    private data class Snapshot(
        val board: Board,
        val score: Int,
        val state: GameState,
        val hasWon: Boolean
    )

    init {
        newGame()
    }

    val canUndo: Boolean get() = previous != null

    // [KT Ch2.2] tham số mặc định: newGame() dùng cỡ bàn cờ hiện tại
    fun newGame(size: Int = board.size) {
        board = Board.empty(size)
        score = 0
        state = GameState.Playing
        hasWon = false
        previous = null
        board = spawnTile(board)
        board = spawnTile(board)
    }

    // Khôi phục ván đã lưu (hoặc dựng bàn cờ có sẵn cho chế độ demo)
    fun load(board: Board, score: Int = 0, hasWon: Boolean = false) {
        this.board = board
        this.score = score
        this.hasWon = hasWon
        previous = null
        state = computeState()
    }

    // Trả về true nếu lượt vuốt làm thay đổi bàn cờ
    fun move(direction: Direction): Boolean {
        if (state is GameState.Lost) return false
        val result = move(board, direction)
        if (!result.moved) return false // không đổi gì thì không sinh ô mới

        previous = Snapshot(board, score, state, hasWon)
        board = spawnTile(result.board)
        score += result.score
        state = computeState()
        return true
    }

    // Sau khi thắng, người chơi chọn "Chơi tiếp"
    fun continueAfterWin() {
        if (state is GameState.Won) state = GameState.Playing
    }

    // Quay lại 1 bước; trả về false nếu không có bước nào để quay lại
    fun undo(): Boolean {
        val snapshot = previous ?: return false
        board = snapshot.board
        score = snapshot.score
        state = snapshot.state
        hasWon = snapshot.hasWon
        previous = null
        return true
    }

    private fun computeState(): GameState {
        if (!hasWon && board.maxTile() >= WIN_TILE) {
            hasWon = true
            return GameState.Won
        }
        return if (board.hasMoves()) GameState.Playing else GameState.Lost
    }

    // Sinh 1 ô ở vị trí trống ngẫu nhiên: 90% là 2, 10% là 4
    // [KT Ch2.2] tham số mặc định lấy từ property random của lớp
    private fun spawnTile(board: Board, random: Random = this.random): Board {
        val empties = board.emptyCells()
        if (empties.isEmpty()) return board
        val (r, c) = empties[random.nextInt(empties.size)]
        val value = if (random.nextInt(10) == 0) 4 else 2
        // Tạo bàn cờ mới (bất biến) với đúng 1 ô thay đổi
        return Board(board.cells.mapIndexed { row, cells ->
            cells.mapIndexed { col, old -> if (row == r && col == c) value else old }
        })
    }
}
