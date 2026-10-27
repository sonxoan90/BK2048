# Sửa lỗi sau rà soát cuối

Ba lỗi tìm ra khi chơi thử toàn bộ kịch bản demo trên emulator.

## 1. Kẹt sau khi thắng rồi chơi tiếp (`domain/Game.kt`)
- **Lỗi:** nếu lúc chạm 2048 bàn cờ đã đầy và không còn cặp kề nhau bằng nhau, bấm *Chơi tiếp* làm `continueAfterWin()` đặt thẳng `state = Playing`. Ván kẹt: vuốt không đổi gì và không bao giờ hiện hộp thoại thua.
- **Sửa:** `continueAfterWin()` gọi lại `computeState()`. Vì `hasWon` đã là `true` nên hàm này không báo thắng lại, chỉ cho biết `Playing` hay `Lost`.
- **Test mới (`GameTest`):** bàn cờ đầy có 2048 và hết nước đi thì sau *Chơi tiếp* phải là `Lost`; còn nước đi thì là `Playing`.

## 2. Back không thoát được khỏi hộp thoại thắng/thua (`GameActivity`)
- **Lỗi:** hộp thoại đặt `setCancelable(false)`, nên Back bị chặn; muốn về Menu phải chọn *Ván mới* hoặc *Hoàn tác*.
- **Sửa:** hàm `showDialog()` dùng `setOnCancelListener { finish() }`. Back đóng hộp thoại và thoát về Menu (ván vẫn được lưu ở `onPause`; ván thua được ghi vào lịch sử). Chạm ra ngoài hộp thoại vẫn không đóng (`setCanceledOnTouchOutside(false)`) để không lỡ tay.
- `dialog?.dismiss()` ở `onDestroy` không gọi `OnCancelListener` nên xoay màn hình không bị `finish()` nhầm.

## 3. Phím mũi tên đầu tiên bị nuốt (`GameActivity`, `activity_game.xml`)
- **Lỗi:** sau khi chạm màn hình (ví dụ nhấn giữ tiêu đề để vào demo), phím mũi tên đầu tiên không có tác dụng. Đây là hành vi của Android: ở "chế độ cảm ứng", phím điều hướng đầu tiên bị hệ thống dùng để rời chế độ này, trừ khi đã có một view giữ focus.
- **Sửa:** tiêu đề `tvTitle` được đặt `focusable` và `focusableInTouchMode`, rồi `requestFocus()` trong `onCreate`. Có view giữ focus thì hệ thống không nuốt phím nữa, và phím được chuyển cho `onKeyDown` của Activity như bình thường.

## 3–5 câu thầy có thể hỏi
1. **Vì sao cần `computeState()` khi chơi tiếp?** Thắng và thua không loại trừ nhau: ván có thể vừa chạm 2048 vừa hết nước đi. Trạng thái phải luôn được tính lại từ bàn cờ, không đặt cứng.
2. **`setOnCancelListener` khác `setOnDismissListener` thế nào?** `onCancel` chỉ chạy khi người dùng hủy (bấm Back hoặc chạm ngoài); `onDismiss` chạy cả khi code gọi `dismiss()`. Dùng `onCancel` để xoay màn hình không làm thoát màn chơi.
3. **Chế độ cảm ứng (touch mode) là gì?** Trạng thái Android vào khi người dùng chạm màn hình: lúc đó không có view nào giữ focus. Phím điều hướng đầu tiên sẽ đưa app ra khỏi chế độ này.

---

# Dọn các vấn đề nhỏ còn lại

## 4. Menu ngang bị cắt (`layout-land/activity_menu.xml`)
- **Lỗi:** layout dọc xếp 5 nút theo chiều dọc nên ở màn ngang nút cuối bị cắt, phải cuộn.
- **Sửa:** layout ngang chia hai cột: tên game, slogan, điểm cao bên trái; các nút bên phải.
- **Lỗi phát sinh và cách tránh:** lúc đầu mình để gốc layout ngang là `LinearLayout`, trong khi layout dọc gốc là `ScrollView`, cùng id `main`. Khi xoay màn hình Android khôi phục trạng thái theo id nên đưa nhầm trạng thái `LinearLayout` vào `ScrollView` và app crash (`ClassCastException`). **Quy tắc:** view cùng id ở các layout dọc/ngang phải cùng kiểu. Giờ cả hai gốc đều là `ScrollView`.

## 5. "Ván mới" ở Menu ghi đè không hỏi (`MenuActivity`)
- Có ván đang lưu thì hiện hộp thoại "Bỏ ván đang chơi?". *Hủy* giữ nguyên. *Ván mới* thì ván cũ (nếu đã có điểm) được ghi vào Lịch sử bằng Room rồi mới mở ván mới. `lifecycleScope.launch` đảm bảo ghi xong mới chuyển màn.

## 6. Điểm cao và ô lớn nhất lưu ngay (`GameActivity`)
- Trước đây chỉ lưu ở `onPause`, nên app bị tắt đột ngột thì mất. Giờ `observe` của `bestScore` và `board` gọi `saveHighScore` / `saveMaxTile` ngay khi giá trị tăng (hàm chỉ ghi khi lớn hơn giá trị cũ). Ván đang chơi vẫn chỉ lưu ở `onPause`.

## 7. Lint
- Xóa hai màu thừa (`black`, `white`).
- `SetTextI18n`: số hiển thị dùng `getString(R.string.number_format, ...)` (định dạng theo ngôn ngữ máy), dòng chi tiết lịch sử dùng chuỗi có tham số `history_detail` thay vì nối chuỗi.
- Số nhỏ ở góc ô tăng từ 10sp lên 11sp.
- Cảnh báo lint giảm từ 22 xuống 15. Còn lại 9 thư viện có bản mới (đổi phiên bản thì phải hỏi nhóm) và 6 gợi ý dùng hàm KTX `edit {}`.

## Câu thầy có thể hỏi
1. **Vì sao xoay màn hình có thể crash dù không viết code xử lý xoay?** Android lưu trạng thái của từng view theo id rồi khôi phục khi Activity được tạo lại; nếu cùng id nhưng khác kiểu view ở hai layout thì khôi phục sai.
2. **Vì sao hàm ghi lịch sử là `suspend`?** Ghi database là việc chậm; `suspend` cho chạy ở luồng nền trong coroutine mà không làm đơ giao diện.
