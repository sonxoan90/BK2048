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
