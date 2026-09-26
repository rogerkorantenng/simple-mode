package com.simplemode.firetv.family

/**
 * "3 minutes ago", spoken-register rather than a clock face -- consistent
 * with a screen meant to be skimmed by a caregiver checking in, not read
 * like a log file. Pure and framework-free so it is directly unit-testable.
 */
fun formatRelativeTime(nowMillis: Long, thenMillis: Long): String {
    val diffSeconds = ((nowMillis - thenMillis) / 1000).coerceAtLeast(0)
    return when {
        diffSeconds < 60 -> "just now"
        diffSeconds < 3600 -> "${diffSeconds / 60} minute${if (diffSeconds / 60 == 1L) "" else "s"} ago"
        diffSeconds < 86_400 -> "${diffSeconds / 3600} hour${if (diffSeconds / 3600 == 1L) "" else "s"} ago"
        else -> "${diffSeconds / 86_400} day${if (diffSeconds / 86_400 == 1L) "" else "s"} ago"
    }
}
