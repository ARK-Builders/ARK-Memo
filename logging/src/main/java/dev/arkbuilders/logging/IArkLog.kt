package dev.arkbuilders.logging

interface IArkLog {
    fun d(tag: String?, msg: String): Int
    fun i(tag: String?, msg: String): Int
    fun v(tag: String?, msg: String): Int
    fun w(tag: String?, msg: String): Int
    fun e(tag: String?, msg: String?, tr: Throwable?): Int
    fun e(tag: String?, msg: String): Int
}