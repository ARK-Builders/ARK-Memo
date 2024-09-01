package dev.arkbuilders.arkmemo.utils

import dev.arkbuilders.arklib.user.properties.Properties

fun Properties.isEqual(properties: Properties): Boolean {
    return (this.titles == properties.titles) &&
        (this.descriptions == properties.descriptions)
}
