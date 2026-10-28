package vn.edu.hust.bk2048.ui.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.edu.hust.bk2048.R
import vn.edu.hust.bk2048.databinding.ActivityGameBinding
import vn.edu.hust.bk2048.databinding.ItemTileBinding
import vn.edu.hust.bk2048.data.GameStorage
import vn.edu.hust.bk2048.data.SavedGame
import vn.edu.hust.bk2048.domain.Board
import vn.edu.hust.bk2048.domain.DEMO_BOARD
import vn.edu.hust.bk2048.domain.maxTile
import vn.edu.hust.bk2048.domain.Direction
import vn.edu.hust.bk2048.domain.GameState
import vn.edu.hust.bk2048.domain.TileSets
import vn.edu.hust.bk2048.ui.SoundManager
import vn.edu.hust.bk2048.ui.TileStyle

class GameActivity : AppCompatActivity() {

    // [KT Ch4] View Binding: truy cập view qua binding thay vì findViewById
    private lateinit var binding: ActivityGameBinding

    // [KT Ch8] by viewModels(): lấy GameViewModel, xoay màn hình vẫn là cùng một đối tượng
    private val viewModel: GameViewModel by viewModels()

    // SharedPreferences lưu ván và điểm cao qua các lần mở app
    private lateinit var storage: GameStorage

    // 16 ô trên lưới (theo thứ tự hàng trước, cột sau)
    private val tiles = mutableListOf<ItemTileBinding>()
    private lateinit var gestureDetector: GestureDetector
    private var dialog: AlertDialog? = null

    // Bàn cờ đã vẽ lần trước, để biết có phải vừa đi một nước hay không
    private var previousBoard: Board? = null

    // Trạng thái đã nhận lần trước, để chỉ phát tiếng thắng/thua đúng lúc trạng thái đổi
    // (không phát lại khi Activity được tạo lại lúc xoay màn hình)
    private var previousState: GameState? = null

    private lateinit var sound: SoundManager

    // [KT Ch7] ghi Log các callback vòng đời để xem thứ tự gọi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Chừa chỗ cho thanh trạng thái/thanh điều hướng, cộng thêm lề 16dp của layout
        val padding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                bars.left + padding, bars.top + padding,
                bars.right + padding, bars.bottom + padding
            )
            insets
        }

        // Nạp ván đã lưu (nếu có) vào ViewModel trước khi bắt đầu observe
        storage = GameStorage(this)
        // [KT Ch7] SoundPool giữ bộ nhớ và luồng âm thanh: tạo ở onCreate, giải phóng ở onDestroy
        sound = SoundManager(this) { storage.isSoundEnabled() }
        viewModel.start(chooseStartGame(savedInstanceState), storage.getHighScore())

        // Sau khi chạm màn hình, Android "nuốt" phím mũi tên đầu tiên để rời chế độ cảm ứng,
        // trừ khi đã có một view giữ focus. Cho tiêu đề giữ focus để phím đầu tiên không bị mất.
        binding.tvTitle.requestFocus()

        createTiles()
        setupInput()
        observeViewModel()
        binding.btnNewGame.setOnClickListener { viewModel.newGame() }
        binding.btnUndo.setOnClickListener { viewModel.undo() }
    }

    // Chọn ván bắt đầu theo nút đã bấm ở Menu (Intent extra). null nghĩa là ván mới hoàn toàn.
    private fun chooseStartGame(savedInstanceState: Bundle?): SavedGame? {
        // Activity được tạo lại (xoay màn hình, hoặc app bị tắt rồi mở lại): luôn lấy ván đã lưu,
        // không áp dụng lại "Ván mới" hay "Demo" một lần nữa
        if (savedInstanceState != null) return storage.loadGame()
        // [KT Ch1.5] when chọn theo chế độ
        return when (intent.getStringExtra(EXTRA_MODE)) {
            MODE_NEW -> {
                storage.clearGame()
                null
            }
            MODE_DEMO -> SavedGame(DEMO_BOARD, score = 5000, hasWon = false)
            else -> storage.loadGame() // MODE_CONTINUE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        saveProgress() // [KT Ch7] onPause là lúc an toàn để lưu: app có thể bị tắt ngay sau đó
    }

    private fun saveProgress() {
        val board = viewModel.board.value ?: return
        val score = viewModel.score.value ?: 0
        viewModel.recordIfLost()
        storage.saveHighScore(viewModel.bestScore.value ?: 0)
        storage.saveMaxTile(board.maxTile())
        // Ván đã thua thì không lưu để lần sau mở ra là ván mới
        if (viewModel.state.value is GameState.Lost) {
            storage.clearGame()
        } else {
            storage.saveGame(board, score, viewModel.hasWon)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        dialog?.dismiss() // tránh rò rỉ cửa sổ khi xoay màn hình
        // [KT Ch7] không release thì SoundPool cũ vẫn giữ bộ nhớ sau khi Activity bị hủy (xoay màn hình, thoát)
        sound.release()
    }

    // Tạo sẵn 16 ô, chỉ đổi nội dung khi vẽ lại
    private fun createTiles() {
        val inflater = LayoutInflater.from(this)
        val grid = binding.grid
        // [KT Ch1.5] vòng lặp lồng nhau với range
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                val tile = ItemTileBinding.inflate(inflater, grid, false)
                tile.root.layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(row), GridLayout.spec(col)
                )
                grid.addView(tile.root)
                tiles.add(tile)
            }
        }
        // Khi biết kích thước lưới thì chia đều cho 16 ô
        grid.doOnLayout { sizeTiles() }
    }

    private fun sizeTiles() {
        val margin = (4 * resources.displayMetrics.density).toInt()
        val cell = binding.grid.width / 4
        // [KT Ch2.4] forEach với lambda
        tiles.forEach { tile ->
            val params = tile.root.layoutParams as GridLayout.LayoutParams
            params.width = cell - 2 * margin
            params.height = cell - 2 * margin
            params.setMargins(margin, margin, margin, margin)
            tile.root.layoutParams = params
        }
    }

    // Vuốt trên màn hình
    @SuppressLint("ClickableViewAccessibility")
    private fun setupInput() {
        // [KT Ch2.4] truyền lambda làm callback: mỗi lần vuốt gọi viewModel.move
        gestureDetector = GestureDetector(this, SwipeListener { direction ->
            viewModel.move(direction)
        })
        binding.main.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    // Phím mũi tên (tiện khi demo trên emulator)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // [KT Ch1.5] when trả về hướng tương ứng, phím khác thì null
        val direction = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> Direction.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> Direction.RIGHT
            KeyEvent.KEYCODE_DPAD_UP -> Direction.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> Direction.DOWN
            else -> null
        }
        if (direction == null) return super.onKeyDown(keyCode, event)
        viewModel.move(direction)
        return true
    }

    // [KT Ch8] observe: LiveData đổi thì Activity tự vẽ lại
    private fun observeViewModel() {
        viewModel.board.observe(this) {
            drawBoard(it)
            storage.saveMaxTile(it.maxTile()) // ô mới đạt được mở khóa Bộ sưu tập ngay
        }
        viewModel.score.observe(this) { binding.tvScore.text = getString(R.string.number_format, it) }
        viewModel.bestScore.observe(this) {
            binding.tvBest.text = getString(R.string.number_format, it)
            storage.saveHighScore(it) // lưu ngay khi phá kỷ lục, không đợi onPause (app có thể bị tắt đột ngột)
        }
        viewModel.state.observe(this) {
            // Tiếng thắng/thua chỉ phát khi trạng thái ĐỔI trong lúc đang chơi
            // (previousState == null là lần nhận đầu tiên: mở màn hình hoặc xoay, không phát)
            if (previousState != null && previousState != it) {
                when (it) {
                    GameState.Won -> sound.playWin()
                    GameState.Lost -> sound.playLose()
                    GameState.Playing -> Unit
                }
            }
            previousState = it
            showStateDialog(it)
        }
        // Nút Hoàn tác chỉ bấm được khi còn bước để quay lại
        viewModel.canUndo.observe(this) { binding.btnUndo.isEnabled = it }
    }

    private fun drawBoard(board: Board) {
        // [KT Ch2.4] forEachIndexed: duyệt 16 ô kèm chỉ số
        tiles.forEachIndexed { index, tile ->
            // Dừng animation cũ (nếu còn) để ô luôn về cỡ bình thường trước khi vẽ lại
            tile.root.animate().cancel()
            tile.root.scaleX = 1f
            tile.root.scaleY = 1f
            val value = board.cells[index / 4][index % 4]
            if (value == 0) {
                tile.tvLabel.text = ""
                tile.tvValue.text = ""
            } else {
                val label = TileSets.infoFor(value).label
                tile.tvLabel.text = label
                tile.tvLabel.textSize = when {
                    label.length <= 2 -> 28f
                    label.length == 3 -> 22f
                    else -> 18f
                }
                tile.tvValue.text = getString(R.string.number_format, value)
            }
            val textColor = TileStyle.textColor(this, value)
            tile.tvLabel.setTextColor(textColor)
            tile.tvValue.setTextColor(textColor)
            tile.root.background = TileStyle.background(this, value)
        }

        // Chỉ chạy animation và phát tiếng khi bàn cờ đổi sau một lượt đi (không chạy lúc mở màn hình hay xoay)
        val changed = previousBoard != null && previousBoard != board
        if (changed && storage.isAnimationEnabled()) animateMove()
        if (changed) playMoveSounds(board)
        previousBoard = board
    }

    // Tiếng trượt cho mọi lượt đi; có ô gộp thì thêm tiếng gộp, cao độ theo ô gộp lớn nhất
    private fun playMoveSounds(board: Board) {
        // Ván mới và Hoàn tác cũng làm bàn cờ đổi nhưng không sinh ô mới (lastSpawn == null)
        // nên không phát tiếng; một lượt đi thật luôn sinh ô mới.
        if (viewModel.lastSpawn == null) return
        sound.playSlide()
        val merged = viewModel.lastMerged
        if (merged.isNotEmpty()) {
            // [KT Ch2.4] maxOf với lambda: lấy giá trị lớn nhất trong các ô vừa gộp
            sound.playMerge(merged.maxOf { (row, col) -> board.cells[row][col] })
        }
    }

    // Ô vừa gộp phóng to rồi thu về; ô mới sinh ra phóng từ nhỏ lên
    private fun animateMove() {
        viewModel.lastMerged.forEach { (row, col) ->
            tiles[row * 4 + col].root.apply {
                scaleX = MERGE_SCALE
                scaleY = MERGE_SCALE
                // [KT Ch5] ViewPropertyAnimator: animate().scaleX(...) chạy hiệu ứng trong một thời gian
                animate().scaleX(1f).scaleY(1f).setDuration(ANIM_MS).start()
            }
        }
        viewModel.lastSpawn?.let { (row, col) ->
            tiles[row * 4 + col].root.apply {
                scaleX = 0f
                scaleY = 0f
                animate().scaleX(1f).scaleY(1f).setDuration(ANIM_MS).start()
            }
        }
    }

    private fun showStateDialog(state: GameState) {
        dialog?.dismiss()
        dialog = null
        // [KT Ch3.4] when trên sealed class: đủ 3 nhánh nên không cần else
        when (state) {
            GameState.Playing -> Unit
            GameState.Won -> dialog = showDialog(
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.win_title)
                    .setMessage(R.string.win_message)
                    .setPositiveButton(R.string.keep_playing) { _, _ -> viewModel.continueAfterWin() }
                    .setNegativeButton(R.string.new_game) { _, _ -> viewModel.newGame() }
            )
            GameState.Lost -> dialog = showDialog(
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.lose_title)
                    .setMessage(getString(R.string.lose_message, viewModel.score.value ?: 0))
                    .setPositiveButton(R.string.new_game) { _, _ -> viewModel.newGame() }
                    .setNegativeButton(R.string.undo) { _, _ -> viewModel.undo() } // thua rồi vẫn quay lại được 1 bước
            )
        }
    }

    // Bấm Back khi đang có hộp thoại thì thoát về Menu (ván vẫn được lưu ở onPause).
    // Chạm ra ngoài hộp thoại thì không đóng, để không lỡ tay.
    private fun showDialog(builder: MaterialAlertDialogBuilder): AlertDialog {
        val alert = builder.setOnCancelListener { finish() }.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
        return alert
    }

    companion object {
        private const val TAG = "GameActivity"
        private const val ANIM_MS = 120L
        private const val MERGE_SCALE = 1.15f

        // Menu gửi chế độ chơi qua Intent bằng key này
        const val EXTRA_MODE = "mode"
        const val MODE_NEW = "new"
        const val MODE_CONTINUE = "continue"
        const val MODE_DEMO = "demo"
    }
}
