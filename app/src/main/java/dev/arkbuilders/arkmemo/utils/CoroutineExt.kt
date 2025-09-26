package dev.arkbuilders.arkmemo.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

fun CoroutineScope.launchPeriodicAsync(
    repeatMillis: Long,
    repeatCondition: Boolean,
    action: () -> Unit,
): Deferred<Unit> {
    val deferred =
        this.async {
            if (repeatMillis > 0) {
                while (this.isActive && repeatCondition) {
                    action()
                    delay(repeatMillis)
                }
            } else {
                action()
            }
        }
    return deferred
}
