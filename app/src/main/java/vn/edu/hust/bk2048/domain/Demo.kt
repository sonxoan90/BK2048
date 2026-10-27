package vn.edu.hust.bk2048.domain

import kotlin.random.Random

// Chạy thử logic trên console (không cần Android): chạy hàm main() trong Android Studio

// Bàn cờ dựng sẵn cho chế độ demo (nhấn giữ tiêu đề ở Menu).
// Vuốt TRÁI rồi vuốt LÊN là có ô 2048: hàng đầu gộp thành 1024, cột đầu có hai ô 1024.
val DEMO_BOARD = Board(
    listOf(
        listOf(512, 512, 256, 128),
        listOf(1024, 0, 0, 0),
        listOf(64, 32, 16, 8),
        listOf(4, 2, 0, 0)
    )
)

private fun printGame(game: Game) {
    println("Điểm: ${game.score}  Trạng thái: ${game.state::class.simpleName}")
    game.board.cells.forEach { row ->
        // [KT Ch2.4] joinToString với lambda: mỗi ô in nhãn điểm chữ, ô trống in dấu chấm
        println(row.joinToString(" ") { value ->
            (if (value == 0) "." else TileSets.infoFor(value).label).padStart(3)
        })
    }
    println()
}

fun main() {
    println("== mergeRow ==")
    listOf(listOf(2, 2, 2, 2), listOf(2, 2, 4, 0), listOf(4, 4, 8, 8)).forEach {
        println("$it -> ${mergeRow(it).row}")
    }

    println("\n== Ván mới (seed 42) ==")
    val game = Game(random = Random(42))
    printGame(game)
    for (dir in listOf(Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN)) {
        val moved = game.move(dir)
        println("Vuốt $dir (bàn cờ có đổi: $moved)")
        printGame(game)
    }
}
