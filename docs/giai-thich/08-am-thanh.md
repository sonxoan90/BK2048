# Âm thanh (SoundPool)

## Làm gì
Game có 4 hiệu ứng âm thanh ngắn, phát đúng lúc người chơi đi một nước:

| Khi nào | Âm | Ghi chú |
|---|---|---|
| Mỗi lượt vuốt làm bàn cờ đổi | tiếng **trượt** (nhỏ) | luôn phát |
| Có ô vừa gộp | tiếng **gộp** | **cao độ tăng** theo ô gộp lớn nhất: ô 4 là chuẩn, mỗi lần gấp đôi tăng 0,1 (ô 1024 → 1,8; ô 2048 → 1,9) |
| Chạm ô 2048 | tiếng **thắng** (4 nốt đi lên) | phát một lần lúc trạng thái đổi sang thắng |
| Hết nước đi | tiếng **thua** (3 nốt đi xuống) | phát một lần lúc trạng thái đổi sang thua |

Cài đặt có công tắc **Âm thanh** (mặc định bật), lưu bằng `GameStorage` nên còn sau khi tắt app. *Xóa dữ liệu* không đụng tới công tắc này.

Không sửa `domain/`. Thông tin "ô nào vừa gộp, ô nào vừa sinh" là `lastMerged` / `lastSpawn` đã có sẵn từ phần animation.

## File
| File | Vai trò |
|---|---|
| `ui/SoundManager.kt` | Bọc `SoundPool`: nạp 4 âm, `playSlide`, `playMerge(giá trị)`, `playWin`, `playLose`, `release()` |
| `ui/game/GameActivity.kt` | Tạo `SoundManager` ở `onCreate`, gọi `release()` ở `onDestroy`, quyết định khi nào phát |
| `data/GameStorage.kt` | `isSoundEnabled()` / `setSoundEnabled()` |
| `ui/settings/SettingsActivity.kt`, `activity_settings.xml` | Công tắc Âm thanh |
| `res/raw/sfx_*.ogg` | 4 file âm thanh (4–6 KB mỗi file) |
| `tools/generate_sounds.py` | Script sinh 4 file `.ogg` bằng sóng sine (cần Python, `soundfile`, `numpy`; chỉ để sinh file, app không phụ thuộc) |
| `src/test/.../SoundManagerTest.kt` | Test hàm tính cao độ `rateFor` |

## Cách hoạt động
1. **SoundPool** nạp trước các âm ngắn vào bộ nhớ nên phát gần như tức thì. Đây là lớp có sẵn trong Android, **không thêm thư viện nào**. `load()` chạy bất đồng bộ; phát khi chưa nạp xong thì chỉ im lặng.
2. **Cao độ:** `SoundPool.play(..., rate)` đổi tốc độ phát, tức là đổi cao độ, trong khoảng 0,5 đến 2,0. `rateFor(value)` tính `1 + 0,1 × (log2(value) − 2)` rồi kẹp trong khoảng đó. Vì ô gộp luôn là lũy thừa của 2, `log2` chính là `countTrailingZeroBits()`.
3. **Chỉ phát khi có một lượt đi thật.** `drawBoard` so bàn cờ với lần vẽ trước (giống điều kiện chạy animation) nên mở màn hình hay xoay màn hình không có tiếng. Ván mới và Hoàn tác cũng làm bàn cờ đổi nhưng `lastSpawn == null` (không sinh ô mới) nên cũng im lặng.
4. **Tiếng thắng/thua** phát trong `observe` của trạng thái, chỉ khi trạng thái **đổi** so với lần nhận trước (`previousState`). Lần nhận đầu tiên của một Activity mới (xoay màn hình) không phát, nên đang hiện hộp thoại thua mà xoay màn hình sẽ không kêu lại.
5. **Công tắc:** `SoundManager` nhận một hàm `isEnabled: () -> Boolean` và hỏi lại mỗi lần phát, nên bật/tắt trong Cài đặt có hiệu lực ngay mà `SoundManager` không cần biết `SharedPreferences`.
6. **Vòng đời (`// [KT Ch7]`):** `SoundPool` giữ bộ nhớ và luồng âm thanh. Tạo ở `onCreate`, **giải phóng ở `onDestroy`** (`sound.release()`). Mỗi lần xoay màn hình Activity bị hủy và tạo lại, nếu không `release()` thì các `SoundPool` cũ rò rỉ.

## Đã kiểm tra trên emulator
Emulator tắt âm nên không nghe được. Mình kiểm tra bằng log tạm ghi kết quả `pool.play()` (ID luồng khác 0 là đã phát), sau đó đã gỡ log:
- Vuốt trái gộp 512+512: phát trượt và gộp với `rate = 1,8`. Vuốt lên gộp 2048: gộp `rate = 1,9` rồi tiếng thắng.
- Ván mới, Hoàn tác, bấm *Chơi tiếp* sau khi thắng, xoay ngang rồi dọc: **không** phát gì.
- Tắt công tắc: mọi lệnh phát đều bị bỏ qua; công tắc còn tắt sau khi tắt hẳn rồi mở lại app.
- Ván thua: tiếng thua phát đúng một lần sau nước đi cuối. Tổng 193 lần phát, không crash.
- **Chưa kiểm tra bằng tai** chất lượng âm thanh và độ to nhỏ trên loa thật; các bạn nên nghe thử và cân chỉnh trong `tools/generate_sounds.py` nếu muốn.

## 3–5 câu thầy có thể hỏi
1. **SoundPool khác MediaPlayer thế nào, vì sao chọn SoundPool?** SoundPool nạp trước nhiều âm ngắn vào bộ nhớ, phát tức thì và phát chồng nhiều âm; MediaPlayer hợp nhạc nền dài, mỗi lần phát phải chuẩn bị nên trễ.
2. **Vì sao phải gọi `release()` ở `onDestroy`?** SoundPool giữ bộ nhớ và luồng âm thanh của hệ thống. Activity bị hủy mà không giải phóng thì bị rò rỉ, nhất là khi xoay màn hình tạo Activity mới liên tục.
3. **Cao độ tăng theo giá trị ô gộp bằng cách nào?** Dùng tham số `rate` của `SoundPool.play`: tốc độ phát nhanh hơn thì âm cao hơn. `rate = 1 + 0,1 × (log2(giá trị) − 2)`, kẹp trong khoảng 0,5 đến 2,0.
4. **Làm sao không phát tiếng khi xoay màn hình hay bấm Ván mới/Hoàn tác?** Chỉ phát khi bàn cờ đổi so với lần vẽ trước *và* vừa sinh một ô mới (`lastSpawn != null`). Tiếng thắng/thua chỉ phát khi trạng thái đổi so với lần nhận trước.
5. **Vì sao `SoundManager` nhận một hàm `() -> Boolean` thay vì đọc SharedPreferences?** Tách trách nhiệm: `SoundManager` chỉ lo phát âm, không biết dữ liệu lưu ở đâu. Hàm bậc cao (Chương 2) cho phép hỏi lại công tắc mỗi lần phát.
6. **File âm thanh lấy ở đâu?** Tự sinh bằng script Python (sóng sine tắt dần, mã hóa Ogg Vorbis), đặt trong `res/raw/`; không dùng âm thanh có bản quyền.
