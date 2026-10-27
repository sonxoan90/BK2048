# Giai đoạn 3: Lưu trữ (GameStorage + SharedPreferences)

## Làm gì
Tắt hẳn app rồi mở lại vẫn chơi tiếp được: lưu ván đang chơi (bàn cờ, điểm, đã thắng chưa), điểm cao nhất và ô lớn nhất từng đạt.

## File
| File | Vai trò |
|---|---|
| `data/GameStorage.kt` | Đọc/ghi `SharedPreferences`; có `data class SavedGame` |
| `ui/game/GameViewModel.kt` | Thêm `start(saved, highScore)` nạp ván đã lưu, chỉ chạy một lần |
| `ui/game/GameActivity.kt` | `onCreate` nạp ván, `onPause` gọi `saveProgress()` |

## Cách hoạt động
- **SharedPreferences** là kho key-value lưu bền trong máy. Mỗi giá trị có một key: `board`, `score`, `has_won`, `high_score`, `max_tile`.
- **Bàn cờ lưu thành chuỗi** `"2,0,4,0,…"` bằng `Board.toText()` (`flatten` + `joinToString`), đọc lại bằng `Board.fromText()` (`split` + `toIntOrNull` + `chunked`). Chuỗi hỏng thì trả về `null`, app bỏ qua và chơi ván mới.
- **Khi nào lưu:** ở `onPause`, vì sau đó app có thể bị hệ thống tắt bất kỳ lúc nào. Ván đã **thua** thì xóa bản lưu để lần sau mở ra là ván mới.
- **Khi nào nạp:** `onCreate` gọi `viewModel.start(...)`. Cờ `started` đảm bảo chỉ nạp khi ViewModel mới tạo; xoay màn hình thì ViewModel còn nguyên nên không nạp đè.
- `saveHighScore` và `saveMaxTile` chỉ ghi khi giá trị mới lớn hơn giá trị đã lưu.

## Chạy thử
Chơi vài nước, bấm nút Home, rồi vuốt tắt app (hoặc `adb shell am force-stop vn.edu.hust.bk2048`), mở lại: bàn cờ và điểm giữ nguyên.

## 3–5 câu thầy có thể hỏi
1. **Vì sao lưu ở `onPause` mà không phải `onDestroy`?** `onDestroy` không được đảm bảo gọi khi hệ thống tắt tiến trình; `onPause` luôn được gọi trước khi Activity rời màn hình.
2. **SharedPreferences lưu được những kiểu nào?** Kiểu đơn giản: String, Int, Boolean, Float, Long. Bàn cờ là List nên phải đổi thành chuỗi.
3. **`getString(KEY, null) ?: return null` nghĩa là gì?** `getString` có thể trả về `null` (chưa lưu). Toán tử `?:` thoát hàm sớm và trả `null` thay vì crash.
4. **`apply()` khác `commit()` thế nào?** `apply()` ghi bất đồng bộ nên không chặn giao diện; `commit()` ghi đồng bộ và trả về kết quả.
5. **Vì sao ván thua không được lưu?** Nạp lại một bàn cờ đã hết nước đi sẽ khiến người chơi không làm gì được.
