package vn.edu.hust.bk2048.domain

// Kết quả của việc gộp một hàng. mergedAt: các vị trí trong hàng mới vừa là ô gộp (cho animation)
// [KT Ch2.2] tham số mặc định: mergedAt không bắt buộc phải truyền
data class RowResult(
    val row: List<Int>,
    val score: Int,
    val moved: Boolean,
    val mergedAt: List<Int> = emptyList()
)

// Kết quả của một lượt vuốt trên cả bàn cờ. mergedCells: các ô (hàng, cột) vừa gộp
data class MoveResult(
    val board: Board,
    val score: Int,
    val moved: Boolean,
    val mergedCells: List<Pair<Int, Int>> = emptyList()
)

// Trượt một hàng sang TRÁI và gộp các ô bằng nhau.
// Mỗi ô chỉ gộp tối đa 1 lần: [2,2,2,2] -> [4,4,0,0]
fun mergeRow(row: List<Int>): RowResult {
    // [KT Ch2.5] filter bỏ các ô trống trước khi gộp
    val tiles = row.filter { it != 0 }
    val merged = mutableListOf<Int>()
    val mergedAt = mutableListOf<Int>()
    var score = 0
    var i = 0
    // [KT Ch1.5] vòng lặp while duyệt từng ô
    while (i < tiles.size) {
        if (i + 1 < tiles.size && tiles[i] == tiles[i + 1]) {
            val value = tiles[i] * 2
            mergedAt.add(merged.size) // ô gộp sẽ nằm ở vị trí này trong hàng mới
            merged.add(value)
            score += value
            i += 2 // nhảy qua cả hai ô vừa gộp để ô mới không gộp lần nữa
        } else {
            merged.add(tiles[i])
            i += 1
        }
    }
    // Thêm số 0 vào cuối cho đủ độ dài hàng
    val newRow = merged + List(row.size - merged.size) { 0 }
    return RowResult(newRow, score, newRow != row, mergedAt)
}

// Vuốt cả bàn cờ theo một hướng. Cả 4 hướng đều dùng chung mergeRow:
//  - LEFT : dùng luôn
//  - RIGHT: đảo hàng -> gộp -> đảo lại
//  - UP   : chuyển vị -> gộp -> chuyển vị lại
//  - DOWN : chuyển vị + đảo hàng -> gộp -> làm ngược lại
fun move(board: Board, direction: Direction): MoveResult {
    // [KT Ch1.5] when chọn cách "xoay" bàn cờ để hướng nào cũng thành vuốt trái
    val prepared = when (direction) {
        Direction.LEFT -> board
        Direction.RIGHT -> board.reversedRows()
        Direction.UP -> board.transpose()
        Direction.DOWN -> board.transpose().reversedRows()
    }

    // [KT Ch2.4] map với lambda: gộp từng hàng
    val results = prepared.cells.map { mergeRow(it) }
    val merged = Board(results.map { it.row })

    val restored = when (direction) {
        Direction.LEFT -> merged
        Direction.RIGHT -> merged.reversedRows()
        Direction.UP -> merged.transpose()
        Direction.DOWN -> merged.reversedRows().transpose()
    }

    // Đổi vị trí ô gộp (hàng r, vị trí i trong bàn cờ đã "xoay") về vị trí thật trên bàn cờ gốc
    val last = board.size - 1
    // [KT Ch2.5] flatMapIndexed: duyệt từng hàng kèm chỉ số, gom các ô gộp thành một danh sách
    val mergedCells = results.flatMapIndexed { r, result ->
        result.mergedAt.map { i ->
            when (direction) {
                Direction.LEFT -> Pair(r, i)
                Direction.RIGHT -> Pair(r, last - i)
                Direction.UP -> Pair(i, r)
                Direction.DOWN -> Pair(last - i, r)
            }
        }
    }
    return MoveResult(restored, results.sumOf { it.score }, results.any { it.moved }, mergedCells)
}
