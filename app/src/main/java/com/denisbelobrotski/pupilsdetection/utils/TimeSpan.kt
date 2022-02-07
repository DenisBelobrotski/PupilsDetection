package com.denisbelobrotski.pupilsdetection.utils

class TimeSpan(private val totalMillis: Long) {
    private val millis: Long
    private val seconds: Long
    private val minutes: Long
    private val hours: Long
    private val days: Long


    val TotalMillis: Long
        get() = totalMillis
    val Millis: Long
        get() = millis
    val Seconds: Long
        get() = seconds
    val Minutes: Long
        get() = minutes
    val Hours: Long
        get() = hours
    val Days: Long
        get() = days


    companion object {
        private const val MILLIES_IN_SEC = 1000
        private const val SECS_IN_MINUTE = 60
        private const val MINUTES_IN_HOUR = 60
        private const val HOURS_IN_DAY = 24
    }


    init {
        var elapsedMillis = totalMillis
        millis = elapsedMillis % MILLIES_IN_SEC
        elapsedMillis /= MILLIES_IN_SEC
        seconds = elapsedMillis % SECS_IN_MINUTE
        elapsedMillis /= SECS_IN_MINUTE
        minutes = elapsedMillis % MINUTES_IN_HOUR
        elapsedMillis /= MINUTES_IN_HOUR
        hours = elapsedMillis % HOURS_IN_DAY
        elapsedMillis /= HOURS_IN_DAY
        days = elapsedMillis
    }


    override fun toString(): String {
        return "$days:$hours:$minutes:$seconds:$millis"
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeSpan

        if (totalMillis != other.totalMillis) return false

        return true
    }
}
