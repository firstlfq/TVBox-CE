package com.github.tvbox.osc.util

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object RegexUtils {
    private val patternCache = ConcurrentHashMap<String, Pattern>()

    @JvmStatic
    fun getPattern(regex: String): Pattern = patternCache.getOrPut(regex) { Pattern.compile(regex) }

    @JvmStatic
    fun getPattern(regex: String, flag: Int): Pattern = patternCache.getOrPut(regex) { Pattern.compile(regex, flag) }
}
