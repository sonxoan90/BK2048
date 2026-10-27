package vn.edu.hust.bk2048.domain

// [KT Ch3.3] extension function: thêm hàm cho class Board mà không sửa class đó

// Chuyển vị: hàng thành cột, cột thành hàng (dùng cho vuốt lên/xuống)
fun Board.transpose(): Board =
    Board(List(size) { col -> List(size) { row -> cells[row][col] } })

// Đảo từng hàng (dùng cho vuốt phải/xuống)
fun Board.reversedRows(): Board = Board(cells.map { it.reversed() })

// Danh sách vị trí (hàng, cột) của các ô trống
// [KT Ch2.5] flatMapIndexed + mapIndexedNotNull để lấy ra các ô bằng 0
fun Board.emptyCells(): List<Pair<Int, Int>> =
    cells.flatMapIndexed { r, row ->
        row.mapIndexedNotNull { c, value -> if (value == 0) Pair(r, c) else null }
    }

// [KT Ch2.3] hàm rút gọn (single-expression)
fun Board.isFull() = emptyCells().isEmpty()

// [KT Ch2.5] maxOrNull trả về Int? nên dùng ?: để có giá trị mặc định
fun Board.maxTile(): Int = cells.flatten().maxOrNull() ?: 0

// Còn nước đi nào không? (còn ô trống, hoặc có hai ô kề nhau bằng nhau)
fun Board.hasMoves(): Boolean {
    if (!isFull()) return true
    // [KT Ch1.5] vòng lặp + ranges
    for (r in 0 until size) {
        for (c in 0 until size) {
            val right = c + 1 < size && cells[r][c] == cells[r][c + 1]
            val down = r + 1 < size && cells[r][c] == cells[r + 1][c]
            if (right || down) return true
        }
    }
    return false
}
