package vn.edu.hust.bk2048.data

import android.content.Context
import vn.edu.hust.bk2048.domain.Board

// Ván đang chơi dở được đọc lại từ bộ nhớ
data class SavedGame(val board: Board, val score: Int, val hasWon: Boolean)

// Lưu dữ liệu bằng SharedPreferences (cặp key-value, lưu bền trong máy)
class GameStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Bàn cờ lưu dạng chuỗi "2,0,4,..." nhờ Board.toText()
    fun saveGame(board: Board, score: Int, hasWon: Boolean) {
        prefs.edit()
            .putString(KEY_BOARD, board.toText())
            .putInt(KEY_SCORE, score)
            .putBoolean(KEY_HAS_WON, hasWon)
            .apply()
    }

    // [KT Ch1.4] null safety: getString có thể trả về null, ?: return null nếu chưa có ván lưu
    fun loadGame(): SavedGame? {
        val text = prefs.getString(KEY_BOARD, null) ?: return null
        val board = Board.fromText(text) ?: return null // chuỗi hỏng thì bỏ qua
        return SavedGame(board, prefs.getInt(KEY_SCORE, 0), prefs.getBoolean(KEY_HAS_WON, false))
    }

    // [KT Ch2.3] hàm rút gọn
    fun hasSavedGame() = loadGame() != null

    fun clearGame() {
        prefs.edit().remove(KEY_BOARD).remove(KEY_SCORE).remove(KEY_HAS_WON).apply()
    }

    fun getHighScore(): Int = prefs.getInt(KEY_HIGH_SCORE, 0)

    // Chỉ ghi khi điểm mới cao hơn điểm cao cũ
    fun saveHighScore(score: Int) {
        if (score > getHighScore()) prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
    }

    // Ô lớn nhất từng đạt (dùng cho Bộ sưu tập ở GĐ 4)
    fun getMaxTile(): Int = prefs.getInt(KEY_MAX_TILE, 0)

    fun saveMaxTile(value: Int) {
        if (value > getMaxTile()) prefs.edit().putInt(KEY_MAX_TILE, value).apply()
    }

    // Bật/tắt hiệu ứng động (mặc định là bật)
    fun isAnimationEnabled(): Boolean = prefs.getBoolean(KEY_ANIMATION, true)

    fun setAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATION, enabled).apply()
    }

    // Bật/tắt âm thanh (mặc định là bật)
    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    // Xóa ván đang lưu, điểm cao, ô lớn nhất. Giữ lại cài đặt (hiệu ứng, âm thanh) của người dùng.
    fun clearAllData() {
        prefs.edit()
            .remove(KEY_BOARD).remove(KEY_SCORE).remove(KEY_HAS_WON)
            .remove(KEY_HIGH_SCORE).remove(KEY_MAX_TILE)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "bk2048_prefs"
        const val KEY_ANIMATION = "animation_enabled"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_BOARD = "board"
        const val KEY_SCORE = "score"
        const val KEY_HAS_WON = "has_won"
        const val KEY_HIGH_SCORE = "high_score"
        const val KEY_MAX_TILE = "max_tile"
    }
}
