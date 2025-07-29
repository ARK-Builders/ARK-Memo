package dev.arkbuilders.logging

import android.util.Log

class ArkLogImpl: IArkLog {

    override fun d(tag: String?, msg: String): Int {
        return Log.d(tag, msg)
    }

    override fun i(tag: String?, msg: String): Int {
        return Log.i(tag, msg)
    }

    override fun v(tag: String?, msg: String): Int {
        return Log.v(tag, msg)
    }

    override fun w(tag: String?, msg: String): Int {
        return Log.w(tag, msg)
    }

    override fun e(tag: String?, msg: String?, tr: Throwable?): Int {
        return Log.e(tag, msg, tr)
    }

    override fun e(tag: String?, msg: String): Int {
        return Log.e(tag, msg)
    }
}