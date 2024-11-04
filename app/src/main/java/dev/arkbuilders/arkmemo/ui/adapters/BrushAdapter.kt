package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.AdapterBrushBinding

class BrushAdapter(
    private val attributes: List<BrushAttribute>,
    private val onItemClick: (attribute: BrushAttribute, pos: Int) -> Unit,
) : RecyclerView.Adapter<BrushAdapter.BrushTypeViewHolder>() {
    private var lastSelectedPos =
        attributes.indexOfFirst { it.isSelected }
            .coerceAtLeast(0)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BrushTypeViewHolder {
        val binding = AdapterBrushBinding.inflate(LayoutInflater.from(parent.context))
        return BrushTypeViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BrushTypeViewHolder,
        position: Int,
    ) {
        val attribute = attributes[holder.bindingAdapterPosition]
        val context = holder.itemView.context

        when (attribute) {
            is BrushSizeTiny -> {
                holder.ivBrush.setImageResource(R.drawable.bg_brush_size)
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_tiny_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }

            is BrushSizeSmall -> {
                holder.ivBrush.setImageResource(R.drawable.bg_brush_size)
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_small_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }

            is BrushSizeMedium -> {
                holder.ivBrush.setImageResource(R.drawable.bg_brush_size)
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_medium_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }

            is BrushSizeLarge -> {
                holder.ivBrush.setImageResource(R.drawable.bg_brush_size)
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_large_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }

            is BrushSizeHuge -> {
                holder.ivBrush.setImageResource(R.drawable.bg_brush_size)
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_huge_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }

            is BrushColorBlack ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.black)
            is BrushColorBlue ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_blue)
            is BrushColorGreen ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_green)
            is BrushColorGrey ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_grey)
            is BrushColorOrange ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_orange)
            is BrushColorPurple ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_purple)
            is BrushColorRed ->
                holder.ivBrush.imageTintList = context.getColorStateList(R.color.brush_color_red)
        }

        if (attribute.isSelected) {
            holder.rootView.setBackgroundResource(R.drawable.bg_selected_brush)
            if (attribute is BrushColor) {
                val padding =
                    holder.itemView.context.resources.getDimensionPixelSize(
                        R.dimen.brush_size_huge_padding,
                    )
                holder.ivBrush.setPadding(padding, padding, padding, padding)
            }
        } else {
            holder.rootView.setBackgroundResource(0)
            if (attribute is BrushColor) {
                holder.ivBrush.setPadding(0, 0, 0, 0)
            }
        }

        holder.rootView.setOnClickListener {
            if (attribute.isSelected) return@setOnClickListener

            attribute.isSelected = true
            attributes[lastSelectedPos].isSelected = false

            notifyItemChanged(holder.bindingAdapterPosition)
            notifyItemChanged(lastSelectedPos)

            lastSelectedPos = holder.bindingAdapterPosition
            onItemClick.invoke(attribute, holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount() = attributes.size

    inner class BrushTypeViewHolder(binding: AdapterBrushBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ivBrush = binding.ivBrush
        val rootView = binding.root
    }
}
