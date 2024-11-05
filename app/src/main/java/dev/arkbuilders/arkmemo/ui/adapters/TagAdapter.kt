package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.databinding.AdapterTagBinding
import dev.arkbuilders.arkmemo.models.Tag

class TagAdapter(
    private val tags: List<Tag>,
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TagViewHolder {
        val binding = AdapterTagBinding.inflate(LayoutInflater.from(parent.context))
        return TagViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        holder: TagViewHolder,
        position: Int,
    ) {
        val tag = tags[position]
        holder.title.text = tag.value
    }

    override fun getItemCount() = tags.size

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding {
            AdapterTagBinding.bind(itemView)
        }
        val title = binding.tvTag
    }
}
