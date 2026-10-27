package vn.edu.hust.bk2048.domain

// [KT Ch3.4] data class: tự có equals/copy/toString, nên so sánh hai bàn cờ chỉ cần dùng ==
// [KT Ch1.6] bàn cờ là List<List<Int>> (mảng 2 chiều bất biến), 0 nghĩa là ô trống
data class Board(val cells: List<List<Int>>) {

    // Số hàng (= số cột) của bàn cờ
    val size: Int get() = cells.size

    // Chuyển bàn cờ thành chuỗi "2,0,4,..." để lưu vào SharedPreferences
    fun toText(): String = cells.flatten().joinToString(",")

    companion object {
        // [KT Ch2.2] tham số mặc định: Board.empty() là bàn cờ 4x4 rỗng
        fun empty(size: Int = 4) = Board(List(size) { List(size) { 0 } })

        // Đọc lại chuỗi đã lưu; trả về null nếu chuỗi hỏng
        // [KT Ch1.4] null safety: toIntOrNull trả về Int? nên phải kiểm tra
        fun fromText(text: String, size: Int = 4): Board? {
            val numbers = text.split(",").map { it.trim().toIntOrNull() }
            if (numbers.size != size * size || numbers.any { it == null }) return null
            return Board(numbers.filterNotNull().chunked(size))
        }
    }
}
