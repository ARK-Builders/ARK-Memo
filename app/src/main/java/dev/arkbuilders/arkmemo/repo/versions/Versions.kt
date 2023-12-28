package dev.arkbuilders.arkmemo.repo.versions

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.storage.Monoid

typealias Versions = Set<ResourceId>

object VersionsMonoid : Monoid<Versions> {

    override val neutral: Versions = setOf()

    override fun combine(a: Versions, b: Versions): Versions {
        val result = a.union(b)
        Log.d(LOG_PREFIX, "merging $a and $b into $result")
        return result
    }
}

internal val LOG_PREFIX: String = "[versions]"