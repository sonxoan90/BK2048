package vn.edu.hust.bk2048.ui.collection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.edu.hust.bk2048.data.GameStorage
import vn.edu.hust.bk2048.databinding.ActivityCollectionBinding
import vn.edu.hust.bk2048.domain.TileSets

class CollectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Ô lớn nhất từng đạt quyết định mức nào được mở khóa
        val maxTile = GameStorage(this).getMaxTile()
        binding.recycler.adapter = TileAdapter(TileSets.grades, maxTile)
    }
}
