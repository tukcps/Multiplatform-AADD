package io.github.tukcps.aadd.values.real.aa

// Just special cases of the pow function ...
fun cbrt(x: AffineForm) = pow(x, 1.0 / 3.0)
fun root(x: AffineForm, degree: Double) = pow(x, 1.0 / degree)
fun root(x: AffineForm, degree: AffineForm) = pow(x, inv(degree))