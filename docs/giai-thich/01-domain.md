# Giai đoạn 0–1: Khung project và logic game (`domain/`)

## Làm gì
- **GĐ 0:** bật View Binding trong `app/build.gradle.kts` (`buildFeatures { viewBinding = true }`), đổi tên app thành `2048 BK`, tạo các package `domain / data / ui`.
- **GĐ 1:** viết toàn bộ luật chơi bằng Kotlin thuần (không `import android.*`) nên test được bằng JUnit và chạy thử bằng `main()`.

## File trong `domain/`
| File | Vai trò |
|---|---|
| `Direction.kt` | enum 4 hướng |
| `Board.kt` | `data class Board(cells)`, `toText()` / `fromText()` để lưu chuỗi `"2,0,4,…"` |
| `BoardExt.kt` | extension: `transpose`, `reversedRows`, `emptyCells`, `isFull`, `maxTile`, `hasMoves` |
| `MoveLogic.kt` | `mergeRow(row)` và `move(board, direction)` |
| `GameState.kt` | sealed class `Playing / Won / Lost` |
| `Game.kt` | giữ board, score, state; `move`, `newGame`, `undo`, `load` |
| `TileInfo.kt`, `TileSets.kt` | nhãn F…KS, tên, mô tả từng mức ô |
| `Demo.kt` | `main()` in bàn cờ ra console |

## Hàm quan trọng: `mergeRow`
1. `filter { it != 0 }` bỏ ô trống → các ô dồn sát nhau.
2. Duyệt bằng `while`: nếu ô `i` bằng ô `i+1` thì gộp (nhân đôi, cộng điểm) rồi **nhảy `i += 2`** để ô vừa gộp không bị gộp lần nữa; ngược lại giữ nguyên ô.
3. Thêm số 0 vào cuối cho đủ độ dài.
4. `moved = newRow != row` cho biết hàng có thay đổi không.

Ví dụ `[2,2,2,2]` → `[4,4,0,0]`; `[2,2,4,0]` → `[4,4,0,0]`.

## Một lượt vuốt (`move`)
Cả 4 hướng dùng chung `mergeRow` (luôn gộp sang trái), chỉ khác bước chuẩn bị:
- LEFT: dùng luôn · RIGHT: đảo hàng → gộp → đảo lại
- UP: chuyển vị → gộp → chuyển vị lại · DOWN: chuyển vị + đảo hàng → gộp → làm ngược lại

`Game.move` chỉ sinh ô mới (90% ô 2, 10% ô 4) khi bàn cờ **có thay đổi**. Thắng khi lần đầu có ô 2048 (`hasWon` đảm bảo chỉ báo một lần). Thua khi không còn ô trống và không còn cặp kề nhau bằng nhau (`hasMoves`). Undo lưu một `Snapshot` của lượt trước.

## Chạy thử
- Test: `gradlew.bat testDebugUnitTest` (hoặc chuột phải thư mục `test` → Run).
- Demo: mở `Demo.kt`, bấm mũi tên xanh cạnh `fun main()`.
- Tìm chỗ minh họa kiến thức: `grep -r "\[KT" app/src`.

## 3–5 câu thầy có thể hỏi
1. **Vì sao hàng `[2,2,2,2]` ra `[4,4,0,0]` mà không ra `[8,0,0,0]`?** Sau khi gộp hai ô, ta nhảy `i += 2` nên ô mới `4` không được so sánh lại với ô kế tiếp; mỗi ô chỉ gộp một lần mỗi lượt.
2. **Làm sao 4 hướng dùng chung một hàm?** Quy mọi hướng về "vuốt trái": hướng phải thì đảo hàng, lên/xuống thì chuyển vị ma trận, gộp xong làm ngược lại.
3. **Vì sao dùng `data class` cho `Board`?** Tự có `equals` và `copy`, so sánh hai bàn cờ bằng `==`, và bất biến nên dễ lưu để Undo.
4. **`sealed class GameState` khác `enum` ở đâu?** Cả hai có số trường hợp cố định, nhưng sealed class có thể mang dữ liệu riêng cho từng trường hợp và `when` không cần nhánh `else`.
5. **Vì sao `Game` nhận `Random` qua constructor?** Để test truyền `Random(seed)` cố định, kết quả sinh ô lặp lại được.
