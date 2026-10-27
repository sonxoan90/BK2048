package vn.edu.hust.bk2048.ui.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.edu.hust.bk2048.R
import vn.edu.hust.bk2048.databinding.ItemCollectionBinding
import vn.edu.hust.bk2048.domain.TileInfo
import vn.edu.hust.bk2048.ui.TileStyle

// [KT Ch5] RecyclerView.Adapter: biến danh sách TileInfo thành các dòng trên màn hình.
// Mức đã đạt (value <= maxTile) hiện đầy đủ, mức chưa đạt hiện khóa "???".
class TileAdapter(
    private val items: List<TileInfo>,
    private val maxTile: Int
) : RecyclerView.Adapter<TileAdapter.TileViewHolder>() {

    // ViewHolder giữ sẵn các view của một dòng để không phải tìm lại mỗi lần cuộn
    class TileViewHolder(val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TileViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        val info = items[position]
        val context = holder.itemView.context
        val view = holder.binding
        if (info.value <= maxTile) {
            view.tvTile.text = info.label
            view.tvTile.background = TileStyle.background(context, info.value)
            view.tvTile.setTextColor(TileStyle.textColor(context, info.value))
            view.tvName.text = info.title
            view.tvDescription.text = info.description
        } else {
            // Chưa đạt: ô xám với dấu ?, ẩn tên và mô tả
            view.tvTile.text = context.getString(R.string.locked_label)
            view.tvTile.background = TileStyle.background(context, 0)
            view.tvTile.setTextColor(context.getColor(R.color.tile_text_dark))
            view.tvName.text = context.getString(R.string.locked_title)
            view.tvDescription.text = context.getString(R.string.locked_description)
        }
    }
}
