package vn.edu.hust.bk2048.domain

// [KT Ch3.4] data class lưu thông tin hiển thị của một mức ô
data class TileInfo(
    val value: Int,
    val label: String,       // nhãn ngắn trên ô (tối đa 2 ký tự)
    val title: String,       // tên đầy đủ (Bộ sưu tập)
    val description: String  // mô tả (Bộ sưu tập)
)
