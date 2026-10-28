# Tổng quan 2048 BK – Tài liệu ôn tập cho cả nhóm

> Đọc file này **trước tiên**, sau đó mới đọc các file `01` → `08` cho phần mình phụ trách.
> Mọi đoạn code trích dưới đây lấy nguyên từ project.

---

## 1. App làm gì (nói trong 30 giây)

**2048 BK – Đường Tới Kỹ Sư** là game giải đố 2048. Mỗi ô hiển thị một **điểm chữ thang Bách khoa**: F → D → D+ → C → C+ → B → B+ → A → A+ → HB (Học bổng) → **KS (Kỹ sư)**. Người chơi vuốt để gộp hai ô giống nhau thành ô cao hơn. Đạt ô **KS** (giá trị 2048) là thắng.

Tính năng: chơi bằng vuốt hoặc phím mũi tên, Hoàn tác 1 bước, tự lưu ván, điểm cao, Bộ sưu tập 11 mức điểm, Lịch sử ván chơi, **Âm thanh** (tiếng trượt, gộp có cao độ tăng theo ô gộp, thắng, thua), Cài đặt (tắt hiệu ứng, tắt âm thanh, xóa dữ liệu), chế độ demo.

---

## 2. Kiến trúc 3 tầng

```
┌──────────── ui/  (giao diện – phụ thuộc Android) ────────────┐
│ MenuActivity  GameActivity  CollectionActivity               │
│ HistoryActivity  SettingsActivity  SoundManager              │
│ GameViewModel (LiveData)  SwipeListener  TileStyle  Adapter  │
└──────────────┬──────────────────────────────┬────────────────┘
               │ gọi luật chơi                 │ lưu / đọc
┌──────────────▼───────────────┐  ┌───────────▼────────────────┐
│ domain/ (Kotlin THUẦN)       │  │ data/                      │
│ Board, BoardExt, MoveLogic,  │  │ GameStorage (SharedPrefs)  │
│ Game, GameState, Direction,  │  │ AppDatabase, GameRecord,   │
│ TileInfo, TileSets, Demo     │  │ GameRecordDao (Room)       │
└──────────────────────────────┘  └────────────────────────────┘
```

**Nguyên tắc vàng:** toàn bộ luật chơi nằm trong `domain/`, **không có dòng `import android.*` nào**. Nhờ vậy:
- Test được bằng JUnit (bộ 31 test, phần lớn kiểm thử `domain/`) và chạy thử bằng `main()` trong `Demo.kt`, không cần emulator.
- Giao diện không chứa luật chơi: Activity chỉ nhận thao tác và vẽ lại.

### Bản đồ file

| File | Làm gì | Ai trình bày |
|---|---|---|
| `domain/Direction.kt` | `enum` 4 hướng | TV1 |
| `domain/Board.kt` | `data class Board(cells)`; `toText()`/`fromText()` đổi qua lại với chuỗi | TV1 |
| `domain/BoardExt.kt` | extension: `transpose`, `reversedRows`, `emptyCells`, `isFull`, `maxTile`, `hasMoves` | TV1 |
| `domain/MoveLogic.kt` | **`mergeRow`** và **`move(board, direction)`** | TV1 (cả nhóm) |
| `domain/Game.kt` | giữ bàn cờ, điểm, trạng thái; `move`, `undo`, `newGame`, `load`, sinh ô | TV1 |
| `domain/GameState.kt` | `sealed class`: Playing / Won / Lost | TV1 |
| `domain/TileInfo.kt`, `TileSets.kt` | nhãn F…KS, tên, mô tả 11 mức | TV1 |
| `domain/Demo.kt` | `DEMO_BOARD` và `main()` chạy thử | TV1 |
| `ui/game/GameActivity.kt` | vẽ lưới, nhận vuốt/phím, hộp thoại, animation, vòng đời | TV2 |
| `ui/game/GameViewModel.kt` | cầu nối Activity ↔ Game qua LiveData; ghi lịch sử | TV2 |
| `ui/game/SwipeListener.kt` | đổi cử chỉ vuốt thành `Direction` | TV2 |
| `ui/TileStyle.kt` | giá trị ô → màu nền, màu chữ | TV2 |
| `ui/SoundManager.kt` | phát tiếng trượt/gộp/thắng/thua bằng `SoundPool`; `rateFor` tính cao độ; `release()` ở `onDestroy` | TV2 |
| `res/raw/sfx_*.ogg`, `tools/generate_sounds.py` | 4 file âm thanh và script Python sinh ra chúng | TV2 |
| `data/GameStorage.kt` | SharedPreferences: ván, điểm cao, ô lớn nhất, cài đặt | TV3 |
| `data/GameRecord.kt`, `GameRecordDao.kt`, `AppDatabase.kt` | Room: bảng lịch sử ván | TV3 |
| `ui/menu/MenuActivity.kt` | màn khởi động, mở GameActivity bằng Intent | TV3 |
| `ui/collection/…`, `ui/history/…` | RecyclerView: Bộ sưu tập, Lịch sử | TV3 |
| `ui/settings/SettingsActivity.kt` | bật/tắt hiệu ứng, bật/tắt âm thanh, xóa dữ liệu | TV3 |

---

## 3. Đầu vào – Xử lý – Đầu ra

| | |
|---|---|
| **Đầu vào** | Vuốt 4 hướng / phím mũi tên; nút Ván mới, Hoàn tác, Chơi tiếp; chọn mục ở Menu; dữ liệu đã lưu |
| **Xử lý** | `domain/`: trượt, gộp, cộng điểm, sinh ô, xét thắng/thua. `data/`: lưu và đọc |
| **Đầu ra** | Lưới 4×4 (nhãn + màu + số nhỏ ở góc), điểm, hiệu ứng, âm thanh, hộp thoại thắng/thua, Bộ sưu tập, Lịch sử, dữ liệu lưu bền |

---

## 4. Một lượt vuốt – AI CŨNG PHẢI NÓI ĐƯỢC

```
Vuốt / phím ←
 → SwipeListener.onFling          đổi cử chỉ thành Direction (bỏ qua nếu vuốt < 80 px)
 → GameActivity: viewModel.move(direction)
 → GameViewModel: game.move(direction)                 ← vào domain/
      1. move(board, direction)  → bàn cờ mới, điểm, moved, ô vừa gộp
      2. moved == false → dừng, KHÔNG sinh ô
      3. lưu Snapshot (cho Undo) → sinh 1 ô mới → cộng điểm → computeState()
 → ViewModel.refresh(): đặt giá trị mới cho các LiveData
 → GameActivity đang observe → drawBoard() vẽ lại 16 ô → animateMove() + playMoveSounds()
```

### 4.1 `mergeRow` – trái tim của game (`MoveLogic.kt`)

```kotlin
fun mergeRow(row: List<Int>): RowResult {
    val tiles = row.filter { it != 0 }            // 1. bỏ ô trống
    val merged = mutableListOf<Int>()
    var score = 0
    var i = 0
    while (i < tiles.size) {
        if (i + 1 < tiles.size && tiles[i] == tiles[i + 1]) {
            val value = tiles[i] * 2               // 2. gộp cặp bằng nhau
            merged.add(value); score += value
            i += 2                                 //    nhảy qua 2 ô → mỗi ô gộp tối đa 1 lần
        } else {
            merged.add(tiles[i]); i += 1           //    không gộp: giữ nguyên
        }
    }
    val newRow = merged + List(row.size - merged.size) { 0 }   // 3. bù số 0
    return RowResult(newRow, score, newRow != row, mergedAt)   // 4. moved = có thay đổi không
}
```

**Chạy tay `[2, 2, 4, 0]`** (F F D trống):

| Bước | `tiles` / `i` | Việc làm | `merged` | Điểm |
|---|---|---|---|---|
| filter | `[2,2,4]` | bỏ số 0 | `[]` | 0 |
| i = 0 | 2 == 2 | gộp → 4, `i = 2` | `[4]` | 4 |
| i = 2 | 4, không có ô kế | giữ | `[4,4]` | 4 |
| bù 0 | | | **`[4,4,0,0]`** | **4** |

Vì sao `[2,2,2,2]` ra `[4,4,0,0]` chứ không ra `[8,0,0,0]`? Vì **`i += 2`**: ô 4 vừa tạo không bao giờ được so sánh lại.

### 4.2 Bốn hướng dùng chung một hàm (`move` trong `MoveLogic.kt`)

```kotlin
val prepared = when (direction) {          // "xoay" để hướng nào cũng thành vuốt TRÁI
    Direction.LEFT  -> board
    Direction.RIGHT -> board.reversedRows()
    Direction.UP    -> board.transpose()
    Direction.DOWN  -> board.transpose().reversedRows()
}
val results = prepared.cells.map { mergeRow(it) }   // gộp từng hàng
val merged = Board(results.map { it.row })
val restored = when (direction) {           // xoay ngược lại
    Direction.LEFT  -> merged
    Direction.RIGHT -> merged.reversedRows()
    Direction.UP    -> merged.transpose()
    Direction.DOWN  -> merged.reversedRows().transpose()
}
```

**Chạy tay ván demo** (`DEMO_BOARD`, nhấn giữ tiêu đề ở Menu):

```
Ban đầu            Vuốt TRÁI                 Vuốt LÊN (cột đầu)
512 512 256 128    1024 256 128   .          2048 ...   ← KS! Thắng
1024  .   .   .    1024   .   .   .          ...
 64  32  16   8      64  32  16   8
  4   2   .   .       4   2   .   . (+1 ô mới)
```
- Vuốt trái: hàng đầu `[512,512,256,128]` → `[1024,256,128,0]`, +1024 điểm.
- Vuốt lên: cột đầu `[1024,1024,64,4]` được chuyển vị thành một hàng → `mergeRow` → `[2048,64,4,0]` → chuyển vị lại → ô 2048 ở góc trên trái.

### 4.3 Sau khi gộp: `Game.move` (`Game.kt`)

```kotlin
fun move(direction: Direction): Boolean {
    if (state is GameState.Lost) return false
    val result = move(board, direction)
    if (!result.moved) return false                 // không đổi gì → không sinh ô
    previous = Snapshot(board, score, state, hasWon)  // nhớ 1 bước cho Undo
    board = spawnTile(result.board)                 // 90% ô 2, 10% ô 4, ở ô trống ngẫu nhiên
    lastSpawn = findSpawned(result.board, board)    // cho animation
    lastMerged = result.mergedCells                 // cho animation
    score += result.score
    state = computeState()
    return true
}
```

---

## 5. Trạng thái ván (`GameState` + `computeState`)

```
             có ô ≥ 2048 lần đầu (hasWon = false → true)
 Playing ─────────────────────────────────────────────► Won ──"Chơi tiếp"──► computeState()
    │                                                    │                     (Playing hoặc Lost)
    │ hết ô trống VÀ không còn cặp kề bằng nhau          └──"Ván mới"──► newGame()
    ▼
  Lost ──"Hoàn tác"──► quay về trạng thái trước     "Ván mới" ──► newGame()
```

- **Thắng chỉ báo 1 lần** nhờ cờ `hasWon`.
- **`hasMoves()`**: còn ô trống thì còn đi được; bàn cờ đầy thì duyệt từng ô, xem ô bên phải hoặc bên dưới có bằng nó không.
- **Chơi tiếp sau khi thắng** gọi lại `computeState()` chứ không đặt cứng `Playing`. Đây là lỗi đã từng gặp: nếu lúc chạm 2048 bàn cờ đã hết nước đi thì ván bị kẹt.
- **Hai ô KS gộp nhau** → ô **4096**, +4096 điểm, không hiện hộp thoại nữa. `TileSets.infoFor(4096)` trả về nhãn chính là số "4096", màu chung `tile_super`; cỡ chữ tự giảm còn 18sp khi nhãn dài từ 4 ký tự.

---

## 6. Tầng giao diện: ViewModel, LiveData, vòng đời

**Vì sao cần ViewModel?** Khi xoay màn hình, Android **hủy và tạo lại Activity**. Nếu `Game` nằm trong Activity thì ván sẽ mất. `GameViewModel` sống qua việc xoay, nên `by viewModels()` trả lại đúng đối tượng cũ.

```kotlin
private val _board = MutableLiveData(game.board)   // private: chỉ ViewModel được sửa
val board: LiveData<Board> = _board                // public: Activity chỉ đọc

// Trong GameActivity
viewModel.board.observe(this) { drawBoard(it) }    // LiveData đổi → tự vẽ lại
```

**Vòng đời GameActivity** (xem Logcat, lọc tag `GameActivity`):

| Callback | Việc làm trong app |
|---|---|
| `onCreate` | tạo binding, tạo `SoundManager`, `viewModel.start(...)` nạp ván theo chế độ, tạo 16 ô, gắn vuốt, `observe` |
| `onPause` | **`saveProgress()`**: lưu ván, điểm cao, ô lớn nhất; ván thua thì xóa bản lưu và ghi lịch sử |
| `onDestroy` | đóng hộp thoại đang mở để tránh rò rỉ cửa sổ; **`sound.release()`** giải phóng `SoundPool` |

Tại sao lưu ở `onPause` mà không phải `onDestroy`? Vì sau `onPause` hệ thống có thể tắt app bất kỳ lúc nào, và `onDestroy` không được đảm bảo sẽ chạy.

**Animation** chỉ chạy khi `previousBoard != board`, nên mở màn hình hay xoay màn hình không có hiệu ứng. Ô gộp phóng 1.15 lần rồi thu về; ô mới phóng từ 0 lên; mỗi hiệu ứng 120 ms.

**Âm thanh** (`ui/SoundManager.kt`, `SoundPool`): chỉ phát cùng lúc với animation, tức là khi bàn cờ đổi sau một lượt đi thật (`lastSpawn != null`, nên Ván mới và Hoàn tác im lặng).
- Mọi lượt vuốt có tiếng **trượt**; có ô gộp thì thêm tiếng **gộp** với cao độ `rate = 1 + 0,1 × (log2(ô gộp lớn nhất) − 2)`, kẹp trong khoảng 0,5 đến 2,0 (ô 1024 → 1,8; ô 2048 → 1,9).
- Tiếng **thắng/thua** phát khi `state` đổi so với `previousState`, nên xoay màn hình lúc đang hiện hộp thoại không kêu lại.
- Công tắc **Âm thanh** trong Cài đặt: `SoundManager` nhận hàm `isEnabled: () -> Boolean` và hỏi lại mỗi lần phát.
- `SoundPool` giữ bộ nhớ nên phải tạo ở `onCreate` và `release()` ở `onDestroy`. Chi tiết xem `08-am-thanh.md`.

---

## 7. Lưu trữ

### SharedPreferences (`GameStorage.kt`): file `bk2048_prefs`

| Key | Kiểu | Ý nghĩa |
|---|---|---|
| `board` | String `"2,0,4,…"` | bàn cờ: `flatten().joinToString(",")`, đọc lại bằng `split` + `toIntOrNull` + `chunked(4)` |
| `score`, `has_won` | Int, Boolean | điểm ván đang chơi, đã thắng chưa |
| `high_score`, `max_tile` | Int | chỉ ghi khi **lớn hơn** giá trị cũ |
| `animation_enabled`, `sound_enabled` | Boolean | cài đặt hiệu ứng và âm thanh, mặc định bật (giữ lại khi xóa dữ liệu) |

Chuỗi hỏng thì `Board.fromText` trả về `null` → app bỏ qua và mở ván mới, không crash.

### Room (`data/`): bảng lịch sử `game_records`

| Thành phần | Vai trò |
|---|---|
| `@Entity GameRecord` | một dòng: `id` tự tăng, `score`, `maxTile`, `won`, `playedAt` |
| `@Dao GameRecordDao` | `insert`, `getAll` (mới nhất trước), `deleteAll`, đều là `suspend` |
| `@Database AppDatabase` | một bản duy nhất cho cả app (`instance ?: synchronized…`) |

Ghi một ván khi: bấm Ván mới (ván cũ có điểm), hoặc rời màn hình khi đã thua. Cờ `recorded` chống ghi hai lần. Ghi bằng `viewModelScope.launch` ở luồng nền nên giao diện không bị đơ.

---

## 8. Các màn phụ

| Màn | Đầu vào | Xử lý | Đầu ra |
|---|---|---|---|
| **Menu** | nút bấm | `Intent(this, GameActivity::class.java).putExtra("mode", …)` với `new` / `continue` / `demo`. Ván mới khi đang có ván lưu thì hỏi xác nhận | mở màn chơi đúng chế độ |
| **GameActivity** đọc Intent | `getStringExtra("mode")` | `chooseStartGame()`: `new` xóa ván lưu; `demo` nạp `DEMO_BOARD` (5000 điểm); còn lại nạp ván lưu. Nếu Activity được tạo lại thì luôn dùng ván lưu | |
| **Bộ sưu tập** | `maxTile` đã lưu | RecyclerView + `TileAdapter`: mức có `value <= maxTile` thì mở | 11 dòng, mức chưa đạt hiện "???" |
| **Lịch sử** | bảng Room | `lifecycleScope.launch { dao.getAll() }` | danh sách ván |
| **Cài đặt** | công tắc, nút Xóa | lưu cài đặt; xóa SharedPreferences + `deleteAll()` sau khi xác nhận | |

Menu cập nhật điểm cao và nút *Chơi tiếp* ở **`onResume`**, vì khi quay lại từ màn chơi thì `onCreate` không chạy lại.

---

## 9. Bản đồ kiến thức giáo trình trong code

Tìm toàn bộ chỗ đánh dấu (60 chỗ): `grep -r "\[KT" app/src`

| Chương | Kiến thức | Ví dụ trong code |
|---|---|---|
| 1.4 | Null safety `?:`, `?.let` | `prefs.getString(KEY_BOARD, null) ?: return null`; `saved?.let { game.load(...) }` |
| 1.5 | `when`, vòng lặp, range | `when (direction)` trong `move`; `for (r in 0 until size)` trong `hasMoves` |
| 1.6 | List 2 chiều | `Board(val cells: List<List<Int>>)` |
| 2.2 | Tham số mặc định | `Board.empty(size: Int = 4)`, `newGame(size: Int = board.size)` |
| 2.3 | Hàm rút gọn | `fun Board.isFull() = emptyCells().isEmpty()` |
| 2.4 | Lambda, hàm bậc cao | `cells.map { mergeRow(it) }`; `SwipeListener(onSwipe: (Direction) -> Unit)` |
| 2.5 | `filter`, `maxOrNull`, `flatMapIndexed` | `row.filter { it != 0 }`; `cells.flatten().maxOrNull() ?: 0` |
| 3.1 | Lớp, constructor, property `private set` | `class Game(size, random)` |
| 3.2 | Kế thừa | `SwipeListener : GestureDetector.SimpleOnGestureListener()` |
| 3.3 | Extension function | `fun Board.transpose()`, `fun Board.hasMoves()` |
| 3.4 | data class, enum, sealed class, object | `Board`, `Direction`, `GameState`, `TileSets` |
| 3.5 | Package | `domain` / `data` / `ui` |
| 4 | Activity, Intent, resources, View Binding | `MenuActivity`, `strings.xml`, `ActivityGameBinding` |
| 5 | ConstraintLayout, RecyclerView, GridLayout | `TileAdapter`, lưới 4×4 |
| 7 | Vòng đời | `Log.d` ở 6 callback; lưu ở `onPause`; tạo `SoundPool` ở `onCreate`, `release()` ở `onDestroy` |
| 8 | ViewModel, LiveData | `GameViewModel`, `observe` |
| 9 | Room, Coroutines | `@Entity`, `@Dao`, `suspend`, `viewModelScope` |

---

## 10. Các tình huống đặc biệt (thầy hay hỏi)

| Tình huống | App xử lý thế nào |
|---|---|
| Vuốt mà không ô nào di chuyển | `moved = false` → không sinh ô, không lưu Undo, không cộng điểm |
| Bàn cờ đầy nhưng còn hai ô kề bằng nhau | **Chưa thua**, vì `hasMoves()` vẫn trả `true` |
| Vừa chạm 2048 vừa hết nước đi | Hiện hộp thoại thắng; bấm Chơi tiếp → `computeState()` → hộp thoại thua |
| Hai ô KS gộp | Ô 4096, nhãn là số, màu `tile_super`, không báo thắng lại |
| Xoay màn hình | Activity tạo lại, ViewModel giữ nguyên ván; layout ngang riêng trong `layout-land/` |
| Tắt hẳn app | Ván đã lưu ở `onPause`; mở lại có nút *Chơi tiếp* |
| Thua rồi bấm Hoàn tác | Quay lại nước trước khi thua (Undo 1 bước) |
| Chuỗi lưu bị hỏng | `fromText` trả `null` → mở ván mới, không crash |
| Bấm Back khi có hộp thoại | Đóng hộp thoại và về Menu; ván vẫn được lưu |
| Ván mới, Hoàn tác, xoay màn hình | Không phát tiếng (chỉ phát khi có lượt đi thật) |
| Tắt công tắc Âm thanh | Mọi lệnh phát bị bỏ qua ngay; cài đặt còn sau khi tắt app và sau Xóa dữ liệu |

---

## 11. Tự kiểm tra (che cột phải, tự trả lời)

| # | Câu hỏi | Ý chính của câu trả lời |
|---|---|---|
| 1 | Một lượt vuốt đi qua những lớp nào? | SwipeListener → GameActivity → GameViewModel → Game → move/mergeRow → LiveData → drawBoard |
| 2 | `mergeRow([4,4,8,8])` ra gì? Điểm? | `[8,16,0,0]`, điểm 24 |
| 3 | Vì sao 4 hướng dùng chung một hàm? | Đảo hàng / chuyển vị để quy về vuốt trái, gộp xong làm ngược lại |
| 4 | Khi nào sinh ô mới? Xác suất? | Chỉ khi bàn cờ đổi; 90% ô 2, 10% ô 4, ở ô trống ngẫu nhiên |
| 5 | Điều kiện thua? | Không ô trống **và** không cặp kề bằng nhau |
| 6 | Vì sao `Game` nhận `Random` qua constructor? | Test truyền `Random(seed)` để kết quả lặp lại được |
| 7 | Vì sao `domain/` không import Android? | Test bằng JUnit và `main()` không cần emulator; tách luật khỏi giao diện |
| 8 | `data class` có lợi gì cho `Board`? | Tự có `equals` (so `==`), `copy`, `toString`; bất biến nên lưu Undo an toàn |
| 9 | `sealed class` khác `enum`? | Mỗi nhánh có thể mang dữ liệu riêng; `when` đủ nhánh không cần `else` |
| 10 | Xoay màn hình sao không mất ván? | ViewModel sống qua việc tạo lại Activity |
| 11 | `_board` và `board` khác nhau? | Mutable private để ViewModel sửa; LiveData public để Activity chỉ đọc |
| 12 | Lưu ở đâu, khi nào? | SharedPreferences ở `onPause`; điểm cao và ô lớn nhất lưu ngay khi tăng |
| 13 | Room gồm mấy phần? | `@Entity`, `@Dao`, `@Database` |
| 14 | Vì sao hàm DAO là `suspend`? | Truy cập database chậm, chạy ở luồng nền trong coroutine để không đơ giao diện |
| 15 | Undo hoạt động sao, vì sao chỉ 1 bước? | Mỗi lượt lưu một `Snapshot` của lượt trước; Undo dùng rồi xóa nó |
| 16 | Menu gửi chế độ chơi sang màn chơi thế nào? | `Intent.putExtra("mode", …)`, bên kia `getStringExtra` |
| 17 | RecyclerView có 3 hàm bắt buộc nào? | `onCreateViewHolder`, `onBindViewHolder`, `getItemCount` |
| 18 | App viết bằng Java hay Kotlin? | 100% Kotlin; thư mục `java/` chỉ là tên mặc định của Android |
| 19 | Vì sao dùng `SoundPool` và vì sao phải `release()` ở `onDestroy`? | Nạp trước âm ngắn, phát tức thì, phát chồng nhau; nó giữ bộ nhớ và luồng âm thanh nên Activity bị hủy (xoay màn hình, thoát) mà không giải phóng sẽ rò rỉ |
| 20 | Cao độ tiếng gộp thay đổi bằng cách nào? | Tham số `rate` của `SoundPool.play`: tốc độ phát nhanh hơn thì âm cao hơn; `rate` tăng 0,1 mỗi lần ô gộp gấp đôi |

---

## 12. Lộ trình ôn (khoảng 3 giờ mỗi người)

1. **Cả nhóm (45 phút):** đọc file này, chạy `Demo.kt` (mũi tên xanh cạnh `fun main()`), chạy bộ 31 test (chuột phải `app/src/test` → Run Tests).
2. **Cả nhóm (30 phút):** mở `MoveLogic.kt`, mỗi người tự chạy tay `mergeRow` với `[2,2,2,2]` và `[0,4,4,4]` ra giấy, rồi giải thích cho hai bạn còn lại.
3. **Theo phần (1,5 giờ):** TV1 đọc `01`; TV2 đọc `02`, `05`, `08`; TV3 đọc `03`, `04`, `06`. Ai cũng đọc `07`.
4. **Cả nhóm (15 phút):** hỏi chéo bằng bảng tự kiểm tra ở mục 11.
