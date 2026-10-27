package vn.edu.hust.bk2048.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import vn.edu.hust.bk2048.R
import vn.edu.hust.bk2048.data.AppDatabase
import vn.edu.hust.bk2048.data.GameStorage
import vn.edu.hust.bk2048.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var storage: GameStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        storage = GameStorage(this)

        // Công tắc hiệu ứng: đọc giá trị đã lưu, đổi là lưu ngay
        binding.switchAnimation.isChecked = storage.isAnimationEnabled()
        binding.switchAnimation.setOnCheckedChangeListener { _, checked ->
            storage.setAnimationEnabled(checked)
        }

        binding.btnClearData.setOnClickListener { confirmClear() }
    }

    // Xóa dữ liệu là thao tác không hoàn lại được nên phải hỏi xác nhận
    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_clear_title)
            .setMessage(R.string.settings_clear_message)
            .setPositiveButton(R.string.settings_clear_confirm) { _, _ -> clearAllData() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun clearAllData() {
        storage.clearAllData()
        lifecycleScope.launch {
            AppDatabase.get(this@SettingsActivity).gameRecordDao().deleteAll()
            Toast.makeText(this@SettingsActivity, R.string.settings_cleared, Toast.LENGTH_SHORT).show()
        }
    }
}
