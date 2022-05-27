package cj.jukebox.utils

import java.time.Instant

// unit: seconds
const val day = 86400
const val week = 604800
const val month = 2629800

/**
 * Raccourci pour acquérir le timestamp actuel.
 */
fun getNow() = Instant.now().epochSecond.toInt()