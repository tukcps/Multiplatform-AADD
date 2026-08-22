package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.RealValue
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.minus
import io.github.tukcps.aadd.values.real.rounding.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.rounding.RoundingMath
import io.github.tukcps.aadd.values.real.toDoubleBound
import kotlin.math.*

/**
 * ## Affine Form
 *
 * An Affine Form implements in particular linear computations with less wrapping effect.
 * The main constructor is private and only sets the states without any checks.
 * To create AffineForms, use one of the factory methods in the companion object:
 * - `create(...)`,
 * - `scalar(...)`, or
 * - `range(...)`
 * @param builder The factory class that builds affine forms and AADD
 * @param min The minimum value of an interval interpretation
 * @param max The maximum value of an interval interpretation
 * @param central The central value of the affine form
 * @param xi The noise variables of the affine form; if it is an empty set, we only use min/max.
 * Note that the Affine Form also inherits RealRange that holds min/max values of interval arithmetic
 * computations. That are used to reduce over-approximation in particular for non-linear operations.
 */
class AffineForm internal constructor(
    val builder: DDBuilder,
    min: DoubleBound,
    max: DoubleBound,
    var central: Double,
    val xi: HashMap<Long, Double> = HashMap(300, 0.75F),
) : RealRange(min, max), NumberRange<DoubleBound>, RealValue {

    /**
     * Creates an affine form as a clone of an existing affine form.
     */
    constructor(builder: DDBuilder, af: AffineForm):
            this(builder, af.min, af.max, af.central, HashMap(af.xi))

    /**
     * Creates an affine form as a clone of an existing affine form,
     * but removes all garbage noise symbols that are
     * smaller than a fixed threshold, when a flag is set.
     */
    constructor(af: AffineForm): this(
        builder = af.builder,
        min = af.min, max = af.max,
        central = af.central,
        xi =HashMap(af.xi)
    )

    /**
     * The radius of the Affine Form.
     * It allows computation of the interval enclosing the Affine Form by
     * interval = [central-radius, central+radius]
     */
    val radius: Double
        get() = when {
            (isEmpty()) -> 0.0
            else -> {
                var result = 0.0
                for (v in xi.values)
                    result = math.add(result, abs(v), Rounding.UP)
                result
            }
        }

    /**
     * Creates a clone unless the representation is a Singleton (Empty, All)
     * @return a clone, but not for the singletons
     */
    fun clone(): AffineForm =
        if (isEmpty()||isReals()) this else AffineForm(builder, min, max, central, xi)

    fun copy(min: DoubleBound? = null, max: DoubleBound? = null): AffineForm =
        create(builder, min?:this.min, max?:this.max, central, 0.0, xi)

    /**
     * Two Affine Forms are equal if central range,
     * central value and partial deviations, r are the same.
     */
    override fun equals(other: Any?): Boolean =
        other is AffineForm &&
                min == other.min && max == other.max &&
                central == other.central &&
                xi == other.xi

    /**
     * Comparison of affine forms:
     *  - We compare the range of this and other.
     *  - If this is for sure larger that the other, we return 1,
     *  - If the other is for sure larger that this, we return -1,
     *  - else we return 0.
     *
     *  Note that this comparison is uncertain for the result 0 as a more
     *  accurate result might turn 0 to -1 or 1 by solving constraint systems.
     *  @param other, the affine form with which we compare this
     *  @return 1 if this > other, -1 if this < other, 0 else.
     */
    operator fun compareTo(other: AffineForm) = when {
        (this.min > other.max)  -> 1
        (other.min < this.max)  -> -1
        else  -> 0
    }

    /**
     * The similarity of two affine forms is measured by the amount of uncorrelated deviation
     * that would be caused by merging them both into a single affine form.
     * @param other The affine form that is compared with this.
     * @param tol The tolerance below which we consider the affine forms as similar.
     * @return true, if similar.
     */
    fun isSimilar(other: AffineForm, tol: Double): Boolean {
        if (abs((min - other.min).toDouble()) > tol) return false // Rounding, etc. negligible
        if (abs((max - other.max).toDouble()) > tol) return false
        if (other === this) return true
        var uncorrelated = abs(central - other.central)
        uncorrelated = (uncorrelated + uncorrelated.ulp) / 2.0
        for (i in xi.keys+other.xi.keys) {
            val xi = xi.getOrElse(i){0.0}
            val yi = other.xi.getOrElse(i){0.0}
            uncorrelated += if (xi * yi > 0) abs(xi - yi) else xi + yi
        }
        return uncorrelated < tol
    }

    /**
     * Computes a conservative affine enclosure of the intersection.
     *
     * The implementation preserves the affine form if both operands are equal.
     * Otherwise, it falls back to the interval intersection and constructs a new
     * affine form from that interval.
     */
    override fun intersect(other: NumberRange<DoubleBound>): AffineForm {

        if (other !is AffineForm)
            return range(builder, (RealRange(this)).intersect(other))

        // Fast path.
        if (this === other)
            return this

        // Same affine model and constraints.
        if (central == other.central && xi == other.xi && this.min == other.min && this.max == other.max)
            return this

        // Conservative interval intersection.
        val interval: RealRange = RealRange(this).intersect(other)
        if (interval.isEmpty())
            return builder.AF.Empty

        return range(builder, interval)
    }

    infix fun constrainToRange(range: NumberRange<DoubleBound>): AffineForm {
        return create(builder, RealRange(range intersect RealRange(this)), central, 0.0, xi)
    }

    /**
     * Computes an affine model of the common range while preserving as much correlation
     * information as possible.
     * @param other the second affine form.
     * @return the joined range as affine form.
     */
    fun join(other: AffineForm): AffineForm {
        var (newCentral, centralErr) = math.addRounded(central, other.central)
        newCentral /= 2
        var newNoise = abs(math.sub(central, other.central, Rounding.UP))
        newNoise = math.div(newNoise, 2.0, Rounding.UP)
        newNoise = math.add(newNoise, centralErr, Rounding.UP)
        val newXi = HashMap<Long, Double>()
        for (i in xi.keys+other.xi.keys) {
            val xi = xi.getOrElse(i){0.0}
            val yi = other.xi.getOrElse(i){0.0}
            if (xi * yi > 0) {
                newXi[i] = min(abs(xi), abs(yi)) * sign(xi)
                val dif = abs(math.sub(xi, yi, Rounding.UP))
                newNoise = math.add(newNoise, dif, Rounding.UP)
            } else {
                newNoise = math.add(newNoise, abs(xi), Rounding.UP)
                newNoise = math.add(newNoise, abs(yi), Rounding.UP)
            }
        }
        return create(builder, this as RealRange join other, newCentral, newNoise, newXi)
    }

    /**
     * HashCode to check equality
     */
    override fun hashCode(): Int =
        31 * (31 * (31 * min.hashCode() + max.hashCode()) + central.hashCode()) + xi.hashCode()

    /**
     * Returns a symbolic representation of the affine form
     * noise variables are e_i with 'i' being the index
     * Form: c+r+sum_i a_i e_i
     * */
    fun toSymbolicString(): String {
        var str = "$central"
        for((key, value) in xi) {
            str+= "+${value}e_$key"
        }
        return str
    }

    companion object {
        /**
         * Platform-dependent rounding methods ...
         */
        val math: RoundingMath = IEEE754RoundingMath

        /**
         * Creates an affine form representing a scalar value.
         * A finite scalar is represented by an affine form with zero radius
         * and no noise symbols. Positive and negative infinity are represented
         * using the corresponding range bounds.
         * IEEE-754 NaN is interpreted as the empty set and returns [DDBuilder.AF.Empty].
         * @param builder the builder managing noise symbols and canonical states
         * @param scalar the scalar value to represent
         * @return an affine form representing the scalar value, or the empty affine
         *   form for NaN
         */
        fun scalar(builder: DDBuilder, scalar: Double): AffineForm = when {
            scalar.isNaN() -> builder.AF.Empty
            else -> {
                val bound = scalar.toDoubleBound()!!
                AffineForm(builder, bound, bound, scalar, hashMapOf())
            }
        }

        fun scalar(builder: DDBuilder, scalar: DoubleBound): AffineForm =
            AffineForm(builder, scalar, scalar, scalar.toDouble(), hashMapOf())

        /**
         * Creates an affine form from an interval representation.
         *
         * This function is used as a fallback when the affine approximation
         * (central value and noise symbols) is no longer valid, but the interval
         * enclosure is still available. Previous noise symbols are discarded and
         * a new canonical affine representation is generated if possible.
         *
         * The central value is rounded to nearest, while the remainder is rounded
         * upwards to preserve a guaranteed enclosure of the original interval.
         *
         * @param builder builder providing canonical affine form instances
         * @param min lower bound of the interval
         * @param max upper bound of the interval
         * @return canonical affine form representing the interval
         */
        fun range(builder: DDBuilder, min: DoubleBound, max: DoubleBound, id: String? = null): AffineForm {
            if (min > max) return builder.AF.Empty
            if (min == max) return scalar(builder, min.toDouble())
            if (min is DoubleBound.Finite && max is DoubleBound.Finite) {
                val central = math.midpoint(min, max, Rounding.NEAREST).toDouble()
                val xi = hashMapOf<Long, Double>()
                xi [builder.noiseVariables.newNoiseVar(id)] = (max.finiteValue - min.finiteValue) / 2.0
                return AffineForm(builder, min, max, central, xi)
            }
            return if (min is DoubleBound.Finite || max is DoubleBound.Finite) {
                AffineForm(builder, min, max, Double.NaN, hashMapOf())
            } else
                builder.AF.All
        }

        fun range(builder: DDBuilder, range: NumberRange<DoubleBound>, id: String? = null): AffineForm =
            range(builder, range.min, range.max, id)

        fun range(builder: DDBuilder, range: ClosedRange<Double>, id: String? = null): AffineForm =
            range(builder, RealRange(range), id)

        /**
         * Central factory method that brings representation to canonical forms and does checks.
         * All factory methods shall use this one in the end.
         * - r must not be < 0.0, throw exception.
         * - if radius is infinite, drop xi completely & set r to +Infinity (hence, use Range only)
         * - if central, radios or any of Xi is NaN, drop xi, drop xi completely & set r to Infinity (hence, use Range only)
         * - check
         */
        fun create(builder: DDBuilder, min: DoubleBound, max: DoubleBound, central: Double, newNoise: Double, xi: HashMap<Long, Double> = hashMapOf()): AffineForm {

            builder.noiseVariables.compressGarbageVariables(xi)

            if (newNoise != 0.0) {
                val i = builder.noiseVariables.newGarbageVar()
                xi[i] = newNoise
            }

            val newCentral: Double = central
            val newXi = HashMap(xi)

            // Compute total radius including noise symbols
            val radius = xi.values.fold(0.0) { acc, value ->
                math.add(acc, abs(value), Rounding.UP)
            }

            // Ensure some invariants and canonical representation for special cases
            when {
                // for NaN or any other input for which no reasonable processing is possible,
                // we set the result to Reals including +/- Infinity.
                central.isNaN() || radius.isNaN() -> {
                    return range(builder, min, max)
                }

                // for Infinite radius, use interval arithmetic and drop noise symbols xi.
                radius.isInfinite() -> return range(builder, min, max)
            }

            val newMax: DoubleBound?
            val newMin: DoubleBound?
            // Update min and max to the best approximation of IA and AA, iff there is valid xi.
            if (xi.isNotEmpty()) {
                newMin = max(min.toDouble(), math.sub(newCentral, radius, Rounding.DOWN)).toDoubleBound()
                newMax = min(max.toDouble(), math.add(newCentral, radius, Rounding.UP)).toDoubleBound()
            } else {
                newMin = max(min.toDouble(), newCentral - radius).toDoubleBound()
                newMax = min(max.toDouble(), newCentral + radius).toDoubleBound()
            }

            // Bring scalars into canonical representation
            if (newMin == newMax)
                return scalar(builder, newMin.toDouble())

            return when {
                // Invalid range ... we don't know anything. All Reals are safe overapproximation.
                newMin == null || newMax == null -> builder.AF.All
                // Empty AF, mapped to singleton.
                newMin > newMax -> builder.AF.Empty
                // All Reals, mapped to singleton.
                newMin.isNegativeInfinity && newMax.isPositiveInfinity -> builder.AF.All
                // Scalar. Represented by canonical form without xi.
                (newMin == newMax) && newMin.isFinite -> AffineForm(builder, newMin, newMax, newMax.toDouble(), xi)
                // Regular case, all new values in use.
                else -> AffineForm(builder, newMin, newMax, newCentral, newXi)
            }
        }

        /**
         * Builds an (extended) Affine Form with canonical representation of special cases.
         * Parameters are the states of an Affine Form.
         * @param builder the builder
         * @param range closed range for the IA part
         * @param central central value of the AA part
         * @param newNoise IA noise term of the Affine Form
         * @param xi Hashmap with the noise variables (index to Double)
         */
        fun create(
            builder: DDBuilder,
            range: RealRange,
            central: Double,
            newNoise: Double,
            xi: HashMap<Long, Double> = HashMap(300, 0.75F)
        ): AffineForm = create(builder, range.min, range.max, central, newNoise, xi)

    }

    /**
     * Creates an affine representation of a closed interval.
     *
     * If the interval contains NaN bounds, the empty affine form is returned.
     * Finite intervals are represented using one noise symbol. Infinite bounds
     * are represented as unbounded ranges without affine noise variables.
     *
     * @param builder the builder managing affine form states
     * @param range the interval to represent
     * @param i the noise symbol to use, or `null` to create no explicit noise
     * @return the affine representation of the interval
     */
    fun create(
        builder: DDBuilder,
        range: ClosedRange<Double>,
        i: Long? = null
    ): AffineForm =
        when {
            range.start.isNaN() || range.endInclusive.isNaN() -> builder.AF.All
            else ->
                AffineForm(
                    builder,
                    min = range.start.toDoubleBound()!!,
                    max = range.endInclusive.toDoubleBound()!!,
                    central = if (range.start.isFinite() && range.endInclusive.isFinite())
                        range.start / 2.0 + range.endInclusive / 2.0
                    else
                        0.0
                    ).apply {
                    if (i != null &&
                        range.start.isFinite() &&
                        range.endInclusive.isFinite() &&
                        range.start != range.endInclusive
                    ) {
                        xi[i] = range.endInclusive / 2.0 - range.start / 2.0
                    }
                }
        }
}