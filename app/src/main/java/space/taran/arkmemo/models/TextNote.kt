package space.taran.arkmemo.models

import space.taran.arkmemo.data.ResourceMeta

data class TextNote (
    val content: Content,
    val meta: ResourceMeta? = null
    )
{
    data class Content(
        val title: String,
        val data: String
        )
}