package dev.arkbuilders.logging

object ALog {

    private val logImpl by lazy { ArkLogImpl() }

    @Volatile
    var isEnableLog: Boolean = false

    fun init(isEnabled: Boolean = false) {
        isEnableLog = isEnabled
    }

    fun d(tag: String?, msg: String): Int {
        if (!isEnableLog) return -1
        return logImpl.d(tag, msg)
    }

    fun i(tag: String?, msg: String): Int {
        if (!isEnableLog) return -1
        return logImpl.i(tag, msg)
    }

    fun v(tag: String?, msg: String): Int {
        if (!isEnableLog) return -1
        return logImpl.v(tag, msg)
    }

    fun w(tag: String?, msg: String): Int {
        if (!isEnableLog) return -1
        return logImpl.w(tag, msg)
    }

    fun e(tag: String?, msg: String?, tr: Throwable?): Int {
        if (!isEnableLog) return -1
        return logImpl.e(tag, msg, tr)
    }

    fun e(tag: String?, msg: String): Int {
        if (!isEnableLog) return -1
        return logImpl.e(tag, msg)
    }
}