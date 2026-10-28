package vn.edu.hust.bk2048.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Chỉ test hàm tính cao độ (thuần Kotlin). Việc phát âm cần SoundPool nên thử trên emulator.
class SoundManagerTest {

    private val delta = 0.0001f

    @Test
    fun o4_laCaoDoChuan() {
        assertEquals(1.0f, SoundManager.rateFor(4), delta)
    }

    @Test
    fun moiLanGapDoi_tang01() {
        assertEquals(1.1f, SoundManager.rateFor(8), delta)
        assertEquals(1.2f, SoundManager.rateFor(16), delta)
        assertEquals(1.9f, SoundManager.rateFor(2048), delta)
    }

    @Test
    fun oLonHon_thiCaoHonHoacBang() {
        val values = listOf(4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192)
        val rates = values.map { SoundManager.rateFor(it) }
        assertTrue(rates.zipWithNext().all { (a, b) -> b >= a })
    }

    @Test
    fun khongVuotGioiHanSoundPool() {
        // SoundPool chỉ nhận tốc độ từ 0.5 đến 2.0
        assertEquals(2.0f, SoundManager.rateFor(1 shl 20), delta)
        assertTrue(SoundManager.rateFor(2) in 0.5f..2.0f)
    }
}
