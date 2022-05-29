package cj.jukebox.utils

import java.time.Duration
import java.time.Instant

// unit: seconds
val day: Duration = Duration.ofDays(1)
val week: Duration = Duration.ofDays(7)
val month: Duration = Duration.ofDays(31)

/**
 * Raccourci pour acquérir le timestamp actuel.
 */
fun getNow(): Instant = Instant.now()