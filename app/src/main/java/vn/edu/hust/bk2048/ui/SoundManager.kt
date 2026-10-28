package vn.edu.hust.bk2048.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import vn.edu.hust.bk2048.R

// Phát hiệu ứng âm thanh ngắn bằng SoundPool (có sẵn trong Android, không cần thêm thư viện).
// SoundPool nạp trước các file ngắn vào bộ nhớ nên phát gần như tức thì, hợp với tiếng game.
// [KT Ch2.4] isEnabled là hàm bậc cao () -> Boolean: mỗi lần phát hỏi lại công tắc Âm thanh,
// nên bật/tắt trong Cài đặt có hiệu lực ngay mà SoundManager không cần biết SharedPreferences.
class SoundManager(context: Context, private val isEnabled: () -> Boolean) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // load() chạy bất đồng bộ và trả về id của âm; chưa nạp xong mà phát thì chỉ im lặng
    private val slideId = pool.load(context, R.raw.sfx_slide, 1)
    private val mergeId = pool.load(context, R.raw.sfx_merge, 1)
    private val winId = pool.load(context, R.raw.sfx_win, 1)
    private val loseId = pool.load(context, R.raw.sfx_lose, 1)

    // Tiếng trượt, nhỏ hơn tiếng gộp để không lấn át
    fun playSlide() = play(slideId, volume = 0.5f)

    // Tiếng gộp: ô gộp càng lớn thì cao độ càng cao
    fun playMerge(largestMergedValue: Int) =
        play(mergeId, volume = 1f, rate = rateFor(largestMergedValue))

    fun playWin() = play(winId)

    fun playLose() = play(loseId)

    // Giải phóng bộ nhớ và luồng âm thanh; gọi ở onDestroy của Activity
    fun release() = pool.release()

    private fun play(soundId: Int, volume: Float = 1f, rate: Float = 1f) {
        if (!isEnabled()) return
        pool.play(soundId, volume, volume, 1, 0, rate)
    }

    companion object {
        private const val MAX_STREAMS = 4
        private const val MIN_RATE = 0.5f // giới hạn của SoundPool: 0.5 đến 2.0
        private const val MAX_RATE = 2.0f

        // Ô 4 -> 1.0, 8 -> 1.1, 16 -> 1.2 ... 2048 -> 1.9 (mỗi lần gấp đôi tăng 0.1).
        // Ô gộp là lũy thừa của 2 nên log2 chính là số bit 0 ở cuối.
        fun rateFor(value: Int): Float {
            if (value < 4) return 1f
            val step = value.countTrailingZeroBits() - 2 // 4 = 2^2 là mức chuẩn
            return (1f + step * 0.1f).coerceIn(MIN_RATE, MAX_RATE)
        }
    }
}
