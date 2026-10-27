package vn.edu.hust.bk2048.domain

// [KT Ch3.4] sealed class: chỉ có đúng 3 trạng thái, when không cần nhánh else
sealed class GameState {
    object Playing : GameState()
    object Won : GameState()
    object Lost : GameState()
}
