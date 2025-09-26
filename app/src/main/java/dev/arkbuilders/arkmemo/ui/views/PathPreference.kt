package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import dev.arkbuilders.arkmemo.R

class PathPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var title: TextView? = null
    private var path: TextView? = null
    var onBindView: () -> Unit = {}

    fun setPath(path: String?) {
        if (path != null) {
            this.path?.text = path
        }
    }

    fun setTitle(title: String?) {
        if (title != null) {
            this.title?.text = title
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        title = holder.findViewById(R.id.title) as TextView
        path = holder.findViewById(R.id.pathValue) as TextView
        onBindView()
    }
}
