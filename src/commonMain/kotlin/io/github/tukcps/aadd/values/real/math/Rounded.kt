package io.github.tukcps.aadd.values.real.math

data class Rounded(
    val value: Double,
    val error: Double
) {
    val exact: Boolean
        get() = error == 0.0
}