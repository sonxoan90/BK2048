# Chuẩn bị báo cáo – 2048 BK: Đường Tới Kỹ Sư

Báo cáo trước giáo viên: **03/11/2026**. Tài liệu này gồm: (1) tóm tắt dự án, (2) kịch bản demo, (3) câu hỏi thầy có thể hỏi theo từng thành viên, (4) hướng dẫn push lên GitHub, (5) danh sách kiểm tra trước giờ báo cáo.

> Slogan: *"Vuốt là qua môn, gộp là ra trường."*

---

## 1. Tóm tắt dự án (30 giây mở đầu)

- Game giải đố 2048 trên Android, ô hiển thị **điểm chữ thang Bách khoa**: F → D → D+ → C → C+ → B → B+ → A → A+ → HB (học bổng) → **KS (Kỹ sư)**. Đạt ô KS là thắng.
- Kotlin + XML Views, nhiều Activity, ViewModel/LiveData, SharedPreferences, RecyclerView, Room.
- **Toàn bộ luật chơi nằm trong `domain/`** (Kotlin thuần, không `import android.*`), được kiểm thử bằng bộ 31 unit test. Giao diện chỉ gọi và vẽ lại.
- Quy mô: 29 file Kotlin, 60 chỗ đánh dấu kiến thức `// [KT ChX.Y]` (tìm bằng `grep -r "\[KT" app/src`).
- Tính năng: chơi 2048, vuốt và phím mũi tên, Hoàn tác 1 bước, tự lưu ván, điểm cao, Bộ sưu tập (khóa/mở), Lịch sử ván (Room), Âm thanh (SoundPool, cao độ tăng theo ô gộp), Cài đặt (tắt hiệu ứng, tắt âm thanh, xóa dữ liệu), chế độ demo.

Luồng một lượt vuốt (ai cũng phải nói được):

```
Vuốt/phím → GameActivity → viewModel.move(dir) → game.move(dir) [domain]
          → ViewModel cập nhật LiveData → Activity observe → vẽ lại lưới
```

---

## 2. Kịch bản demo (khoảng 8 phút)

### Chuẩn bị trước (làm ở nhà, không làm trên lớp)
1. Bật emulator với **Quick Boot**, kiểm tra khởi động nhanh. Mở sẵn app một lần cho "ấm máy".
2. Vào **Cài đặt → Xóa dữ liệu → Xóa** để bắt đầu từ trạng thái sạch (Bộ sưu tập sẽ toàn dấu `?`, tạo tương phản đẹp khi mở khóa).
3. Quay sẵn **một video màn hình** toàn bộ kịch bản (thanh công cụ emulator → quay màn hình) để dự phòng nếu máy lỗi.
4. Đóng ứng dụng khác, bật bàn phím máy tính để dùng **phím mũi tên** điều khiển emulator.
5. **Bật âm thanh:** chạy emulator **không** có tùy chọn `-no-audio`, tăng âm lượng máy tính và thử một nước đi để chắc chắn nghe được tiếng.

### Các bước (người trình bày gợi ý theo phân công mục 10 của CLAUDE.md)

| # | Thao tác | Nói gì | Ai nói |
|---|---|---|---|
| 1 | Mở app, thấy Menu (tên, slogan, điểm cao 0) | Giới thiệu game và slogan | Cả nhóm / TV3 |
| 2 | **Bộ sưu tập**: toàn ô khóa `?` / `???` | RecyclerView, mức chưa đạt bị khóa | TV3 |
| 3 | Back → **Ván mới**, vuốt vài nước (F gộp thành D, D+…) | Mỗi ô một nhãn điểm chữ, số nhỏ ở góc; ô gộp phóng nhẹ. **Có tiếng trượt và tiếng gộp, ô gộp càng lớn tiếng càng cao** | TV2 |
| 4 | Bấm **Hoàn tác** | Chỉ quay lại 1 bước (snapshot trong `Game`) | TV1 / TV2 |
| 5 | **Xoay màn hình** (nút Rotate của emulator) | Ván không mất nhờ ViewModel (Ch8); Activity bị tạo lại, xem Logcat tag `GameActivity` | TV2 |
| 6 | Back → Menu: có nút **Chơi tiếp** | Ván tự lưu ở `onPause` (SharedPreferences) | TV3 |
| 7 | **Nhấn giữ tiêu đề "2048 BK"** → ván demo gần thắng (5000 điểm) | Chế độ demo để khỏi chơi lâu | TV3 |
| 8 | **Vuốt trái** (←) rồi **vuốt lên** (↑) → xuất hiện **KS** → hộp thoại thắng | Thuật toán gộp: `[512,512,…]` → `1024`, hai ô 1024 cùng cột → `2048`. Mỗi ô chỉ gộp 1 lần/lượt | TV1 |
| 9 | Bấm **Chơi tiếp**, rồi Back về Menu | Chỉ báo thắng một lần mỗi ván (`hasWon`) | TV1 |
| 10 | Mở lại **Bộ sưu tập**: 11 mức đã mở, hiện tên và mô tả | Mở khóa theo ô lớn nhất từng đạt | TV3 |
| 11 | **Lịch sử**: hiện các ván đã chơi | Room: `@Entity`, `@Dao`, `suspend`, coroutine | TV3 |
| 12 | **Cài đặt**: tắt/bật Hiệu ứng động và **Âm thanh** (vào lại ván để thấy hết tiếng); **Xóa dữ liệu** (có xác nhận) | Công tắc lưu bằng SharedPreferences; hộp thoại xác nhận cho thao tác không hoàn lại | TV3 |
| 13 | (Tùy thời gian) Tắt hẳn app bằng nút Recent, mở lại, **Chơi tiếp** | Dữ liệu còn sau khi tắt app | TV3 |
| 14 | Mở `Demo.kt` trong Android Studio, chạy `main()` + chạy bộ test (31 test) | Logic chạy độc lập, không cần Android | TV1 |

### Mẹo và dự phòng
- Muốn thấy hộp thoại **thua** nhanh: ván mới rồi bấm phím mũi tên liên tục. Back từ hộp thoại sẽ về Menu.
- Nếu phím mũi tên không ăn: bấm thêm một lần, hoặc vuốt bằng chuột. (Lỗi "phím đầu tiên bị nuốt" đã được sửa nhưng nên có sẵn phương án dự phòng.)
- Nếu emulator đơ: chiếu video đã quay sẵn và vẫn giải thích như bình thường.
- Chạy thử nguyên kịch bản **ít nhất 2 lần** ở nhà, bấm giờ.

---

## 3. Câu hỏi thầy có thể hỏi

Mỗi thành viên đọc file `docs/giai-thich/` tương ứng (Thành viên 2 thêm `08-am-thanh.md`) và tự chạy thử. Câu trả lời ngắn gọn nằm trong các file đó; dưới đây là bản câu hỏi tập trung.

### Thành viên 1 – Logic game (`domain/`, đọc `01-domain.md`)
1. Giải thích `mergeRow`. Vì sao `[2,2,2,2]` ra `[4,4,0,0]` mà không ra `[8,0,0,0]`? *(nhảy `i += 2` sau khi gộp.)*
2. Làm sao 4 hướng dùng chung một hàm? *(LEFT dùng luôn; RIGHT đảo hàng; UP chuyển vị; DOWN chuyển vị + đảo; gộp xong làm ngược lại.)*
3. Khi nào sinh ô mới? Xác suất? *(Chỉ khi bàn cờ đổi; 90% ô 2, 10% ô 4.)*
4. Điều kiện thắng và thua? *(Thắng: có ô 2048, báo một lần. Thua: hết ô trống và không còn cặp kề nhau bằng nhau, hàm `hasMoves`.)*
5. Vì sao `Board` là `data class`, `GameState` là `sealed class`, `TileSets` là `object`, `Direction` là `enum`? Khác nhau thế nào?
6. Extension function là gì? Ví dụ trong code? *(`Board.transpose()`, `emptyCells()`… ở `BoardExt.kt`.)*
7. Vì sao `Game` nhận `Random` qua constructor? *(Test dùng seed cố định.)*
8. Vì sao `domain/` không được `import android.*`? *(Test bằng JUnit và `main()` mà không cần emulator.)*
9. **Câu khó:** em từng tìm ra lỗi nào trong logic? *(Kẹt sau khi thắng: bàn cờ đầy lúc chạm 2048, bấm Chơi tiếp bị đặt cứng `Playing` thay vì tính lại. Đã sửa bằng `computeState()` và có test.)*
10. Kể các test em viết và chúng kiểm tra gì. *(`MoveLogicTest` 13, `GameTest` 11, `GameStorageTest` 2.)*

### Thành viên 2 – Màn chơi (`GameActivity`, `GameViewModel`, đọc `02-man-choi.md`, `05-hoan-thien.md`)
1. Vì sao dùng ViewModel? Xoay màn hình điều gì xảy ra? *(Activity bị hủy và tạo lại, ViewModel còn nguyên.)*
2. LiveData khác biến thường chỗ nào? `observe(this)` truyền `this` để làm gì? *(Chỉ cập nhật khi Activity đang hoạt động, tự hủy đăng ký.)*
3. Vì sao có cặp `_board` (Mutable, private) và `board` (LiveData, public)?
4. `GestureDetector` và `onFling` hoạt động ra sao? Vì sao `onDown` phải trả `true`? Ngưỡng 80 pixel để làm gì?
5. `SwipeListener` dùng kiến thức nào của Chương 3? *(Kế thừa `SimpleOnGestureListener`; callback là hàm bậc cao `(Direction) -> Unit`.)*
6. Vòng đời Activity: thứ tự `onCreate → onStart → onResume → onPause → onStop → onDestroy`. Em lưu ván ở đâu, vì sao?
7. View Binding là gì, khác `findViewById` thế nào?
8. Animation làm bằng gì? Làm sao biết ô nào vừa gộp, ô nào vừa sinh? *(`ViewPropertyAnimator`; `domain` trả `mergedCells` và `lastSpawn`, giao diện chỉ vẽ.)*
9. Vì sao `GameViewModel` kế thừa `AndroidViewModel`? *(Cần `Application` để mở Room.)*
10. Âm thanh làm thế nào? Vì sao dùng `SoundPool` thay vì `MediaPlayer`, và vì sao phải `release()` ở `onDestroy`? *(SoundPool nạp trước âm ngắn, phát tức thì và phát chồng nhau; giữ bộ nhớ và luồng âm thanh nên phải giải phóng khi Activity bị hủy, nhất là lúc xoay màn hình. Cao độ tiếng gộp tăng nhờ tham số `rate`, xem `08-am-thanh.md`.)*
11. **Câu khó:** em xử lý phím mũi tên ra sao và gặp vấn đề gì? *(`onKeyDown`; sau khi chạm màn hình, Android nuốt phím điều hướng đầu tiên để rời touch mode; sửa bằng cho tiêu đề giữ focus.)*

### Thành viên 3 – Lưu trữ và các màn phụ (`GameStorage`, Menu, Bộ sưu tập, Lịch sử, Cài đặt, đọc `03`, `04`, `06`)
1. SharedPreferences lưu được kiểu nào? Bàn cờ lưu thế nào? *(Chuỗi `"2,0,4,…"` bằng `joinToString` / `split`, đọc lại bằng `Board.fromText`, trả `null` nếu hỏng.)*
2. `apply()` khác `commit()` thế nào?
3. Vì sao lưu ở `onPause` mà không phải `onDestroy`? Em còn lưu điểm cao ở đâu nữa và vì sao? *(Lưu ngay khi phá kỷ lục để tránh mất khi app bị tắt đột ngột.)*
4. Intent dùng thế nào để Menu chọn chế độ Ván mới/Chơi tiếp/Demo? *(`putExtra("mode", …)` và `getStringExtra`.)*
5. RecyclerView gồm những phần nào? `ViewHolder` để làm gì? Mức nào được mở khóa? *(`onCreateViewHolder`, `onBindViewHolder`, `getItemCount`; mở khi `value <= maxTile`.)*
6. Room gồm những phần nào? Vì sao hàm DAO là `suspend`? `viewModelScope` và `lifecycleScope` khác gì?
7. Vì sao các Activity phụ để `exported="false"`?
8. Vì sao xóa dữ liệu phải hỏi xác nhận? Xóa những gì?
9. **Câu khó:** em từng gặp lỗi nào khi làm Menu? *(Chuỗi `???` trong `strings.xml` bị hiểu là tham chiếu thuộc tính nên phải viết `\?\?\?`. Và crash khi xoay vì cùng id nhưng khác kiểu view giữa layout dọc và ngang.)*

### Câu hỏi chung cho cả nhóm
1. Vì sao dùng Views mà không dùng Jetpack Compose, Navigation, Fragment? *(Theo giáo trình và stack nhóm đã chốt; nhiều Activity + Intent; bám Chương 3–8.)*
2. Nhóm dùng AI thế nào? Các bạn hiểu code ra sao? *(Trả lời trung thực: AI viết code, từng thành viên đọc file giải thích, tự chạy thử và phải giải thích được `mergeRow` cùng luồng một lượt vuốt.)*
3. Em chỉ một chỗ minh họa Kotlin Chương 2 trong code. *(Mở bằng `grep -r "\[KT Ch2" app/src`.)*
4. Hạn chế và hướng phát triển? *(Undo chỉ 1 bước; chưa có nhạc nền (mới có hiệu ứng âm thanh ngắn); chưa có ảnh chibi; chưa kiểm thử trên nhiều cỡ màn hình; có thể thêm bảng xếp hạng.)*
5. App chạy được trên máy thật không? *(minSdk 26; hiện nhóm chủ yếu chạy emulator, nên nói rõ.)*

---

## 4. Hướng dẫn push lên GitHub

Hiện repo chỉ có **git local** với 8 commit, chưa có remote.

### Bước 1 – Quyết định danh tính commit
Các commit hiện mang tên **`sonmh <sonxoan9x@gmail.com>`** (lấy từ cấu hình git toàn cục của máy). Nếu muốn commit mang tên trưởng nhóm, đặt cho riêng repo này:

```bash
git config user.name "Tên của bạn"
```

```bash
git config user.email "email-github-cua-ban@example.com"
```

Tùy chọn: đổi tác giả cho **toàn bộ** 8 commit cũ (viết lại lịch sử, chỉ nên làm **trước khi push lần đầu**):

```bash
git rebase -r --root --exec "git commit --amend --reset-author --no-edit"
```

Nếu không muốn viết lại lịch sử, cứ để nguyên; từ commit sau sẽ mang tên mới.

### Bước 2 – Tạo repo trên GitHub
1. Vào github.com → **New repository**.
2. Đặt tên, ví dụ `bk2048`. Chọn **Private** nếu chưa muốn công khai (sau đó mời thầy và các bạn bằng *Settings → Collaborators*).
3. **Không** tích "Add a README", "Add .gitignore" hay license (để repo rỗng, tránh xung đột với lịch sử có sẵn).

### Bước 3 – Gắn remote và push
Thay `<tai-khoan>` bằng tài khoản GitHub của bạn.

```bash
git remote add origin https://github.com/<tai-khoan>/bk2048.git
```

```bash
git branch -M main
```

```bash
git push -u origin main
```

Lần đầu GitHub sẽ yêu cầu đăng nhập. Trên Windows, Git Credential Manager mở cửa sổ trình duyệt; hoặc dùng **Personal Access Token** (Settings → Developer settings → Tokens) thay cho mật khẩu. Hoặc dùng *Git → Push* trong Android Studio / GitHub Desktop.

### Bước 4 – Các bạn còn lại tải về và chạy
```bash
git clone https://github.com/<tai-khoan>/bk2048.git
```

1. Mở thư mục vừa clone bằng **Android Studio** (File → Open), chờ Gradle sync.
2. Chọn emulator và bấm Run. Android Studio tự tạo `local.properties` (đường dẫn SDK), file này **không** nằm trong git.
3. Chạy test: chuột phải thư mục `app/src/test` → Run Tests, hoặc `gradlew.bat testDebugUnitTest`.

### Lưu ý
- Repo đã có `.gitignore` bỏ qua `local.properties`, `.idea/`, `build/`, `.gradle/`, `.kotlin/`. Không commit file `local.properties`.
- Thư mục `BK20482` (bản sao dư do tạo project nhầm tên) nằm **ngoài** repo, có thể xóa.
- Cả nhóm cùng sửa code thì nên làm theo nhánh (`git switch -c ten-nhanh`) rồi mở Pull Request; còn nếu chỉ một máy build như đã thống nhất thì push thẳng `main` là đủ.

---

## 5. Danh sách kiểm tra trước giờ báo cáo

- [ ] Cả 3 thành viên đã đọc file giải thích của phần mình và tự chạy thử app.
- [ ] Ai cũng giải thích được `mergeRow` và luồng một lượt vuốt (mục 1).
- [ ] Emulator bật Quick Boot, đã chạy thử kịch bản 2 lần, đã bấm giờ.
- [ ] Đã **Xóa dữ liệu** để bắt đầu từ trạng thái sạch.
- [ ] Phím mũi tên điều khiển được; chế độ demo (nhấn giữ tiêu đề) hoạt động.
- [ ] Có video quay sẵn kịch bản để dự phòng.
- [ ] Code đã push lên GitHub, các bạn clone về chạy được.
- [ ] Slide: tên game và slogan, kiến trúc 3 tầng (`domain / data / ui`), luồng một lượt vuốt, bảng điểm chữ, kịch bản demo, hạn chế và hướng phát triển.
- [ ] Mỗi người nắm 3–5 câu hỏi của phần mình (mục 3).
