package vn.edu.hust.bk2048.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Test hàm chuyển bàn cờ <-> chuỗi (GameStorage ở GĐ 3 sẽ dùng chính hai hàm này)
class GameStorageTest {

    @Test
    fun chuyenThanhChuoiRoiDocLai_raDungBanCo() {
        val board = Board(
            listOf(
                listOf(2, 0, 4, 0),
                listOf(0, 8, 0, 16),
                listOf(32, 0, 64, 0),
                listOf(0, 128, 0, 2048)
            )
        )
        val text = board.toText()
        assertEquals("2,0,4,0,0,8,0,16,32,0,64,0,0,128,0,2048", text)
        assertEquals(board, Board.fromText(text))
    }

    @Test
    fun chuoiHong_traVeNull() {
        assertNull(Board.fromText("abc"))
        assertNull(Board.fromText("2,4,8"))
    }
}
