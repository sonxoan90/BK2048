# Giai đoạn 5: Hoàn thiện (animation, icon, chuỗi tiếng Việt)

## Làm gì
- **Animation đơn giản:** ô vừa **gộp** phóng to nhẹ (1.15 lần) rồi thu về; ô vừa **sinh ra** phóng từ nhỏ lên. Mỗi hiệu ứng 120 ms.
- **Icon app:** nền xanh than với lưới 2×2 bốn ô điểm F (đỏ), D+ (cam), C (vàng), A (xanh lá). Không dùng logo trường hay ảnh người thật.
- **Chuỗi:** kiểm tra lại, mọi chuỗi giao diện đều nằm trong `res/values/strings.xml`. Riêng nhãn/tên/mô tả của 11 mức điểm nằm trong `domain/TileSets.kt` vì đó là dữ liệu của game (theo thiết kế mục 6 của CLAUDE.md).
- **Màu sắc:** màu ô trong `colors.xml` chuyển dần từ đỏ (F) qua vàng (C) đến xanh lá (A+), HB và KS màu vàng gold (đã có từ GĐ 2).

## Làm sao biết ô nào cần animation?
Logic nằm ở `domain/` nên domain cung cấp thông tin, giao diện chỉ vẽ:
1. `mergeRow` trả thêm `mergedAt`: vị trí các ô vừa gộp trong hàng mới.
2. `move(board, direction)` đổi vị trí đó về tọa độ thật trên bàn cờ (mỗi hướng một công thức, vì bàn cờ đã bị "xoay" trước khi gộp) và trả về `mergedCells`.
3. `Game` lưu `lastMerged` và `lastSpawn` (ô trống trước khi sinh nhưng có giá trị sau khi sinh).
4. `GameViewModel` đưa hai thông tin này ra; `GameActivity.animateMove()` đặt `scaleX/scaleY` ban đầu rồi gọi `animate().scaleX(1f).scaleY(1f).setDuration(120).start()`.

`GameActivity` chỉ chạy animation khi bàn cờ **đổi sau một lượt đi** (so với `previousBoard`), nên mở màn hình, xoay màn hình hay bấm *Ván mới* không có hiệu ứng.

## File thay đổi
`domain/MoveLogic.kt`, `domain/Game.kt`, `ui/game/GameViewModel.kt`, `ui/game/GameActivity.kt`, `res/drawable/ic_launcher_background.xml`, `ic_launcher_foreground.xml`, và hai test mới trong `MoveLogicTest`, `GameTest`.

## Chạy thử
Mở app, nhấn giữ tiêu đề để vào ván demo, vuốt trái: ô HB (1024) vừa gộp nảy lên, ô mới sinh ra phóng to dần. Vuốt lên: gộp thành KS. Icon xem trong ngăn ứng dụng của emulator.

## 3–5 câu thầy có thể hỏi
1. **`ViewPropertyAnimator` là gì?** Cách gọi `view.animate()` rồi nối các thuộc tính (`scaleX`, `alpha`, `translationX`...) để chạy hiệu ứng mà không cần viết vòng lặp thời gian.
2. **Vì sao vị trí ô gộp phải đổi lại theo hướng?** Mọi hướng đều được quy về "vuốt trái" (đảo hàng hoặc chuyển vị), nên vị trí tính được thuộc bàn cờ đã xoay; phải đổi ngược lại mới ra ô thật trên màn hình.
3. **Vì sao animation không nằm trong `domain/`?** `domain/` là Kotlin thuần, không biết View; nó chỉ nói "ô nào vừa gộp", còn việc phóng to là của giao diện.
4. **Vì sao cần `previousBoard`?** Để không chạy animation khi mở màn hình hoặc xoay (bàn cờ không đổi so với lần vẽ trước).
5. **Vì sao đưa chuỗi vào `strings.xml`?** Dễ sửa một chỗ, dễ dịch sang ngôn ngữ khác, và là cách chuẩn của Android.
