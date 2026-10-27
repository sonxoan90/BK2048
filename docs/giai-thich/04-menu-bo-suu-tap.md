# Giai đoạn 4: Menu, Bộ sưu tập và chế độ demo

## Làm gì
- **MenuActivity** (màn khởi động): tên game, slogan, điểm cao nhất, các nút *Chơi tiếp* (chỉ hiện khi có ván đang lưu), *Ván mới*, *Bộ sưu tập*.
- **CollectionActivity**: danh sách 11 mức điểm bằng RecyclerView. Mức đã đạt hiện ô, tên và mô tả; mức chưa đạt hiện ô khóa "?" và "???".
- **Chế độ demo:** nhấn giữ tiêu đề "2048 BK" ở Menu để mở ván dựng sẵn gần thắng.
- Nút *Cài đặt* chưa có vì `SettingsActivity` thuộc GĐ 6 (mở rộng).

## File
| File | Vai trò |
|---|---|
| `ui/menu/MenuActivity.kt` | Menu; gửi chế độ chơi sang GameActivity bằng Intent |
| `ui/collection/CollectionActivity.kt` | Đọc ô lớn nhất từ `GameStorage`, gắn adapter |
| `ui/collection/TileAdapter.kt` | Adapter + ViewHolder cho RecyclerView |
| `ui/game/GameActivity.kt` | `chooseStartGame()` chọn ván theo Intent |
| `domain/Demo.kt` | `DEMO_BOARD`: bàn cờ demo |
| `res/layout/activity_menu.xml`, `activity_collection.xml`, `item_collection.xml` | Giao diện |
| `AndroidManifest.xml` | Menu là màn khởi động; hai Activity còn lại `exported="false"` |

## Điều hướng bằng Intent
Menu tạo `Intent(this, GameActivity::class.java)` rồi `putExtra("mode", ...)` với ba giá trị `new` / `continue` / `demo`. `GameActivity` đọc `intent.getStringExtra` trong `chooseStartGame()`:
- `new`: xóa ván đã lưu, bắt đầu ván mới.
- `continue`: nạp ván đã lưu.
- `demo`: nạp `DEMO_BOARD` (điểm 5000).
Khi Activity bị tạo lại (xoay màn hình hoặc tắt rồi mở lại), luôn dùng ván đã lưu để không áp dụng lại "Ván mới" hay "Demo".

## Ván demo
`DEMO_BOARD` có hàng đầu `512, 512, 256, 128` và ô `1024` ngay dưới ô đầu tiên. **Vuốt trái** thì hàng đầu thành `1024, 256, 128`; **vuốt lên** thì hai ô 1024 cùng cột gộp thành **2048** và hộp thoại thắng hiện ra.

## RecyclerView
Adapter có 3 hàm bắt buộc: `onCreateViewHolder` (tạo dòng), `onBindViewHolder` (đổ dữ liệu vào dòng), `getItemCount`. Một mức được mở khi `info.value <= maxTile`, với `maxTile` là ô lớn nhất từng đạt, lưu từ GĐ 3 trong `GameStorage.saveMaxTile` (gọi ở `onPause`).

## Chạy thử
1. Mở app, bấm *Ván mới*, chơi vài nước, bấm Back: Menu có nút *Chơi tiếp* và điểm cao.
2. Nhấn giữ tiêu đề, vuốt trái rồi vuốt lên: hộp thoại thắng. Vào *Bộ sưu tập*: mọi mức đều mở.
3. Muốn thấy trạng thái khóa: xóa dữ liệu app (`adb shell pm clear vn.edu.hust.bk2048`).

## 3–5 câu thầy có thể hỏi
1. **Intent là gì, `putExtra` để làm gì?** Intent là "tin nhắn" yêu cầu hệ thống mở một Activity; `putExtra` đính kèm dữ liệu (ở đây là chế độ chơi) để Activity đích đọc bằng `getStringExtra`.
2. **Vì sao RecyclerView tiết kiệm hơn ListView/ScrollView?** Nó chỉ tạo các dòng đang nhìn thấy và tái sử dụng khi cuộn (ViewHolder), không tạo cả danh sách.
3. **Vì sao hai Activity để `exported="false"`?** Chỉ trong app mới mở được chúng; app khác không gọi được.
4. **Vì sao Menu cập nhật ở `onResume` chứ không phải `onCreate`?** `onCreate` chỉ chạy một lần; khi quay lại từ màn chơi thì Menu chỉ chạy `onResume`, lúc đó điểm cao và nút *Chơi tiếp* mới đổi.
5. **Nhấn giữ tiêu đề hoạt động thế nào?** `setOnLongClickListener` với lambda; trả về `true` báo là đã xử lý để không kích hoạt thêm sự kiện click.
6. **Lỗi `???` trong strings.xml là gì?** Chuỗi bắt đầu bằng `?` bị aapt hiểu là tham chiếu thuộc tính nên phải viết `\?\?\?`.
