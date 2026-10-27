package vn.edu.hust.bk2048.ui.game

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs
import vn.edu.hust.bk2048.domain.Direction

// [KT Ch3.2] kế thừa: SwipeListener là con của SimpleOnGestureListener, chỉ ghi đè hàm cần dùng
// [KT Ch2.4] onSwipe là hàm bậc cao (callback) kiểu (Direction) -> Unit
class SwipeListener(private val onSwipe: (Direction) -> Unit) :
    GestureDetector.SimpleOnGestureListener() {

    // Phải trả về true thì GestureDetector mới tiếp tục gửi onFling
    override fun onDown(e: MotionEvent): Boolean = true

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // [KT Ch1.4] e1 có thể null nên dùng ?: để thoát sớm
        val start = e1 ?: return false
        val dx = e2.x - start.x
        val dy = e2.y - start.y
        if (abs(dx) < MIN_DISTANCE && abs(dy) < MIN_DISTANCE) return false

        // Vuốt theo trục nào dài hơn thì lấy trục đó
        val direction = if (abs(dx) > abs(dy)) {
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        } else {
            if (dy > 0) Direction.DOWN else Direction.UP
        }
        onSwipe(direction)
        return true
    }

    private companion object {
        const val MIN_DISTANCE = 80f // pixel
    }
}
