package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.VersionMeta

@Parcelize
data class Version (
    val content: Content,
    @IgnoredOnParcel
    val meta: VersionMeta? = null
): Parcelable
{
    @Parcelize
    data class Content(
        val idList: List<String>
        ): Parcelable
}