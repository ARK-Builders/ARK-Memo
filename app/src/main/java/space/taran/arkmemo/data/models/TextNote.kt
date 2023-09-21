package space.taran.arkmemo.data.models

import android.os.Parcelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.parcelize.IgnoredOnParcel
import space.taran.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    var content: Content,
    @IgnoredOnParcel
    val meta: ResourceMeta? = null
): BaseNote<String>(content.data), Parcelable
{

    @IgnoredOnParcel
    override var isForked = false
    fun putContent(content: Content) {
        this.content = content
        update(content.data)
    }

    fun isNotEmpty() = content.data.isNotEmpty()

    @Parcelize
    data class Content(
        val title: String,
        val data: String
        ): Parcelable
}

abstract class BaseNote<T>(public val data: T) {
    abstract var isForked: Boolean
    private val flow = MutableStateFlow(false)
    protected fun update(content: T) {
        flow.value = data != content
    }
    suspend fun hasChanged(emit: (Boolean) -> Unit) {
        flow.collectLatest {
            emit(it)
        }
    }
}