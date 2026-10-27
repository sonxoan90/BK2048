package vn.edu.hust.bk2048.domain

import kotlin.random.Random

// Chạy thử logic trên console (không cần Android): chạy hàm main() trong Android Studio

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
