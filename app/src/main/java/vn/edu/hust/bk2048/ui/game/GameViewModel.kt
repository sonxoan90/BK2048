package vn.edu.hust.bk2048.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.edu.hust.bk2048.data.AppDatabase
import vn.edu.hust.bk2048.data.GameRecord
import vn.edu.hust.bk2048.data.SavedGame
import vn.edu.hust.bk2048.domain.Board
import vn.edu.hust.bk2048.domain.Direction
import vn.edu.hust.bk2048.domain.Game
import vn.edu.hust.bk2048.domain.GameState
import vn.edu.hust.bk2048.domain.maxTile

// [KT Ch8] ViewModel sống qua việc xoay màn hình nên ván chơi không bị mất.
// Activity chỉ observe các LiveData rồi vẽ lại, không chứa luật chơi.
// AndroidViewModel là ViewModel có thêm Application (cần để mở database Room).
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val game = Game()
    private val recordDao = AppDatabase.get(application).gameRecordDao()

    // Ván hiện tại đã được ghi vào lịch sử chưa (tránh ghi hai lần)
    private var recorded = false

    // Cặp MutableLiveData (private, chỉ ViewModel sửa) / LiveData (public, Activity chỉ đọc)
    private val _board = MutableLiveData(game.board)
    val board: LiveData<Board> = _board

    private val _score = MutableLiveData(game.score)
    val score: LiveData<Int> = _score

    private val _bestScore = MutableLiveData(0)
    val bestScore: LiveData<Int> = _bestScore

    private val _state = MutableLiveData<GameState>(game.state)
    val state: LiveData<GameState> = _state

    // Nút Hoàn tác chỉ bật khi còn bước để quay lại
    private val _canUndo = MutableLiveData(game.canUndo)
    val canUndo: LiveData<Boolean> = _canUndo

    // Dành cho animation: ô vừa sinh ra và các ô vừa gộp ở lượt đi gần nhất
    val lastSpawn: Pair<Int, Int>? get() = game.lastSpawn
    val lastMerged: List<Pair<Int, Int>> get() = game.lastMerged

    // Có đang ở trạng thái thắng không (để lưu cùng ván)
    val hasWon: Boolean get() = game.hasWon

    // Nạp ván đã lưu và điểm cao, chỉ chạy một lần khi ViewModel mới tạo
    // (xoay màn hình thì ViewModel còn nguyên nên không nạp lại)
    private var started = false

    fun start(saved: SavedGame?, highScore: Int) {
        if (started) return
        started = true
        // [KT Ch1.4] saved có thể null (chưa có ván lưu) nên dùng ?.let
        saved?.let { game.load(it.board, it.score, it.hasWon) }
        _bestScore.value = maxOf(highScore, game.score)
        refresh()
    }

    fun move(direction: Direction) {
        if (game.move(direction)) refresh()
    }

    // Quay lại 1 bước (chỉ nhớ được lượt đi gần nhất)
    fun undo() {
        if (game.undo()) {
            recorded = false // quay lại thì ván chưa kết thúc
            refresh()
        }
    }

    fun newGame() {
        recordGame() // ván đang chơi (nếu có điểm) được ghi vào lịch sử trước khi bỏ
        recorded = false
        game.newGame()
        refresh()
    }

    // Ván đã thua mà người chơi rời màn hình thì cũng ghi vào lịch sử
    fun recordIfLost() {
        if (game.state is GameState.Lost) recordGame()
    }

    // [KT Ch9] viewModelScope.launch: chạy coroutine, tự hủy khi ViewModel bị xóa
    private fun recordGame() {
        if (recorded || game.score == 0) return
        recorded = true
        val record = GameRecord(
            score = game.score,
            maxTile = game.board.maxTile(),
            won = game.hasWon,
            playedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            // NonCancellable: Activity đóng ngay sau đó cũng không làm mất bản ghi
            withContext(NonCancellable) { recordDao.insert(record) }
        }
    }

    fun continueAfterWin() {
        game.continueAfterWin()
        refresh()
    }

    // Đẩy trạng thái mới của Game vào LiveData để giao diện tự vẽ lại
    private fun refresh() {
        _board.value = game.board
        _score.value = game.score
        // [KT Ch1.4] value của LiveData có thể null nên dùng ?: 0
        if (game.score > (_bestScore.value ?: 0)) _bestScore.value = game.score
        _state.value = game.state
        _canUndo.value = game.canUndo
    }
}
