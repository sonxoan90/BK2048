package vn.edu.hust.bk2048.domain

// [KT Ch3.4] object: chỉ có một bản duy nhất (singleton), gọi thẳng TileSets.infoFor(...)
object TileSets {
    // 11 mức điểm theo thang điểm chữ Bách khoa
    val grades: List<TileInfo> = listOf(
        TileInfo(2, "F", "Điểm F", "Ai rồi cũng từng bắt đầu từ đây"),
        TileInfo(4, "D", "Điểm D", "Qua môn trong gang tấc"),
        TileInfo(8, "D+", "Điểm D+", "Đã bớt run khi xem điểm"),
        TileInfo(16, "C", "Điểm C", "Trung bình nhưng không tầm thường"),
        TileInfo(32, "C+", "Điểm C+", "Bắt đầu có phong độ"),
        TileInfo(64, "B", "Điểm B", "Khá rồi đấy"),
        TileInfo(128, "B+", "Điểm B+", "Gần chạm tới giỏi"),
        TileInfo(256, "A", "Điểm A", "Giỏi thật sự"),
        TileInfo(512, "A+", "Điểm A+", "Điểm tuyệt đối"),
        TileInfo(1024, "HB", "Học bổng", "Thành tích được ghi nhận"),
        TileInfo(2048, "KS", "Kỹ sư Bách khoa", "Tấm bằng trong tay. Chiến thắng!")
    )

    // [KT Ch2.5] firstOrNull + ?: các ô từ 4096 trở lên dùng chung một kiểu "vượt giới hạn"
    fun infoFor(value: Int): TileInfo =
        grades.firstOrNull { it.value == value }
            ?: TileInfo(value, value.toString(), "Vượt giới hạn", "Dùng chung một kiểu hiển thị")
}
