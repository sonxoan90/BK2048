package vn.edu.hust.bk2048.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// [KT Ch9] Cơ sở dữ liệu Room: khai báo danh sách bảng (entities) và phiên bản
@Database(entities = [GameRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameRecordDao(): GameRecordDao

    companion object {
        // Chỉ tạo một bản duy nhất cho cả app (tạo database tốn tài nguyên)
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bk2048.db"
                ).build().also { instance = it }
            }
    }
}
