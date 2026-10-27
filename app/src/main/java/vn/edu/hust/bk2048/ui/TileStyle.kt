package vn.edu.hust.bk2048.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import vn.edu.hust.bk2048.R

// Ánh xạ giá trị ô -> màu nền, màu chữ (phần phụ thuộc Android nên nằm ở ui/)
// [KT Ch3.4] object: một bản duy nhất, gọi TileStyle.background(...)
object TileStyle {

    // [KT Ch1.5] when chọn màu nền theo giá trị ô; 4096 trở lên dùng chung màu tile_super
    private fun colorRes(value: Int): Int = when (value) {
        0 -> R.color.tile_empty
        2 -> R.color.tile_2
        4 -> R.color.tile_4
        8 -> R.color.tile_8
        16 -> R.color.tile_16
        32 -> R.color.tile_32
        64 -> R.color.tile_64
        128 -> R.color.tile_128
        256 -> R.color.tile_256
        512 -> R.color.tile_512
        1024 -> R.color.tile_1024
        2048 -> R.color.tile_2048
        else -> R.color.tile_super
    }

    // Nền sáng (vàng, chanh, vàng gold) dùng chữ tối; còn lại dùng chữ trắng
    private fun textColorRes(value: Int): Int = when (value) {
        16, 32, 1024, 2048 -> R.color.tile_text_dark
        else -> R.color.tile_text_light
    }

    fun background(context: Context, value: Int): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = 12f * context.resources.displayMetrics.density
            setColor(ContextCompat.getColor(context, colorRes(value)))
        }

    fun textColor(context: Context, value: Int): Int =
        ContextCompat.getColor(context, textColorRes(value))
}
