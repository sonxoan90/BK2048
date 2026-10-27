package vn.edu.hust.bk2048.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import vn.edu.hust.bk2048.R
import vn.edu.hust.bk2048.data.GameRecord
import vn.edu.hust.bk2048.databinding.ItemHistoryBinding
import vn.edu.hust.bk2048.domain.TileSets
import vn.edu.hust.bk2048.ui.TileStyle

// Mỗi dòng: ô lớn nhất đạt được, điểm, ngày giờ và kết quả
class HistoryAdapter(private val items: List<GameRecord>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = items[position]
        val context = holder.itemView.context
        val view = holder.binding

        view.tvTile.text = TileSets.infoFor(record.maxTile).label
        view.tvTile.background = TileStyle.background(context, record.maxTile)
        view.tvTile.setTextColor(TileStyle.textColor(context, record.maxTile))

        view.tvScore.text = context.getString(R.string.history_score, record.score)
        val result = context.getString(if (record.won) R.string.history_won else R.string.history_not_won)
        view.tvDetail.text = "${dateFormat.format(Date(record.playedAt))} · $result"
    }
}
