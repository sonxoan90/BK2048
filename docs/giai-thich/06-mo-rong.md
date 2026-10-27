# Giai đoạn 6: Mở rộng (Undo, Lịch sử bằng Room, Cài đặt)

Làm theo thứ tự ưu tiên của CLAUDE.md. Các tính năng cũ vẫn chạy như trước.

## 1. Hoàn tác (Undo)
- `Game` đã có sẵn `undo()` từ GĐ 1: trước mỗi lượt đi có làm đổi bàn cờ, nó lưu một `Snapshot` (bàn cờ, điểm, trạng thái, đã thắng chưa). Undo trả về đúng snapshot đó. **Chỉ quay lại được 1 bước.**
- `GameViewModel.undo()` gọi `game.undo()` rồi `refresh()`; `canUndo` là `LiveData<Boolean>` để nút **Hoàn tác** tự bật/tắt (mờ khi không còn bước để quay lại).
- Khi thua, hộp thoại có thêm nút *Hoàn tác* để quay lại nước đi cuối.

## 2. Lịch sử ván chơi (Room, Chương 9)
| File | Vai trò |
|---|---|
| `data/GameRecord.kt` | `@Entity`: một dòng của bảng (điểm, ô lớn nhất, đã thắng, thời gian) |
| `data/GameRecordDao.kt` | `@Dao`: `insert`, `getAll`, `deleteAll` (đều là hàm `suspend`) |
| `data/AppDatabase.kt` | `@Database`, tạo một bản duy nhất cho cả app |
| `ui/history/HistoryActivity.kt`, `HistoryAdapter.kt` | Màn danh sách bằng RecyclerView, ván mới nhất ở trên |

- **Khi nào ghi một ván:** bấm *Ván mới* khi ván đang có điểm, hoặc rời màn hình lúc đã thua. Cờ `recorded` trong ViewModel chống ghi hai lần.
- **Coroutines:** ghi bằng `viewModelScope.launch { withContext(NonCancellable) { dao.insert(...) } }`; đọc bằng `lifecycleScope.launch { dao.getAll() }`. Hàm `suspend` chạy ở luồng nền nên giao diện không bị đơ.
- `GameViewModel` đổi sang `AndroidViewModel` vì cần `Application` để mở database.
- Gradle: thêm Room và KSP (`gradle/libs.versions.toml`), `room-compiler` chạy qua `ksp(...)`.

## 3. Cài đặt
- `SettingsActivity`: công tắc **Hiệu ứng động** (lưu vào SharedPreferences, `GameActivity` kiểm tra trước khi chạy animation) và nút **Xóa dữ liệu** có hộp thoại xác nhận.
- Xóa dữ liệu: `GameStorage.clearAllData()` (ván lưu, điểm cao, ô lớn nhất) và `deleteAll()` của Room. Cài đặt hiệu ứng được giữ lại.
- Menu có thêm nút *Lịch sử* và *Cài đặt*.

## Chạy thử
1. Vào ván mới, vuốt vài nước, bấm *Hoàn tác*: điểm và bàn cờ trở lại, nút mờ đi.
2. Bấm *Ván mới* rồi vào *Lịch sử*: thấy ván vừa bỏ với ô lớn nhất và điểm.
3. *Cài đặt* → *Xóa dữ liệu* → *Xóa*: điểm cao về 0, mất nút *Chơi tiếp*, lịch sử trống, Bộ sưu tập khóa lại.

## 3–5 câu thầy có thể hỏi
1. **Room là gì, gồm những phần nào?** Thư viện giúp dùng SQLite dễ hơn: `@Entity` (bảng), `@Dao` (câu truy vấn), `@Database` (gom lại). Room tự sinh code cho DAO.
2. **Vì sao hàm DAO là `suspend`?** Truy cập database là việc chậm; `suspend` cho phép chạy ở luồng nền trong coroutine mà không làm đơ giao diện, và Room không cho truy vấn trên luồng chính.
3. **`viewModelScope` và `lifecycleScope` khác gì?** Cả hai tự hủy coroutine khi chủ của nó bị hủy: `viewModelScope` gắn với ViewModel, `lifecycleScope` gắn với Activity.
4. **Vì sao `GameViewModel` kế thừa `AndroidViewModel`?** Để có `Application` làm context cho Room; dùng Activity làm context sẽ bị rò rỉ khi xoay màn hình.
5. **Undo hoạt động thế nào và vì sao chỉ 1 bước?** Mỗi lượt đi lưu một snapshot của lượt trước (bàn cờ bất biến nên chỉ cần giữ tham chiếu). Undo trả về snapshot đó rồi xóa nó nên chỉ quay lại được một bước.
6. **Vì sao xóa dữ liệu phải có hộp thoại xác nhận?** Thao tác không hoàn lại được, bấm nhầm là mất hết.
