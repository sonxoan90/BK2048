package vn.edu.hust.bk2048.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vn.edu.hust.bk2048.data.SavedGame
import vn.edu.hust.bk2048.domain.Board
import vn.edu.hust.bk2048.domain.Direction
import vn.edu.hust.bk2048.domain.Game
import vn.edu.hust.bk2048.domain.GameState

// [KT Ch8] ViewModel sống qua việc xoay màn hình nên ván chơi không bị mất.
// Activity chỉ observe các LiveData rồi vẽ lại, không chứa luật chơi.
class GameViewModel : ViewModel() {

    private val game = Game()

    // Cặp MutableLiveData (private, chỉ ViewModel sửa) / LiveData (public, Activity chỉ đọc)
    private val _board = MutableLiveData(game.board)
    val board: LiveData<Board> = _board

    private val _score = MutableLiveData(game.score)
    val score: LiveData<Int> = _score

    private val _bestScore = MutableLiveData(0)
    val bestScore: LiveData<Int> = _bestScore

    private val _state = MutableLiveData<GameState>(game.state)
    val state: LiveData<GameState> = _state

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

    fun newGame() {
        game.newGame()
        refresh()
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
    }
}
