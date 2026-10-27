package vn.edu.hust.bk2048.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.edu.hust.bk2048.R
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import vn.edu.hust.bk2048.data.AppDatabase
import vn.edu.hust.bk2048.data.GameRecord
import vn.edu.hust.bk2048.data.GameStorage
import vn.edu.hust.bk2048.data.SavedGame
import vn.edu.hust.bk2048.domain.maxTile
import vn.edu.hust.bk2048.databinding.ActivityMenuBinding
import vn.edu.hust.bk2048.ui.collection.CollectionActivity
import vn.edu.hust.bk2048.ui.game.GameActivity
import vn.edu.hust.bk2048.ui.history.HistoryActivity
import vn.edu.hust.bk2048.ui.settings.SettingsActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var storage: GameStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        storage = GameStorage(this)

        // [KT Ch4] xử lý sự kiện bấm nút bằng lambda; Intent mở màn hình khác
        binding.btnContinue.setOnClickListener { startGame(GameActivity.MODE_CONTINUE) }
        binding.btnNewGame.setOnClickListener { onNewGameClicked() }
        binding.btnCollection.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        // Nhấn giữ tiêu đề: mở ván demo gần thắng. Trả về true = đã xử lý sự kiện
        binding.tvTitle.setOnLongClickListener {
            startGame(GameActivity.MODE_DEMO)
            true
        }
    }

    // Quay lại từ màn chơi thì điểm cao và nút "Chơi tiếp" có thể đã đổi
    override fun onResume() {
        super.onResume()
        binding.tvHighScore.text = getString(R.string.menu_high_score, storage.getHighScore())
        binding.btnContinue.visibility = if (storage.hasSavedGame()) View.VISIBLE else View.GONE
    }

    // Đang có ván lưu thì hỏi trước khi bỏ, và ghi ván đó vào lịch sử (nếu đã có điểm)
    private fun onNewGameClicked() {
        val saved = storage.loadGame()
        if (saved == null) {
            startGame(GameActivity.MODE_NEW)
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_game_confirm_title)
            .setMessage(R.string.new_game_confirm_message)
            .setPositiveButton(R.string.new_game) { _, _ -> recordThenStartNewGame(saved) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // [KT Ch9] ghi bản ghi xong (suspend) rồi mới mở ván mới
    private fun recordThenStartNewGame(saved: SavedGame) {
        lifecycleScope.launch {
            if (saved.score > 0) {
                val record = GameRecord(
                    score = saved.score,
                    maxTile = saved.board.maxTile(),
                    won = saved.hasWon,
                    playedAt = System.currentTimeMillis()
                )
                AppDatabase.get(this@MenuActivity).gameRecordDao().insert(record)
            }
            startGame(GameActivity.MODE_NEW)
        }
    }

    private fun startGame(mode: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_MODE, mode)
        startActivity(intent)
    }
}
