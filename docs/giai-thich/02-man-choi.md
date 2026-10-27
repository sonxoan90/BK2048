# Giai đoạn 2: Màn chơi (GameActivity + GameViewModel)

## Làm gì
Vẽ lưới 4×4 hiển thị nhãn điểm chữ (F…KS) kèm số nhỏ ở góc, màu nền theo mức điểm, điểm hiện tại và điểm cao nhất (tạm thời chỉ trong phiên chơi). Vuốt 4 hướng và phím mũi tên, nút *Ván mới*, hộp thoại thắng/thua. Xoay màn hình không mất ván. `GameActivity` tạm là màn khởi động (GĐ 4 sẽ có `MenuActivity`).

## File
| File | Vai trò |
|---|---|
| `ui/game/GameViewModel.kt` | Giữ `Game`, đưa board/score/bestScore/state ra `LiveData` |
| `ui/game/GameActivity.kt` | Tạo 16 ô, `observe` LiveData rồi vẽ lại, nhận vuốt và phím |
| `ui/game/SwipeListener.kt` | `onFling` đổi cử chỉ thành `Direction`, báo ra bằng callback |
| `ui/TileStyle.kt` | Giá trị ô → màu nền, màu chữ |
| `res/layout/activity_game.xml`, `layout-land/…`, `item_tile.xml` | Giao diện dọc, ngang, và một ô |
| `res/values/colors.xml`, `strings.xml` | Màu ô, chuỗi tiếng Việt |

## Luồng một lượt vuốt
Vuốt → `GestureDetector` gọi `SwipeListener.onFling` → callback `viewModel.move(direction)` → `Game.move` (domain) → `refresh()` đặt giá trị mới cho `LiveData` → Activity `observe` thấy thay đổi → `drawBoard` vẽ lại 16 ô.

## Điểm quan trọng
- **Xoay màn hình:** Activity bị hủy và tạo lại (log `onDestroy` → `onCreate`), nhưng `by viewModels()` trả về đúng `GameViewModel` cũ nên `Game` còn nguyên. Activity tạo lại chỉ việc `observe` và vẽ.
- **Kích thước ô:** `grid.doOnLayout` chờ biết chiều rộng lưới rồi chia đều cho 16 ô.
- **Hộp thoại:** `when (state)` trên sealed class; hộp thoại Thắng có "Chơi tiếp" / "Ván mới". Chỉ báo thắng một lần vì `Game.hasWon`.
- **Vòng đời:** các callback `onCreate/onStart/onResume/onPause/onStop/onDestroy` đều `Log.d` (xem trong Logcat, lọc tag `GameActivity`). `onPause` sẽ lưu ván ở GĐ 3.

## Chạy thử
Mở emulator, bấm Run. Vuốt bằng chuột, hoặc dùng phím mũi tên trên bàn phím máy tính. Bấm Rotate trên thanh công cụ emulator để thử xoay.

## 3–5 câu thầy có thể hỏi
1. **Vì sao dùng ViewModel?** Activity bị hủy khi xoay màn hình, còn ViewModel thì không, nên ván chơi giữ nguyên.
2. **LiveData khác biến thường chỗ nào?** LiveData báo cho `observe` mỗi khi giá trị đổi, và chỉ báo khi Activity đang hoạt động nên không crash khi Activity đã đóng.
3. **Vì sao có cặp `_board` và `board`?** `_board` là `MutableLiveData` private để ViewModel sửa, `board` là `LiveData` public để Activity chỉ đọc.
4. **`SwipeListener` dùng kiến thức nào?** Kế thừa `SimpleOnGestureListener` (Ch3.2), nhận lambda `(Direction) -> Unit` làm callback (Ch2.4).
5. **Luật chơi nằm ở đâu?** Toàn bộ ở `domain/`; Activity và ViewModel không có `if` nào về luật, chỉ chuyển lời gọi và vẽ kết quả.
