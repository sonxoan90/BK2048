# 2048 BK — Đường Tới Kỹ Sư

Đồ án môn **IT4785 – Lập trình di động**. Game giải đố 2048 trên Android, các ô hiển thị **điểm chữ theo thang điểm Bách khoa (F → A+)**, đích đến là tấm bằng Kỹ sư.

> Slogan: *"Vuốt là qua môn, gộp là ra trường."*

---

## 1. Bối cảnh (Claude Code đọc kỹ trước khi viết code)

- Môn học diễn ra 21/10/2026 – 03/11/2026, mỗi tối 18:30–20:45. **Báo cáo trước giáo viên: 03/11/2026.** Nhóm 2–3 người.
- Yêu cầu của giáo viên: một app Android chạy được, áp dụng kiến thức đã học, được dùng AI hỗ trợ.
- Giáo trình (13 chương): Kotlin cơ bản → Hàm → Lớp & đối tượng → App Android đầu tiên → Layout (ConstraintLayout, Data Binding, RecyclerView) → Navigation/Fragment → Lifecycle → ViewModel/LiveData → Room/Coroutines → RecyclerView nâng cao → Internet.
- Tiến độ trên lớp: tới 02/11 dự kiến chỉ học hết **Chương 3** và phần đầu **Chương 4**.
- **Cách làm của nhóm:** AI viết code, nhưng **mọi thành viên phải hiểu và trả lời được khi thầy hỏi**. Vì vậy:
  - Code phải **đơn giản, rõ ràng, dễ giải thích**. Không dùng kỹ thuật "khoe".
  - Kiến thức Kotlin của Chương 1–3 phải thể hiện rõ, vì đây là phần chấm chính.
  - Phần Android là phần nhóm tự học. **Được phép dùng kiến thức chưa dạy** (đã chốt), nhưng chỉ ở mức có trong giáo trình (ViewModel/LiveData Ch8, Room Ch9) và phải giải thích được.
- **Cách làm việc:** chỉ **một máy** (máy của trưởng nhóm) chạy Claude Code để build. Cả 3 thành viên cùng học, đọc code và đọc tài liệu giải thích sau mỗi giai đoạn. Code được push lên GitHub để mọi người xem và chạy được.

## 2. Stack (đã chốt; không tự ý đổi, muốn đổi thì phải hỏi trước)

| Hạng mục | Lựa chọn |
|---|---|
| Ngôn ngữ | Kotlin |
| Build | Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) |
| SDK | `minSdk 26`, `compileSdk`/`targetSdk` theo bản mới nhất mà Android Studio tạo ra |
| Package | `vn.edu.hust.bk2048` |
| Tên hiển thị | `2048 BK` |
| Giao diện | **XML Views + ConstraintLayout + View Binding**. Dùng Material Components (MaterialButton, MaterialCardView, MaterialAlertDialog) |
| Màn hình | **Nhiều Activity + Intent** |
| Tầng giao diện | **ViewModel + LiveData** cho GameActivity (Ch8): giữ ván khi xoay màn hình; Activity chỉ `observe` rồi vẽ lại |
| Danh sách | RecyclerView (màn Bộ sưu tập) |
| Lưới game | `GridLayout` 4×4 |
| Điều khiển | `GestureDetector` (`onFling`) để vuốt + **phím mũi tên** (`onKeyDown`, tiện khi demo trên emulator) |
| Lưu trữ | **SharedPreferences**. Bàn cờ lưu dạng chuỗi `"2,0,4,…"` bằng `joinToString`/`split`, không cần Gson |
| Test | JUnit 4 (unit test cho `domain/`) + file `Demo.kt` có hàm `main()` để chạy thử logic |

| Mở rộng (GĐ 6) | **Room** lưu lịch sử các ván đã chơi (Ch9), chỉ làm khi phần chính đã xong |

**Dependency được phép thêm ngoài template:** `androidx.lifecycle:lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`, `androidx.activity:activity-ktx` (cho `by viewModels()`), và Room + KSP (chỉ ở GĐ 6). Muốn thêm bất cứ thứ gì khác thì phải hỏi nhóm.

**KHÔNG dùng:** Jetpack Compose, Navigation Component, Fragment, Hilt/Koin/Dagger, Flow, RxJava, thư viện game hay animation bên ngoài.

Môi trường: máy Windows, Android Studio bản mới nhất. Lệnh build dùng `gradlew.bat`, hoặc `./gradlew` nếu chạy trong Git Bash.

## 3. Luật chơi

- Bàn cờ 4×4. Đầu ván sinh sẵn 2 ô.
- Vuốt theo 4 hướng: mọi ô trượt hết về phía đó; hai ô **cùng giá trị** chạm nhau thì gộp thành một ô có giá trị gấp đôi.
- Trong một lượt vuốt, mỗi ô **chỉ được gộp tối đa 1 lần**. Ví dụ hàng `[2,2,2,2]` vuốt sang trái cho ra `[4,4,0,0]`, không ra `[8,0,0,0]`. Hàng `[2,2,4,0]` cho ra `[4,4,0,0]`.
- Sau một lượt **có làm thay đổi bàn cờ**, sinh thêm 1 ô ở vị trí trống ngẫu nhiên: 90% là ô 2, 10% là ô 4. Lượt vuốt không làm thay đổi gì thì **không** sinh ô.
- Điểm: mỗi lần gộp được cộng giá trị của ô mới.
- **Thắng:** xuất hiện ô 2048. Hiện hộp thoại cho chọn "Chơi tiếp" hoặc "Ván mới". Chỉ báo thắng một lần mỗi ván.
- **Thua:** không còn ô trống **và** không còn hai ô kề nhau nào bằng nhau.
- Hoàn tác (Undo): được quay lại **1 bước**. Đây là tính năng mở rộng.

## 4. Chủ đề ô: "Điểm chữ Bách khoa"

Trên ô chỉ hiện **nhãn ngắn (tối đa 2 ký tự)**, chữ to, kèm **số nhỏ ở góc** để người chơi biết giá trị. Tên đầy đủ và mô tả chỉ hiện trong màn Bộ sưu tập.

| Giá trị | Nhãn trên ô | Tên đầy đủ (Bộ sưu tập) | Mô tả (Bộ sưu tập) |
|---|---|---|---|
| 2 | **F** | Điểm F | Ai rồi cũng từng bắt đầu từ đây |
| 4 | **D** | Điểm D | Qua môn trong gang tấc |
| 8 | **D+** | Điểm D+ | Đã bớt run khi xem điểm |
| 16 | **C** | Điểm C | Trung bình nhưng không tầm thường |
| 32 | **C+** | Điểm C+ | Bắt đầu có phong độ |
| 64 | **B** | Điểm B | Khá rồi đấy |
| 128 | **B+** | Điểm B+ | Gần chạm tới giỏi |
| 256 | **A** | Điểm A | Giỏi thật sự |
| 512 | **A+** | Điểm A+ | Điểm tuyệt đối |
| 1024 | **HB** | Học bổng | Thành tích được ghi nhận |
| 2048 | **KS** | Kỹ sư Bách khoa | Tấm bằng trong tay. Chiến thắng! |
| 4096+ | *(hiện số)* | Vượt giới hạn | Dùng chung một kiểu hiển thị |

Thứ tự F < D < D+ < C < C+ < B < B+ < A < A+ theo đúng thang điểm chữ của trường. Màu nền chuyển dần từ đỏ/xám (F) qua vàng (C) đến xanh lá (A+), riêng HB và KS dùng màu vàng gold nổi bật.

**Hình ảnh:**
- Mặc định **chỉ dùng chữ + màu nền**, không cần ảnh.
- Mở rộng (nếu nhóm muốn): thêm ảnh chibi theo quy ước tên file `res/drawable/tile_grade_<giá trị>.webp`, ví dụ `tile_grade_2.webp`. Ảnh vuông 256×256, hiển thị trong Bộ sưu tập.
- Code ánh xạ giá trị sang ảnh bằng một `Map`/`when` khai báo tường minh. **Không dùng `getIdentifier`.** Nếu chưa có ảnh thì chỉ hiện nhãn chữ + màu.
- **Không dùng logo chính thức của trường và không dùng ảnh người thật** (giáo viên…).

## 5. Màn hình

1. **MenuActivity** (màn khởi động): tên game và slogan, các nút *Chơi tiếp* (chỉ hiện khi có ván đang lưu), *Ván mới*, *Bộ sưu tập*, *Cài đặt*. Hiển thị điểm cao nhất.
2. **GameActivity**: điểm hiện tại, điểm cao nhất, lưới 4×4, các nút *Ván mới* và *Hoàn tác* (khi đã làm Undo). Bắt cử chỉ vuốt trên vùng lưới. Hộp thoại thắng và thua. Tự lưu ván khi `onPause`.
3. **CollectionActivity**: RecyclerView liệt kê 11 mức điểm. Mức đã đạt hiện ô, tên đầy đủ và mô tả; mức chưa đạt hiện ô khóa "???".
4. **SettingsActivity** (mở rộng): xóa dữ liệu (có xác nhận), bật/tắt hiệu ứng.
5. **Chế độ demo** (để báo cáo): trong Menu, **nhấn giữ vào tiêu đề game** để mở ván dựng sẵn gần thắng (ví dụ đã có 1024, 512, 256…). Như vậy khi thuyết trình chỉ cần vài nước là tới 2048.

## 6. Kiến trúc & cấu trúc thư mục

```
vn.edu.hust.bk2048
├── domain/                 // Kotlin THUẦN — không import android.*; test được bằng JUnit và main()
│   ├── Direction.kt        // enum class Direction { LEFT, RIGHT, UP, DOWN }
│   ├── Board.kt            // data class Board(val cells: List<List<Int>>), các hàm tiện ích
│   ├── BoardExt.kt         // extension: transpose(), reversedRows(), emptyCells(), maxTile()
│   ├── MoveLogic.kt        // mergeRow(row): RowResult; move(board, direction): MoveResult
│   ├── GameState.kt        // sealed class GameState { Playing, Won, Lost }
│   ├── Game.kt             // class Game: giữ board, score, state; move(), newGame(), undo(); nhận Random qua constructor để test được
│   ├── TileInfo.kt         // data class TileInfo(value, label, title, description)
│   ├── TileSets.kt         // object TileSets { val grades: List<TileInfo>; fun infoFor(value): TileInfo }
│   └── Demo.kt             // fun main(): in bàn cờ ra console, thử vài nước đi
├── data/
│   └── GameStorage.kt      // SharedPreferences: lưu/đọc ván, điểm cao, ô lớn nhất từng đạt, bộ ô đang chọn
└── ui/
    ├── menu/MenuActivity.kt
    ├── game/GameActivity.kt, GameViewModel.kt (LiveData<Board>, LiveData<Int> score, LiveData<GameState>), SwipeListener.kt
    ├── collection/CollectionActivity.kt, TileAdapter.kt
    ├── settings/SettingsActivity.kt
    └── TileStyle.kt        // ánh xạ giá trị ô → màu nền, màu chữ, drawable (phần phụ thuộc Android)
```

Nguyên tắc:
- **Toàn bộ luật chơi nằm trong `domain/`.** Luồng xử lý: Activity nhận cử chỉ → gọi `viewModel.move(dir)` → ViewModel gọi `game.move(dir)` rồi cập nhật LiveData → Activity `observe` và vẽ lại.
- Cả 4 hướng **dùng chung một hàm `mergeRow`**. Hướng phải thì đảo hàng; hướng lên/xuống thì chuyển vị (transpose) ma trận. Không viết 4 phiên bản logic riêng.
- Ưu tiên dữ liệu bất biến (`List`, `copy()`), riêng `Game` giữ trạng thái hiện tại.

## 7. Kiến thức Kotlin phải xuất hiện (và đánh dấu trong code)

Mỗi chỗ minh họa kiến thức thêm một comment ngắn **tiếng Việt** có thẻ `// [KT ChX.Y]`, để nhóm `grep` ra làm slide. Ví dụ:
```kotlin
// [KT Ch2.5] filter bỏ các ô trống trước khi gộp
val tiles = row.filter { it != 0 }
```

| Kiến thức | Dùng ở đâu |
|---|---|
| val/var, kiểu dữ liệu, null safety (Ch1) | Board, GameStorage (`getString(...) ?: ...`) |
| `if`/`when`, vòng lặp, ranges (Ch1.5) | mergeRow, kiểm tra thua |
| List, MutableList, mảng 2 chiều (Ch1.6) | Board |
| Hàm, tham số mặc định, named arguments (Ch2.2) | `newGame(size: Int = 4)`, `spawnTile(random = ...)` |
| Hàm rút gọn / single-expression (Ch2.3) | `fun isFull() = emptyCells().isEmpty()` |
| Lambda, hàm bậc cao (Ch2.4) | `map`, `any`, `forEachIndexed`, callback vuốt `(Direction) -> Unit` |
| `filter`/`map`/`flatten`/`maxOrNull` (Ch2.5) | mergeRow, maxTile, emptyCells |
| Lớp, constructor, property (Ch3.1) | Game |
| Kế thừa / interface (Ch3.2) | `SwipeListener : GestureDetector.SimpleOnGestureListener()` |
| Extension function (Ch3.3) | BoardExt.kt |
| data class, enum, sealed class, object (Ch3.4) | Board/TileInfo, Direction, GameState, TileSets |
| Tổ chức package (Ch3.5) | domain / data / ui |
| Activity, resources, xử lý sự kiện (Ch4) | các Activity, `strings.xml`, `colors.xml` |
| ConstraintLayout, RecyclerView (Ch5) | layout các màn, CollectionActivity |
| Vòng đời Activity (Ch7) | lưu ván ở `onPause`, ghi `Log.d` các callback vòng đời trong GameActivity |
| ViewModel, LiveData (Ch8) | GameViewModel; demo xoay màn hình mà không mất ván |
| Room, Coroutines (Ch9, mở rộng) | Lịch sử ván chơi |

## 8. Test bắt buộc (`app/src/test/`)

`MoveLogicTest`:
- `[0,0,0,0]` → không đổi, `moved = false`
- `[2,0,2,0]` → `[4,0,0,0]`, điểm 4
- `[2,2,2,2]` → `[4,4,0,0]`, điểm 8
- `[2,2,4,0]` → `[4,4,0,0]` (không gộp tiếp thành 8)
- `[4,4,8,8]` → `[8,16,0,0]`
- `[2,4,8,16]` → không đổi
- Đúng kết quả cho cả 4 hướng trên cùng một bàn cờ mẫu

`GameTest` (dùng `Random(seed)` cố định):
- Nước đi không làm thay đổi bàn cờ thì không sinh ô mới
- Ô mới chỉ sinh ở vị trí trống
- Nhận ra thắng khi có ô 2048; thua khi bàn cờ đầy và không còn cặp kề bằng nhau
- Bàn cờ đầy nhưng còn cặp kề bằng nhau thì **chưa** thua
- Undo trả về đúng bàn cờ và điểm của lượt trước

`GameStorageTest` (hoặc test trên hàm chuyển đổi thuần): chuyển bàn cờ thành chuỗi rồi đọc lại phải ra đúng bàn cờ ban đầu.

## 9. Kế hoạch xây dựng theo giai đoạn

**Quy tắc cho mọi giai đoạn:** làm xong giai đoạn nào thì phải **build thành công** (`gradlew assembleDebug`) và **test pass** (`gradlew testDebugUnitTest`), sau đó commit với message rõ ràng. Rồi viết file giải thích `docs/giai-thich/<số>-<tên>.md`, khoảng 1 trang tiếng Việt: giai đoạn này làm gì, file nào, hàm quan trọng hoạt động ra sao, và **3–5 câu thầy có thể hỏi kèm câu trả lời**. Dừng lại báo cáo, chờ nhóm xác nhận rồi mới sang giai đoạn tiếp theo.

| GĐ | Nội dung | Tiêu chí hoàn thành |
|---|---|---|
| 0 | Kiểm tra project (do nhóm tạo bằng Android Studio, template **Empty Views Activity**); bật View Binding; tạo cấu trúc package; `.gitignore` | App trống chạy được trên emulator |
| 1 | `domain/` đầy đủ + `Demo.kt` + toàn bộ test ở mục 8 | Test pass; chạy `main()` in ra được bàn cờ |
| 2 | GameActivity + GameViewModel: vẽ lưới 4×4 (nhãn F…KS + màu nền + số nhỏ ở góc), điểm, vuốt 4 hướng + phím mũi tên, ván mới, hộp thoại thắng/thua | Chơi được trọn một ván trên emulator; xoay màn hình không mất ván |
| 3 | GameStorage: lưu và khôi phục ván, điểm cao, ô lớn nhất | Tắt app mở lại vẫn chơi tiếp được |
| 4 | MenuActivity + CollectionActivity + chế độ demo (nhấn giữ tiêu đề) | Đi qua đủ các màn; Bộ sưu tập khóa/mở đúng |
| 5 | Hoàn thiện: animation đơn giản (`ViewPropertyAnimator` phóng nhẹ ô mới/ô vừa gộp), màu sắc, icon app, toàn bộ chuỗi trong `strings.xml` (tiếng Việt) | Giao diện gọn gàng, không còn chuỗi viết cứng trong code |
| 6 | Mở rộng nếu còn thời gian, theo thứ tự ưu tiên: Undo → màn Lịch sử ván chơi bằng Room → SettingsActivity | Không làm hỏng các tính năng đã có |

## 10. Phân công

Code do Claude Code viết trên máy trưởng nhóm. Mỗi thành viên **phụ trách hiểu sâu và trình bày** một phần (phải đọc kỹ file `docs/giai-thich/` tương ứng và tự chạy thử):

- **Thành viên 1 – Logic game:** `domain/` + test (GĐ 1). Trình bày thuật toán `mergeRow`, 4 hướng vuốt, điều kiện thắng/thua, và phần Kotlin Ch1–3.
- **Thành viên 2 – Màn chơi:** GameActivity, GameViewModel/LiveData, vuốt và phím, hộp thoại, animation (GĐ 2, 5). Trình bày luồng UI → ViewModel → domain, vòng đời Activity.
- **Thành viên 3 – Lưu trữ & các màn phụ:** GameStorage, Menu, Bộ sưu tập (RecyclerView), mở rộng (GĐ 3, 4, 6). Trình bày SharedPreferences, RecyclerView, điều hướng bằng Intent.

**Ai cũng phải giải thích được hàm `mergeRow`** và luồng đi của một lượt vuốt.

## 11. Lịch

| Ngày | Build (Claude Code) | Cả nhóm học |
|---|---|---|
| 28/10 | GĐ 0–1 | Đọc `domain/`, chạy `Demo.kt` và test |
| 29/10 | GĐ 2 | ViewModel/LiveData, GestureDetector |
| 30/10 | GĐ 3 | SharedPreferences, vòng đời Activity |
| 31/10 | GĐ 4 | RecyclerView, Intent |
| 01/11 | GĐ 5 (+6 nếu kịp) | Ôn tập theo các file giải thích, tập trả lời câu hỏi |
| 02/11 | Sửa lỗi, kiểm tra trên emulator | Làm slide, tập thuyết trình, chạy thử toàn bộ kịch bản demo |
| 03/11 | Báo cáo | |

## 12. Các quyết định đã chốt

- **Được dùng kiến thức chưa dạy:** CÓ, dùng ViewModel/LiveData ở phần chính, Room ở phần mở rộng. Sau này có thể xin phép nghiên cứu thêm.
- **Demo trên emulator.** Trước buổi báo cáo phải kiểm tra emulator khởi động nhanh (bật Quick Boot), điều khiển được bằng phím mũi tên và chế độ demo (nhấn giữ tiêu đề) hoạt động.
- **Build:** một máy duy nhất chạy Claude Code; nhóm 3 người cùng học (xem mục 10).
- **Ảnh chibi:** không bắt buộc; ô chỉ dùng chữ + màu.
