package vn.edu.hust.bk2048.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// [KT Ch9] @Dao: các hàm truy cập bảng. Room tự sinh code cho interface này.
// [KT Ch9] suspend: hàm của Coroutines, chạy ở luồng nền nên không làm đơ giao diện
@Dao
interface GameRecordDao {

    @Insert
    suspend fun insert(record: GameRecord)

    // Ván mới nhất ở trên cùng
    @Query("SELECT * FROM game_records ORDER BY playedAt DESC")
    suspend fun getAll(): List<GameRecord>

    @Query("DELETE FROM game_records")
    suspend fun deleteAll()
}
