package io.github.tukcps.aadd.values.real.rounding

internal actual object FMA {
        actual fun compute(a: Double, b: Double, c: Double): Double =
                a * b + c
}