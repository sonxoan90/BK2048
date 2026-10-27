package vn.edu.hust.bk2048.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// [KT Ch9] @Entity: mỗi GameRecord là một dòng trong bảng "game_records" của Room
@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Room tự đánh số
    val score: Int,
    val maxTile: Int,   // ô lớn nhất đạt được trong ván
    val won: Boolean,   // ván đã chạm ô 2048 chưa
    val playedAt: Long  // thời điểm kết thúc ván (mili giây, System.currentTimeMillis)
)
