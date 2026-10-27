package vn.edu.hust.bk2048.ui.history

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vn.edu.hust.bk2048.data.AppDatabase
import vn.edu.hust.bk2048.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // [KT Ch9] lifecycleScope.launch: đọc database ở luồng nền, xong thì cập nhật giao diện
        lifecycleScope.launch {
            val records = AppDatabase.get(this@HistoryActivity).gameRecordDao().getAll()
            binding.recycler.adapter = HistoryAdapter(records)
            binding.tvEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
