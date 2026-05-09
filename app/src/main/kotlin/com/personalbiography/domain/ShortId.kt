package com.personalbiography.domain

import java.security.SecureRandom

/**
 * Generate short, human-friendly entry IDs.
 *
 * Mirrors `app/store/short_id.py` in the Python repo: 6-character random IDs
 * drawn from a Crockford-base32-ish alphabet that omits the visually
 * ambiguous characters 0/O, 1/I/L, U.
 */
private const val LENGTH = 6
private const val ALPHABET = "ABCDEFGHJKMNPQRSTVWXYZ23456789"
private val RNG = SecureRandom()

fun makeShortId(): String =
    buildString(LENGTH) {
        repeat(LENGTH) {
            append(ALPHABET[RNG.nextInt(ALPHABET.length)])
        }
    }
