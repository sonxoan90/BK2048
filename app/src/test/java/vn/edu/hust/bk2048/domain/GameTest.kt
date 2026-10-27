package vn.edu.hust.bk2048.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {

    private fun newGame() = Game(random = Random(1234))

    private fun tileCount(board: Board) = board.cells.flatten().count { it != 0 }

    // Bàn cờ đầy, không còn cặp kề nhau bằng nhau -> thua
    private val deadBoard = Board(
        listOf(
            listOf(2, 4, 2, 4),
            listOf(4, 2, 4, 2),
            listOf(2, 4, 2, 4),
            listOf(4, 2, 4, 2)
        )
    )

    @Test
    fun vanMoi_coDungHaiO() {
        assertEquals(2, tileCount(newGame().board))
    }

    @Test
    fun nuocDiKhongDoi_khongSinhOMoi() {
        val game = newGame()
        game.load(
            Board(
                listOf(
                    listOf(2, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0)
                )
            )
        )
        val moved = game.move(Direction.LEFT) // ô đã nằm sát trái
        assertFalse(moved)
        assertEquals(1, tileCount(game.board))
    }

    @Test
    fun nuocDiCoDoi_sinhDungMotOMoi_oViTriTrong() {
        val game = newGame()
        game.load(
            Board(
                listOf(
                    listOf(0, 0, 0, 2),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0)
                )
            )
        )
        assertTrue(game.move(Direction.LEFT))
        // Ô 2 trượt về (0,0); thêm 1 ô mới ở chỗ khác, giá trị 2 hoặc 4
        assertEquals(2, tileCount(game.board))
        assertEquals(2, game.board.cells[0][0])
        val newTiles = game.board.cells.flatten().filter { it != 0 }
        assertTrue(newTiles.all { it == 2 || it == 4 })
    }

    @Test
    fun nhanRaThang_khiCoO2048() {
        val game = newGame()
        game.load(
            Board(
                listOf(
                    listOf(1024, 1024, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0)
                )
            )
        )
        game.move(Direction.LEFT)
        assertEquals(GameState.Won, game.state)
        assertTrue(game.hasWon)
    }

    @Test
    fun chiBaoThangMotLan() {
        val game = newGame()
        game.load(
            Board(
                listOf(
                    listOf(1024, 1024, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0)
                )
            )
        )
        game.move(Direction.LEFT)
        game.continueAfterWin()
        assertEquals(GameState.Playing, game.state)
        game.move(Direction.DOWN)
        assertEquals(GameState.Playing, game.state) // vẫn còn ô 2048 nhưng không báo thắng lại
    }

    @Test
    fun banCoDayKhongConCapKe_thua() {
        val game = newGame()
        game.load(deadBoard)
        assertEquals(GameState.Lost, game.state)
    }

    @Test
    fun banCoDayNhungConCapKeBangNhau_chuaThua() {
        val board = Board(
            listOf(
                listOf(2, 2, 8, 4),
                listOf(4, 8, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )
        val game = newGame()
        game.load(board)
        assertEquals(GameState.Playing, game.state)
    }

    @Test
    fun undo_triVeDungBanCoVaDiem() {
        val game = newGame()
        game.load(
            Board(
                listOf(
                    listOf(2, 2, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0),
                    listOf(0, 0, 0, 0)
                )
            ),
            score = 10
        )
        val boardBefore = game.board
        assertTrue(game.move(Direction.LEFT))
        assertEquals(14, game.score)

        assertTrue(game.undo())
        assertEquals(boardBefore, game.board)
        assertEquals(10, game.score)
        assertFalse(game.undo()) // chỉ quay lại được 1 bước
    }
}
