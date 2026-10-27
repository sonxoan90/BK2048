package vn.edu.hust.bk2048.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveLogicTest {

    @Test
    fun hangTrong_khongDoi() {
        val r = mergeRow(listOf(0, 0, 0, 0))
        assertEquals(listOf(0, 0, 0, 0), r.row)
        assertFalse(r.moved)
        assertEquals(0, r.score)
    }

    @Test
    fun haiO2CachNhau_gopThanh4() {
        val r = mergeRow(listOf(2, 0, 2, 0))
        assertEquals(listOf(4, 0, 0, 0), r.row)
        assertEquals(4, r.score)
        assertTrue(r.moved)
    }

    @Test
    fun bonO2_raHaiO4_khongRa8() {
        val r = mergeRow(listOf(2, 2, 2, 2))
        assertEquals(listOf(4, 4, 0, 0), r.row)
        assertEquals(8, r.score)
    }

    @Test
    fun hai_hai_bon_khongGopTiepThanh8() {
        val r = mergeRow(listOf(2, 2, 4, 0))
        assertEquals(listOf(4, 4, 0, 0), r.row)
    }

    @Test
    fun bon_bon_tam_tam() {
        val r = mergeRow(listOf(4, 4, 8, 8))
        assertEquals(listOf(8, 16, 0, 0), r.row)
        assertEquals(24, r.score)
    }

    @Test
    fun hangKhongGopDuoc_khongDoi() {
        val r = mergeRow(listOf(2, 4, 8, 16))
        assertEquals(listOf(2, 4, 8, 16), r.row)
        assertFalse(r.moved)
    }

    // Bàn cờ mẫu dùng chung cho 4 hướng
    private val sample = Board(
        listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(4, 0, 4, 0),
            listOf(0, 2, 0, 2)
        )
    )

    @Test
    fun vuotTrai() {
        val r = move(sample, Direction.LEFT)
        val expected = Board(
            listOf(
                listOf(4, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(8, 0, 0, 0),
                listOf(4, 0, 0, 0)
            )
        )
        assertEquals(expected, r.board)
        assertEquals(16, r.score)
        assertTrue(r.moved)
    }

    @Test
    fun vuotPhai() {
        val r = move(sample, Direction.RIGHT)
        val expected = Board(
            listOf(
                listOf(0, 0, 0, 4),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 8),
                listOf(0, 0, 0, 4)
            )
        )
        assertEquals(expected, r.board)
    }

    @Test
    fun vuotLen() {
        val r = move(sample, Direction.UP)
        val expected = Board(
            listOf(
                listOf(2, 4, 4, 2),
                listOf(4, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        assertEquals(expected, r.board)
    }

    @Test
    fun vuotXuong() {
        val r = move(sample, Direction.DOWN)
        val expected = Board(
            listOf(
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(2, 0, 0, 0),
                listOf(4, 4, 4, 2)
            )
        )
        assertEquals(expected, r.board)
    }

    // Vị trí ô vừa gộp (dùng cho animation) phải đúng ở cả 4 hướng
    @Test
    fun oVuaGop_dungViTriCa4Huong() {
        assertEquals(setOf(0 to 0, 2 to 0, 3 to 0), move(sample, Direction.LEFT).mergedCells.toSet())
        assertEquals(setOf(0 to 3, 2 to 3, 3 to 3), move(sample, Direction.RIGHT).mergedCells.toSet())
        assertEquals(setOf(0 to 1), move(sample, Direction.UP).mergedCells.toSet())
        assertEquals(setOf(3 to 1), move(sample, Direction.DOWN).mergedCells.toSet())
    }

    @Test
    fun mergeRow_ghiViTriO_gop() {
        assertEquals(listOf(0, 1), mergeRow(listOf(2, 2, 2, 2)).mergedAt)
        assertEquals(listOf(0), mergeRow(listOf(2, 2, 4, 0)).mergedAt)
    }

    @Test
    fun banCoKhongDoi_moved_false() {
        val full = Board(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )
        Direction.values().forEach { assertFalse(move(full, it).moved) }
    }
}
