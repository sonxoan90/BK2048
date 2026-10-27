package vn.edu.hust.bk2048.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.edu.hust.bk2048.R
import vn.edu.hust.bk2048.data.GameStorage
import vn.edu.hust.bk2048.databinding.ActivityMenuBinding
import vn.edu.hust.bk2048.ui.collection.CollectionActivity
import vn.edu.hust.bk2048.ui.game.GameActivity

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
        binding.btnNewGame.setOnClickListener { startGame(GameActivity.MODE_NEW) }
        binding.btnCollection.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java))
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

    private fun startGame(mode: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_MODE, mode)
        startActivity(intent)
    }
}
