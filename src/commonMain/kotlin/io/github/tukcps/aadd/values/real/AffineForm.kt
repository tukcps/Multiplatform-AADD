package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDInternalError
import io.github.tukcps.aadd.util.minusUlp
import io.github.tukcps.aadd.util.plusUlp
import io.github.tukcps.aadd.util.plusMinusUlp
import io.github.tukcps.aadd.values.BoundKind
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.math.RoundingMath
import io.github.tukcps.aadd.values.real.math.IEEE754RoundingMath
import io.github.tukcps.aadd.values.real.math.Rounding
import kotlin.collections.iterator
import kotlin.math.*

/**
 * ## Affine Form
 *
 * An Affine Form. The main constructor is private and only sets the states without any checks.
 * use AffineForm.buildAF.
 * @param builder: The factory class that builds affine forms and AADD
 * @param central: The central value of the affine form
 * @param xi The noise variables of the affine form; if it is an empty set, we only use min/max.
 * @param r: An interval for avoiding excessive use of noise variables
 * Note that the affine form also has an inherited range that holds min/max values of interval arithmetic
 * computations that are used to reduce over-approximation in particular for non-linear operations.
 */
class AffineForm private constructor(
    val builder: DDBuilder,
    min: Double,
    max: Double,
    var central: Double,
    var r: Double,
    val xi: HashMap<Int, Double> = HashMap(300, 0.75F),
) : RealRange(min, max) {

    /**
     * Creates a representation of a scalar value with r = 0 and no xi.
     * Min and max are set to the respective value of.
     * @param builder the builder with the table of noise symbols.
     * @param scalar the scalar value that is represented.
     */
    constructor(builder: DDBuilder, scalar: Double):
            this(builder, min = scalar, max = scalar, central = scalar, r = 0.0)

    /**
     * Creates a representation of a range without noise symbols.
     * No partial deviations are created. Only for internal use.
     * @param builder the builder with the table of noise symbols.
     * @param min the minimum of the range.
     * @param max the maximum of the range.
     */
    internal constructor(builder: DDBuilder, min: Double, max: Double):
            this(builder, min = min, max = max, central = Double.NaN, r = Double.POSITIVE_INFINITY)

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     * @param i the noise symbol to be used; if null, a new noise symbol is generated
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>, i: Int): this(builder,
        min = range.start,
        max = range.endInclusive,
        central = if (range.start.isFinite() && range.endInclusive.isFinite())
            range.start/2.0 + range.endInclusive/2.0 else 0.0,
        r = 0.0
    ) {
        if (max != min) xi [i] = (max - min) / 2.0
    }

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>): this(builder,
        min = range.start,
        max = range.endInclusive,
        central = if (range.start.isFinite() && range.endInclusive.isFinite())
            range.start/2.0 + range.endInclusive/2.0 else 0.0,
        r = 0.0
    ) {
        if (max != min)
            xi [builder.noiseVars.newNoiseVar()] = (max - min) / 2.0
    }

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     * @param i the noise symbol to be used as a string
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>, i: String): this(builder,
        min = range.start,
        max = range.endInclusive,
        central = if (range.start.isFinite() && range.endInclusive.isFinite())
            range.start/2.0 + range.endInclusive/2.0 else 0.0,
        r = 0.0,
    ) {
        if (max != min)
            xi [builder.noiseVars.newNoiseVar(i)] = (max - min) / 2.0
    }

    /**
     * Creates an affine form as a clone of an existing affine form.
     */
    constructor(builder: DDBuilder, af: AffineForm):
            this(builder, af.min, af.max, af.central, af.r, HashMap(af.xi))

    /**
     * Creates an affine form as a clone of an existing affine form,
     * but removes all garbage noise symbols that are
     * smaller than a fixed threshold, when a flag is set.
     */
    constructor(af: AffineForm): this(
        builder = af.builder, min = af.min, max = af.max,
        central = af.central, r = af.r,
        xi = if(af.builder.config.thresholdFlag){
            val xiCopy = HashMap(af.xi)
            for ((key, value) in af.xi){
                if (key >= 10000000 && abs(value) <= abs(af.builder.config.threshold)) {
                    xiCopy.remove(key)
                }
            }
            xiCopy
        } else {
            HashMap(af.xi)
        }
    )

    /**
     * The radius of the affine form.
     */
    val radius: Double
        get() {
            if (isEmpty()) return 0.0
            var rad = 0.0
            for (v in xi.values)
                rad = math.add(rad, abs(v), Rounding.UP)
            return math.add(rad, r, Rounding.UP)
        }

    override fun clone(): AffineForm =
        if (isEmpty()) this
        else AffineForm(builder, min, max, central, r, xi)

    fun copy(min: Double?, max: Double?, r: Double? = null): AffineForm {
        if (isEmpty()) return this
        else  {
            val lb: Double = min?:this.min
            val ub: Double = max?:this.max
            return AffineForm(builder, lb, ub, central, r?:this.r, xi)
        }
    }

    /**
     * Two Affine Forms are equal if central range,
     * central value and partial deviations, r are the same.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is AffineForm) return false
        return when {
            isScalar() -> central == other.central
            isRange()  -> central.compareTo(other.central) == 0 && r.compareTo(other.r) == 0
                    && xi == other.xi && min.compareTo(other.min) == 0 && max.compareTo(other.max) == 0
            else -> true
        }
    }

    /**
     * As hashCode, we use just the min, max, central value.
     */
    override fun hashCode(): Int {
        return central.hashCode()+ min.hashCode()+ 2*max.hashCode()
    }

    /**
     * The similarity of two affine forms is measured by the amount of uncorrelated deviation
     * that would be caused by merging them both into a single affine form.
     * @param other The affine form that is compared with this.
     * @param tol The tolerance below which we consider the affine forms as similar.
     * @return true, if similar.
     */
    fun isSimilar(other: AffineForm, tol: Double): Boolean {
        if (abs(min-other.min) > tol) return false
        if (abs(max-other.max) > tol) return false
        if (other === this) return true
        var nr = abs(central - other.central)
        nr = (nr + nr.ulp) / 2.0
        for (i in xi.keys+other.xi.keys) {
            val xi = xi.getOrElse(i){0.0}
            val yi = other.xi.getOrElse(i){0.0}
            nr += if (xi * yi > 0) abs(xi - yi) else xi + yi
        }
        return nr < tol
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
        var nr = abs(math.sub(central, other.central, Rounding.UP))
        nr = math.div(nr, 2.0, Rounding.UP)
        nr = math.add(nr, r, Rounding.UP)
        nr = math.add(nr, other.r, Rounding.UP)
        nr = math.add(nr, centralErr, Rounding.UP)
        val nxi = HashMap<Int, Double>()
        for (i in xi.keys+other.xi.keys) {
            val xi = xi.getOrElse(i){0.0}
            val yi = other.xi.getOrElse(i){0.0}
            if (xi * yi > 0) {
                nxi[i] = min(abs(xi), abs(yi)) * sign(xi)
                val dif = abs(math.sub(xi, yi, Rounding.UP))
                nr = math.add(nr, dif, Rounding.UP)
            } else {
                nr = math.add(nr, abs(xi), Rounding.UP)
                nr = math.add(nr, abs(yi), Rounding.UP)
                //TODO: store value of nr as noise symbol and make sure r is zero?
            }
        }
        return buildAF(builder, this as RealRange join other, newCentral, nr, nxi)
    }

    /** Adds two affine forms   */
    operator fun plus(other: AffineForm): AffineForm {
        when {
            isEmpty() || other.isEmpty() -> return builder.AFEmpty
            isReals() || other.isReals() -> return builder.AFReals
            this.isZero() -> return other
            other.isZero() -> return this
        }
        var (newCentral, errCentral) = math.addRounded(central, other.central)
        errCentral = abs(errCentral)
        val nts = HashMap<Int, Double>(2 * this.builder.config.xiHashMapSize)
        for (i in xi.keys + other.xi.keys) {
            val v1 = xi.getOrElse(i) { 0.0 }
            val v2 = other.xi.getOrElse(i) { 0.0 }
            if (i >= this.builder.noiseVars.getBeginIndexGarbage() && abs(v1) >= builder.config.threshold && abs(
                    v2
                ) >= builder.config.threshold
            ) {
                this.builder.noiseVars.addUsed()
            }
            val sum =math.add(v1, v2, Rounding.AWAY)
            nts[i] = sum
        }
        var nr = math.add(r, other.r, Rounding.UP)
        nr = math.add(nr, errCentral, Rounding.UP)
        addNonlinearNoise(GarbageVarMapping.roundingPLUS, errCentral, nts, AffineForm(other))
        val result = buildAF(builder, this as RealRange + other as RealRange, newCentral, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    /**
     * Subtracts two affine forms and the interval is assigned the better invall approximation between IA and AA.
     **/
    operator fun minus(other: AffineForm): AffineForm {
        when {
            isEmpty() || other.isEmpty() -> return builder.AFEmpty
            isReals() || other.isReals() -> return builder.AFReals
            this.isZero() -> return -other
            other.isZero() -> return this
        }
        val nts = HashMap<Int, Double>(2 * this.builder.config.xiHashMapSize)    // new noise terms
        var (newCentral, errNewCentral) = math.subRounded(central, other.central)    // new central value
        errNewCentral = abs(errNewCentral)
        var sumOfNoiseTerms = 0.0
        for (i in xi.keys + other.xi.keys) {
            val v1 = xi.getOrElse(i){0.0}
            val v2 = other.xi.getOrElse(i){0.0}
            if (i >= this.builder.noiseVars.getBeginIndexGarbage() && abs(v1) >= builder.config.threshold && abs(v2) >= builder.config.threshold){
                this.builder.noiseVars.addUsed()
            }
            val dif = math.sub(v1, v2, Rounding.AWAY)
            sumOfNoiseTerms = math.add(sumOfNoiseTerms, abs(dif), Rounding.UP)
            if (dif != 0.0)
                nts[i] = dif
        }
        val nRadius = RealRange(((newCentral - sumOfNoiseTerms - r - other.r - errNewCentral)..(newCentral + sumOfNoiseTerms + r + other.r + errNewCentral)))
        val nRealRange = (this as RealRange - other as RealRange)
        val smallestRange = nRadius intersect nRealRange
        var nr = math.add(r, other.r, Rounding.UP)
        if (this.builder.config.noiseSymbolsFlag){
            addNonlinearNoise(GarbageVarMapping.roundingMINUS, errNewCentral, nts, AffineForm(other))
        }
        else {
            nr = math.add(nr, errNewCentral, Rounding.UP)
        }
        val result = buildAF(builder, smallestRange, newCentral, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    /** Adds a (possibly negative) scalar to an affine form. */
    override operator fun plus(other: Double): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            other.isNaN() -> return builder.AFEmpty
            other == Double.POSITIVE_INFINITY -> return AffineForm(builder, Double.POSITIVE_INFINITY)
            other == Double.NEGATIVE_INFINITY -> return AffineForm(builder, Double.NEGATIVE_INFINITY)
        }
        val newNoiseTerms = HashMap(xi)
        val (newCentral, err) = math.addRounded(central, other)
        val newR = math.add(r, abs(err), Rounding.UP)

        if (this.builder.config.noiseSymbolsFlag) {
            addNonlinearNoise(GarbageVarMapping.roundingSCALARPLUS, err, newNoiseTerms, builder.AFEmpty)
        }
        val result = buildAF(builder, this as RealRange + RealRange(other), newCentral, newR, newNoiseTerms)
        result.reduceNoiseSymbols()
        return result
    }

    override val maxKind: BoundKind
        get() = TODO("Not yet implemented")
    override val minKind: BoundKind
        get() = TODO("Not yet implemented")

    /** Adds a (possibly negative) scalar to an affine form. */
    override operator fun minus(other: Double): AffineForm = plus(-other)

    /**
     * Multiplies an affine form by a given scalar.
     * FP roundoff is considered as 1 ulp in each result.
     */
    override operator fun times(other: Double): AffineForm {
        when {
            isEmpty()      -> return builder.AFEmpty
            isReals()      -> return builder.AFReals
            other.isNaN()  -> return builder.AFEmpty
            this.isEmpty() -> return builder.AFEmpty
            this.isOne()   -> return AffineForm(builder, other)
            other == 0.0   -> return AffineForm(builder, 0.0)
            other == 1.0   -> return AffineForm(builder, this)
        }
        val nts = HashMap<Int, Double>(this.builder.config.xiHashMapSize)
        var fpArithmeticR = 0.0 // Uncertainty due to FP operations
        xi.keys.forEach {
            nts[it] = xi[it]!! * other
            fpArithmeticR += nts[it]!!.ulp
        }
        val nCentral = central * other
        fpArithmeticR += nCentral.ulp
        var nR = r * abs(other)

        if (this.builder.config.noiseSymbolsFlag){
            addNonlinearNoise(GarbageVarMapping.roundingSCALARTIMES, fpArithmeticR, nts, builder.AFEmpty)
        } else {
            nR = math.add(nR, fpArithmeticR, Rounding.UP)
        }
        val result = buildAF(builder, this as RealRange * RealRange(other), nCentral, nR, nts)
        if (this.builder.config.originalFormsFlag){
            val base1 = this.builder.noiseVars.newOriginalForm(AffineForm(this), AffineForm(this), builder.AFEmpty)
            addOriginalFormsMapping(result,base1)
        }
        result.reduceNoiseSymbols()
        return result
    }

    /** Negation; no roundoff error  */
    override operator fun unaryMinus(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
        }
        val nc = -central
        val nr = r
        val nts = HashMap<Int, Double>(this.builder.config.xiHashMapSize)
        xi.keys.forEach { nts[it] = -xi[it]!! }
        return buildAF(builder, -RealRange(this), nc, nr, nts)
    }

    /**
     * Multiplication. Uses the simpler approximation proposed by Stolfi et al.
     * instead of the more precise but costlier version. Computes interval product
     * as well and keeps the intersection of the results, minimizing error propagation.
     */
    operator fun times(other: AffineForm): AffineForm {
        when {
            this.isEmpty() || other.isEmpty()  -> return builder.AFEmpty
            this.isZero()  && other.isFinite() -> return AffineForm(builder, 0.0)
            other.isZero() && this.isFinite()  -> return AffineForm(builder, 0.0)
            this.isOne()                       -> return other
            other.isOne()                      -> return this
            this.isReals() || other.isReals()  -> return builder.AFReals
            min.isInfinite() || max.isInfinite()|| other.min.isInfinite() || other.max.isInfinite()
                                               -> return AffineForm(builder, this as RealRange * other as RealRange)
        }

        if (this.builder.config.noiseSymbolsFlag){
            val c = central * other.central
            val nr = abs(central) * other.r + abs(other.central) * r
            val noise = radius * other.radius
            val nts = HashMap<Int, Double>(2 * this.builder.config.xiHashMapSize)
            val idx: MutableSet<Int> = HashSet(xi.keys)
            var fpArithmeticR: Double = noise.ulp // Uncertainty due to FP operations
            var contained = false
            val newGarbageKey: Int
            var base1: Pair<AffineForm, AffineForm> = Pair(builder.AFEmpty, builder.AFEmpty)
            var base2: Pair<AffineForm, AffineForm> = Pair(builder.AFEmpty, builder.AFEmpty)
            if(builder.config.originalFormsFlag){
                base1 = this.builder.noiseVars.newOriginalForm(AffineForm(this), AffineForm(this), builder.AFEmpty)
                base2 = other.builder.noiseVars.newOriginalForm(AffineForm(other), AffineForm(other), builder.AFEmpty)


                newGarbageKey = when {
                    base1.second == builder.AFEmpty && base2.second == builder.AFEmpty -> {
                        this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, base1.first, base2.first)
                    }
                    base1.second == builder.AFEmpty && base2.second != builder.AFEmpty -> {
                        this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, base1.first, AffineForm(other))
                    }
                    base1.second != builder.AFEmpty && base2.second == builder.AFEmpty -> {
                        this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, AffineForm(this), base2.second)
                    }
                    else -> {
                        this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, AffineForm(this), AffineForm(other))
                    }
                }
            }
            else {
                newGarbageKey = this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, AffineForm(this), AffineForm(other))
            }
            idx.addAll(other.xi.keys)
            idx.forEach {
                val xi = if (xi.containsKey(it)) xi[it]!! else 0.0
                val yi = if (other.xi.containsKey(it)) other.xi[it]!! else 0.0
                if (it >= this.builder.noiseVars.getBeginIndexGarbage() && abs(xi) >= builder.config.threshold && abs(yi) >= builder.config.threshold){
                    this.builder.noiseVars.addUsed()
                }
                nts[it] = xi * other.central + yi * central
                fpArithmeticR += nts[it]!!.ulp
                if(it == newGarbageKey) {
                    contained = true
                    if (nts[it]!! >= 0) nts[it] = nts[it]!! + noise
                    else nts[it] = nts[it]!! - noise
                }
            }

            if(!contained){ nts[newGarbageKey]=noise }

            /* val newGarbageKey = this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, this, other)
               nts[newGarbageKey] = noise*/

            if (this.builder.config.roundingErrorMappingFlag){
                val roundingGarbageKey = this.builder.noiseVars.newGarbageVar(GarbageVarMapping.roundingTIMES,
                    AffineForm(this), AffineForm(other))
                nts[roundingGarbageKey] = fpArithmeticR
            }
            else {
                nts[builder.noiseVars.newGarbageVar()] = fpArithmeticR
            }

            val result = buildAF(builder, this as RealRange * other as RealRange, c, nr, nts)
            //if one of the affine forms is scalar, only the other one needs to update it´s original form
            if (builder.config.originalFormsFlag && base1.first != builder.AFEmpty){
                if (isScalar()&&!other.isScalar()) {
                    other.builder.noiseVars.newOriginalForm(AffineForm(result), base2.first, base2.second)
                }
                else if (!isScalar()&&other.isScalar()) {
                    this.builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, base1.second)
                }
                //none of the both affine forms is scalar
                else {//case 1: both affine forms have one original form entry
                    if (base1.second == builder.AFEmpty && base2.second == builder.AFEmpty) {
                        builder.noiseVars.newOriginalForm(AffineForm(result), base1.first,
                            base2.first)
                    }//case 2: this has one original form entry, other has two
                    else if (base1.second == builder.AFEmpty && base2.second != builder.AFEmpty){
                        builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, AffineForm(other))
                    }//case 3: this has two original forms, other has one
                    else if (base1.second != builder.AFEmpty && base2.second == builder.AFEmpty){
                        builder.noiseVars.newOriginalForm(AffineForm(result), AffineForm(this), base2.first)
                    }
                    else (builder.noiseVars.newOriginalForm(AffineForm(result),AffineForm(this), AffineForm(other)))
                }
            }
            result.reduceNoiseSymbols()
            return result
        } else {
            if (!isFinite()) return buildAF(builder, RealRange(min..max), 0.0, Double.POSITIVE_INFINITY, hashMapOf())
            val c = central * other.central
            val noise = abs(central) * other.r + abs(other.central) * r + radius * other.radius
            val nts = HashMap<Int, Double>()
            val idx: MutableSet<Int> = HashSet(xi.keys)
            var fpArithmeticR: Double = noise.ulp // Uncertainty due to FP operations
            idx.addAll(other.xi.keys)
            idx.forEach {
                val xi = if (xi.containsKey(it)) xi[it]!! else 0.0
                val yi = if (other.xi.containsKey(it)) other.xi[it]!! else 0.0
                nts[it] = xi * other.central + yi * central
                fpArithmeticR += nts[it]!!.ulp
            }

            return buildAF(builder, this as RealRange * other as RealRange, c, noise+fpArithmeticR, nts)
        }
    }


    /** Scalar addition, multiplication and noise increment on a single form */
    fun affine(alpha: Double, delta: Double, noise: Double): AffineForm {
        val nc = central * alpha + delta
        var nr = r * abs(alpha) + noise
        nr += nr.ulp + nc.ulp + central.ulp
        val nts = HashMap(xi)
        for (i in xi.keys) {
            val nval = math.mul(xi[i]!!, alpha, Rounding.AWAY)
            nts[i] = xi[i]!! * alpha
        }
        val nMin = math.add(math.mul(min, alpha, Rounding.AWAY), delta, Rounding.AWAY)
        val nMax = math.add(math.mul(max, alpha, Rounding.AWAY), delta, Rounding.AWAY)
        return buildAF(builder, RealRange(
            min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)
        ), nc, nr, nts)
    }

    enum class GarbageVarMapping{
        TIMES,EXP,SQRT,LOG,DIV, INV, roundingTIMES, roundingEXP, roundingLOG, roundingINV, roundingPLUS, roundingSCALARTIMES, roundingMINUS, roundingSCALARPLUS, POW, roundingPOW, NO, roundingSQRT
    }

    /** Scalar addition, multiplication and noise increment on a single form; mapping of GarbageVars to functions */
    fun affine(alpha: Double, delta: Double, noise: Double, fu: GarbageVarMapping): AffineForm {
        val nc = central * alpha + delta
        val nr = r * abs(alpha) /** + noise    Noise is stored in hashmap and must not be added to r anymore */
        val nts = HashMap(xi)
        var rounding = nc.ulp + central.ulp
        for (i in xi.keys) {
            val nval = xi[i]!! * alpha
            rounding += nval.ulp
            nts[i] = xi[i]!! * alpha
        }

        if (fu == GarbageVarMapping.NO){
            nts[this.builder.noiseVars.newGarbageVar()] = noise
        }
        else{
            val newGarbageKey = this.builder.noiseVars.newGarbageVar(fu, AffineForm(this), this.builder.AFEmpty)
            nts[newGarbageKey] = noise
        }

        if (this.builder.config.roundingErrorMappingFlag && fu != GarbageVarMapping.NO){
            val roundingFU : GarbageVarMapping = when (fu) {
                GarbageVarMapping.LOG -> GarbageVarMapping.roundingLOG
                GarbageVarMapping.EXP -> GarbageVarMapping.roundingEXP
                GarbageVarMapping.INV -> GarbageVarMapping.roundingINV
                GarbageVarMapping.SQRT -> GarbageVarMapping.roundingSQRT
                else -> { error("Missing rounding error type in affine(alpha, delta, noise, fu) function of AffineForm.kt") }
            }
            val newRoundingKey = this.builder.noiseVars.newGarbageVar(roundingFU, AffineForm(this), builder.AFEmpty)
            nts[newRoundingKey] = rounding
        }
        else {
            nts[builder.noiseVars.newGarbageVar()] = rounding
        }

        val nMin = math.add(math.mul(min, alpha, Rounding.AWAY), delta, Rounding.AWAY)
        val nMax = math.add(math.mul(max, alpha, Rounding.AWAY), delta, Rounding.AWAY)
        val result =  buildAF(builder, RealRange(
            min(nMin - noise, nMax - noise), max(nMin + noise, nMax + noise)
        ), nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }
    /** Adapted affine function for pow(other:Double) */
    fun affine(alpha: Double, delta: Double, noise: Double, fu: GarbageVarMapping, other: AffineForm): AffineForm {
        val nc = central * alpha + delta
        val nr = r * abs(alpha) /** + noise    Noise is stored in hashmap and must not be added to r anymore */
        val nts = HashMap(xi)
        var rounding = nc.ulp + central.ulp
        for (i in xi.keys) {
            val nval = xi[i]!! * alpha
            rounding += nval.ulp
            nts[i] = xi[i]!! * alpha
        }
        val newGarbageKey = this.builder.noiseVars.newGarbageVar(fu, AffineForm(this), AffineForm(other))
        nts[newGarbageKey] = noise

        if (this.builder.config.roundingErrorMappingFlag){
            val roundingFU : GarbageVarMapping = when (fu) {
                GarbageVarMapping.POW -> GarbageVarMapping.roundingPOW
                else -> {
                    error("Missing rounding error type in affine(alpha, delta, noise, fu, other) function of AffineForm.kt")
                }
            }
            val newRoundingKey = this.builder.noiseVars.newGarbageVar(roundingFU, AffineForm(this), AffineForm(other))
            nts[newRoundingKey] = rounding
        }
        else {
            nts[builder.noiseVars.newGarbageVar()] = rounding
        }

        var nMin = min * alpha + delta
        nMin -= nMin.ulp
        var nMax = max * alpha + delta
        nMax += nMax.ulp
        val result =  buildAF(builder, RealRange(
            min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)
        ), nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    /** Scalar addition, multiplication and noise increment on a single form; mapping of GarbageVars to functions and affine forms to their origin */
    fun affine(alpha: Double, delta: Double, noise: Double, fu: GarbageVarMapping, base: Pair<AffineForm,AffineForm>): AffineForm {
        val nc = central * alpha + delta
        val nr = r * abs(alpha) /* Noise is stored in hashmap and must not be added to r anymore */
        val nts = HashMap(xi)
        var rounding = nc.ulp + central.ulp
        val newGarbageKey = this.builder.noiseVars.newGarbageVar(fu, base.first, base.second)
        var contained = false
        for (i in xi.keys) {
            val nval = xi[i]!! * alpha
            rounding += nval.ulp
            nts[i] = nval
            if(i == newGarbageKey) {
                contained = true
                if (nval >= 0) nts[i] = nval + noise
                else nts[i] = nval - noise
            }
        }
        if (!contained){
            nts[newGarbageKey] = noise
        }
        if (this.builder.config.roundingErrorMappingFlag){
            val roundingFU : GarbageVarMapping = when (fu) {
                GarbageVarMapping.INV -> GarbageVarMapping.roundingINV
                else -> {
                    error("Missing rounding error type in affine(alpha, delta, noise, fu, base) function of AffineForm.kt")
                }
            }
            val newRoundingKey = this.builder.noiseVars.newGarbageVar(roundingFU, AffineForm(this), builder.AFEmpty)
            nts[newRoundingKey] = rounding
        }
        else {
            nts[builder.noiseVars.newGarbageVar()] = rounding
        }

        var nMin = min * alpha + delta
        nMin -= nMin.ulp
        var nMax = max * alpha + delta
        nMax += nMax.ulp

        val result = buildAF(builder, RealRange(
            min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)
        ), nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    private fun addNonlinearNoise(aux: AffineForm, d: Double):AffineForm{
        aux.xi[builder.noiseVars.newGarbageVar()] = d
        aux.reduceNoiseSymbols()
        return aux
    }

    private fun addNonlinearNoise(errorType: GarbageVarMapping, err: Double, nts: HashMap<Int,Double>, other: AffineForm) {
        if (this.builder.config.noiseSymbolsFlag) {
            if (this.builder.config.roundingErrorMappingFlag) {
                val newGarbageKey = this.builder.noiseVars.newGarbageVar(errorType, this, other)
                nts[newGarbageKey] = err
            } else {
                nts[builder.noiseVars.newGarbageVar()] = err
            }
        }
    }

    private fun addOriginalFormsMapping(result: AffineForm, base1: Pair<AffineForm,AffineForm>){
        if (this.builder.config.originalFormsFlag){
            if (base1.second == builder.AFEmpty) {
                this.builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, builder.AFEmpty)
            }
            else (this.builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, base1.second))
        }
    }


    fun reduceNoiseSymbols():AffineForm {               //When the mapping for rounding errors is active, this algorithm is reduced to combining the smallest GarbageVars (regardless of the error type)
        if (this.builder.config.reductionFlag && this.xi.size>this.builder.config.maxSymbols) {
            var nval = 0.0
            var mini: Double? = null
            var mkey = 0
            while (xi.size>this.builder.config.maxSymbols){
                for (i in 1..this.builder.config.mergeSymbols){   // search for the smallest GarbageVars that are not mapped to functions
                    for (entries in xi){
                        if (entries.key>=builder.noiseVars.getBeginIndexGarbage() && !this.builder.noiseVars.nonLinearNoise.containsKey(entries.key)){
                            if (mini == null){
                                mini = entries.value
                                mkey = entries.key
                            }
                            else {
                                if (abs(entries.value) < abs(mini)){
                                    mini = entries.value
                                    mkey = entries.key
                                }
                            }
                        }
                    }
                    if (mini == null) { //only used if there are no GarbageVars without mapping left
                        for (entries in xi){
                            if (entries.key>=builder.noiseVars.getBeginIndexGarbage()){
                                if (mini == null){
                                    mini = entries.value
                                    mkey = entries.key
                                }
                                else {
                                    if (abs(entries.value) < abs(mini)){
                                        mini = entries.value
                                        mkey = entries.key
                                    }
                                }
                            }
                        }
                    }
                    if (mini != null) {
                        nval += abs(mini)
                        xi.remove(mkey)
                        mini = null
                        mkey = 0
                    }
                    else { //there are no further GarbageVars that could be reduced
                        if(nval!=0.0){
                            nval += nval.ulp
                            xi[builder.noiseVars.newGarbageVar()] = nval
                        }
                        return this
                    }
                }
                nval += nval.ulp
                xi[builder.noiseVars.newGarbageVar()] = nval

            }
            return this
        }
        return this
    }
    /**
     * ceiling function for AFs
     */
    override fun ceil() : AffineForm {
        val lb = ceil(this.min)
        val ub = ceil(this.max)
        val c = (lb + ub) / 2.0
        var r = abs(lb - ub) / 2.0
        r += r.ulp
        val xi: HashMap<Int, Double> = HashMap() // not being used
        val result =  buildAF(builder, lb .. ub, c, r, xi)
        return result
    }

    override fun invCeil() : AffineForm {
        var lb = this.min - 1.0
        var ub = this.max
        val c = (lb + ub) / 2.0
        var r = abs(ub - lb) / 2.0
        lb -= lb.ulp
        ub += ub.ulp
        r += r.ulp
        val xi: HashMap<Int, Double> = HashMap() // not being used

        return buildAF(builder, lb .. ub, c, r, xi)
    }

    /**
     * ceiling function for AFs
     */
    override fun ceilAsLong() : Long  = ceil(this.max).toLong()

    /**
     * ceiling function for AFs, also converts to IntegerRange
     * @return IntegerRange
     */
    fun ceilToIntRange() : IntegerRange = IntegerRange(ceil(this.max).toLong())

    /** Exponentiation */
    override fun exp(): AffineForm {
        when {
            isEmpty()   -> return builder.AFEmpty
            isReals()   -> return builder.AFReals
            isScalar()  -> return AffineForm(builder, exp(central).plusMinusUlp())
        }

        val fMin = exp(min).minusUlp()
        val fMax = exp(max).plusUlp()
        var alpha: Double = fMin
        var delta: Double = (fMax + alpha * (1.0 - min - max)) / 2.0
        var noise: Double = (fMax + alpha * (min - max - 1.0)) / 2.0

        if(builder.scheme==DDBuilder.ApproximationScheme.Chebyshev && max-min>0.000000001 ) {
            alpha = (fMax- fMin)/(max-min)
            val touchingPoint = ln(alpha)
            delta = (fMin + alpha -  alpha * (min + touchingPoint)) / 2.0
            noise = abs((alpha - fMin - alpha * (touchingPoint-min )) / 2.0)
        }
        var aux = affine(alpha, delta, noise)
        if (this.builder.config.noiseSymbolsFlag) {
            aux = affine(alpha, delta, noise, GarbageVarMapping.EXP)
            aux.reduceNoiseSymbols()
        }
        return buildAF(builder, RealRange(fMin, fMax), aux.central , aux.r, aux.xi )
    }

    /**
     * Exponentiation
     */
    override fun pow(other : Double): AffineForm {
        when {
            isZero()  -> return this // 0^other = Zero
            isOne()   -> return this  // 1^other = One
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            other == 1.0 -> return this
            other == 0.0 -> return AffineForm(builder, 1.0)
            isScalar()  -> return AffineForm(builder, central.pow(other).plusMinusUlp())
        }

        val fMin = this.min.pow(other).minusUlp()
        val fMax = this.max.pow(other).plusUlp()
        val iaMin = min(fMin, fMax)
        val iaMax = max(fMin, fMax)
        if(min<0.0 ) {
            if (floor(other) == other) {
                if (max >= 0.0)
                // extrema of 0.0 included and cannot reasonably keep the correlation to the noise symbol in the range
                // [min,max] as linear transformation
                    return AffineForm(builder, min(iaMin, 0.0) .. iaMax)
                else {
                    // f'(x)*f''(x) <= for even and odd exponent
                    var alpha = other * max.pow(other - 1)
                    var delta = (fMax + fMin - alpha * (min + max)) / 2.0
                    var noise = abs(fMax - fMin - alpha * (max - min)) / 2.0
                    // for smaller ranges MinRange is as good as Chebyshev
                    if(builder.scheme==DDBuilder.ApproximationScheme.Chebyshev && max-min>0.0001){
                        alpha = (max.pow(other)-min.pow(other))/(max-min)
                        val touchingPoint =(alpha/other).pow(1/(other-1))
                        delta = (fMin + touchingPoint.pow(other) - alpha * (min + touchingPoint)) / 2.0
                        noise = abs(touchingPoint.pow(other) - fMin - alpha * (touchingPoint - min)) / 2.0
                    }
                    //pythonVisualApprox(alpha,delta,noise,"np.power(x,$other)")
                    return if(builder.config.noiseSymbolsFlag){
                        affine(alpha, delta, noise, GarbageVarMapping.POW, AffineForm(builder, other))
                    } else {
                        affine(alpha, delta, noise)
                    }

                }
            } else {
                return builder.AFReals
            }
        }
        //min range approximation according to rump
        var alpha = other * min.pow(other-1)
        var delta = (fMax + fMin - alpha *( min + max)) / 2.0
        var noise = abs(fMax - fMin -alpha * (max - min )) / 2.0
        if(builder.scheme==DDBuilder.ApproximationScheme.Chebyshev && max-min>0.0001){
            alpha = (max.pow(other)-min.pow(other))/(max-min)
            val touchingPoint =(alpha/other).pow(1/(other-1))
            delta = (fMin + touchingPoint.pow(other) - alpha * (min + touchingPoint)) / 2.0
            noise = abs(touchingPoint.pow(other) - fMin - alpha * (touchingPoint - min)) / 2.0
        }
        var aux = affine(alpha, delta, noise)
        if(builder.config.noiseSymbolsFlag){
            aux = affine(alpha, delta, noise, GarbageVarMapping.POW, AffineForm(builder, other))
        }
        //pythonVisualApprox(alpha,delta,noise,"np.power(x,$other)")
        return buildAF(builder, RealRange(iaMin, iaMax), aux.central, aux.r, aux.xi)
    }

    /** Exponentiation */
    fun pow(y : AffineForm): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            y.isScalar() -> return pow(y.max)
            y.isZero() -> return AffineForm(builder, 1.0)
            y.isOne() -> return AffineForm(builder, this)
            this.isZero() -> return AffineForm(builder, 0.0)
            this.isOne()  -> return AffineForm(builder, 1.0)
        }

        // if the result of computation is 0.0-0.ulp (e. g. through the constructor of Range.times) that will be the input for the power
        // function is the commented in code should catch only the one ulp overapproximation and nudges to 0.0 so that not NaN is the result
        // if (min<0.0) return builder.AFRealsNaN
        val minGe0 = if(min<0.0) 0.0 else min
        if (!y.max.isFinite()) {
            return AffineForm(builder, (minGe0.pow(y.min)).minusUlp() .. Double.POSITIVE_INFINITY)
        }
        val iaMult = listOf(minGe0.pow(y.min), minGe0.pow(y.max), max.pow(y.min), max.pow(y.max))
        val iaMin = iaMult.minOrNull()!!
        val iaMax = iaMult.maxOrNull()!!
        /*val fMin = min(this.min.pow(y.min),this.min.pow(y.max))
        val fMax = max(this.max.pow(y.min),this.max.pow(y.max))
        var iaMin = fMin
        var iaMax = fMax*/

        val workPointX = (max+min)/2
        var workPointY = y.min
        if(y.min <0){
            if(y.max <=0){
                //the recipropale function is monotone decreasing
                workPointY=y.max
            }else{
                workPointY= (y.max+y.min)/2
            }
        }

        //inspired from a min range approximation according to rump
        val alphaX = workPointY * workPointX.pow( workPointY -1)
        val alphaY = ln(workPointX)*workPointX.pow(workPointY)
        // t(x,y) = alphaX*(x-x.min)+ alphaY*(y-y.min)+pow(min,y.min)
        val mimiDiff = minGe0.pow(y.min)- (alphaX*(minGe0-workPointX)+ alphaY*(y.min-workPointY)+workPointX.pow(workPointY))
        val mimaDiff = minGe0.pow(y.max)- (alphaX*(minGe0-workPointX)+ alphaY*(y.max-workPointY)+workPointX.pow(workPointY))
        val mamiDiff = max.pow(y.min)- (alphaX*(this.max-workPointX)+ alphaY*(y.min-workPointY)+workPointX.pow(workPointY))
        val mamaDiff = max.pow(y.max)- (alphaX*(this.max-workPointX)+ alphaY*(y.max-workPointY)+workPointX.pow(workPointY))
        val differenceToReal = listOf(mimiDiff,mimaDiff,mamiDiff,mamaDiff)

        val posNoise = differenceToReal.maxOrNull()!!
        val negNoise = differenceToReal.minOrNull()!!
        val delta : Double
        if(posNoise > abs(negNoise) && posNoise > 0) delta =posNoise/2
        else delta = negNoise/2

        val tx = (this-workPointX)*alphaX+workPointX.pow(workPointY)
        var ty = y.affine(alphaY,-alphaY*workPointY+delta,abs(delta))
        if(builder.config.noiseSymbolsFlag){
            ty = y.affine(alphaY,-alphaY*workPointY+delta,abs(delta), GarbageVarMapping.NO)
        }
        var res = tx + ty

        if(res.max<iaMax){
            val diffToMax= iaMax-res.max
            var e = diffToMax/2
            if(builder.config.noiseSymbolsFlag){
                res.xi[this.builder.noiseVars.newGarbageVar()] = e
                e = 0.0
            }
            res = buildAF(builder, RealRange(iaMin, iaMax),res.central+ diffToMax/2,res.r+ e,res.xi)
        }
        if(res.min>iaMin){
            val diffToMin= res.min-iaMin
            var e = diffToMin/2
            if(builder.config.noiseSymbolsFlag){
                res.xi[this.builder.noiseVars.newGarbageVar()] = e
                e = 0.0
            }
            return buildAF(builder, RealRange(iaMin, iaMax),res.central- diffToMin/2,res.r+ e,res.xi)
        }
        return buildAF(builder, RealRange(iaMin, iaMax),res.central,res.r,res.xi)
    }

    /**
     * floor function for AFs
     */
    override fun floor() : AffineForm {
        var lb = floor(this.min)
        var ub = floor(this.max)
        val c = (lb + ub) / 2.0
        var r = abs(lb - ub) / 2.0
        lb -= lb.ulp
        ub += ub.ulp
        r += r.ulp
        val xi: HashMap<Int, Double> = HashMap() // not being used
        return buildAF(builder, lb..ub, c, r, xi)
    }

    override fun invFloor() : AffineForm {
        var lb = this.min
        var ub = this.max + 1.0
        val c = (lb + ub) / 2.0
        var r = abs(ub - lb) / 2.0
        lb -= lb.ulp
        ub += ub.ulp
        r += r.ulp
        val xi: HashMap<Int, Double> = HashMap() // not being used

        return buildAF(builder, lb..ub, c, r, xi)
    }

    /**
     * floor function for AFs
     */
    override fun floorAsLong() : Long = floor(this.min).toLong()

    /**
     * floor function for AFs
     */
    fun floorToIntRange() : IntegerRange = IntegerRange(floor(this.min).toLong())

    /*
     * 2^x function for AffineForms
     */
    fun power2(): AffineForm =
        times(ln(2.0)).exp()

    /**
     * min-Range approximation of square root
     */
    override fun sqrt(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            isOne()   -> return AffineForm(builder, 1.0)
            isZero()  -> return AffineForm(builder, 0.0)
            isScalar()-> return if (central < 0.0) builder.AFEmpty
                            else AffineForm(builder, sqrt(central).plusMinusUlp())
        }

        val l = max(min, 0.0)
        val u = max(max, 0.0)
        val fMax = if (u > 0) sqrt(u).plusUlp() else 0.0
        val fMin = if (l > 0) sqrt(l).minusUlp() else 0.0

        var alpha = 1.0 / (2 * fMax)
        var delta = (2*fMax*fMin+u-l)/(4 * fMax)
        var noise = (fMax-fMin)*(fMax-fMin)/(4 * fMax)

        if(builder.scheme==DDBuilder.ApproximationScheme.Chebyshev&& max-min>0.0001) {
            alpha = 1 / (fMax + fMin)
            val touchingPoint = 1 / (4 * alpha.pow(2))
            delta = (fMin + sqrt(touchingPoint) - alpha * (min + touchingPoint)) / 2.0
            noise = abs(sqrt(touchingPoint) - fMin - alpha * (touchingPoint - min)) / 2.0
        }
        noise += (noise.ulp + alpha.ulp + delta.ulp) + (u+l).ulp + (u-l).ulp

        val af: AffineForm = if (this.builder.config.noiseSymbolsFlag){
            affine(alpha, delta, max(0.0, noise), GarbageVarMapping.SQRT)
        } else {
            affine(alpha, delta, max(0.0, noise))
        }
        //pythonVisualApprox(alpha,delta,noise,"np.sqrt(x)")

        af.min = fMin
        af.max = fMax

        if (this.builder.config.noiseSymbolsFlag){
            af.reduceNoiseSymbols()
        }
        return af
    }

    fun root(other: AffineForm) = pow( builder.real(1.0) / other )

    /*
     /**
      * sqrt-Root as a piece-wise approximated AADD
      * @param extraUncertainty maximal overapproximation in the individual intervals. Default: 1
      * @return the AADD with the piece-wise approximation from the 'this' AffineForm
      * TODO piecewise stuff
      */

     fun sqrt(extraUncertainty: Double = 1.0, extraIntervals: Int = -1): AADD {
         val sqrt = Sqrt()
         val approx = ApproximationScheme()

         val definitionRange = this.builder.leaf(this).greaterThanOrEquals(0.0)
         val approximation = approx.wrapperApproxLinearStraigthForward(
             sqrt,
             this,
             extraUncertainty,
             extraIntervals,
             true,
             max(0.0, this.min),
             max(0.0, this.max)
         )

         return definitionRange.ite(approximation, builder.RealsNaN)
     }*/
    fun log(base:AffineForm):AffineForm = log().div(base.log())
    /** Natural logarithm */
    override fun log(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            min <= 0.0 -> return AffineForm(builder, Double.NEGATIVE_INFINITY, ln(max).plusUlp())
        }

        val l = ln(min).minusUlp()
        val u = ln(max).plusUlp()

        // It prevents however the division by 0 or near-0.
        // use min range approximation for small intervals
        val alpha = if ((max-min) < 0.00001 || builder.scheme==DDBuilder.ApproximationScheme.MinRange){
            1/max
        } else {(u - l) / (max - min)}
        val touchingPoint = 1 / alpha
        val logxs = ln(touchingPoint)
        val delta = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
            (ln(min*max)-min/max-1)/2
        else(logxs + l-alpha*(min+touchingPoint)) / 2
        val noise = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
            (abs(ln(max/min)+min/max-1)/2)
        else abs(logxs - l - alpha * (touchingPoint - min)  ) / 2
        var af = affine(alpha, delta, noise)
        if (this.builder.config.noiseSymbolsFlag) {
            af = affine(alpha, delta, noise, GarbageVarMapping.LOG)
            af.reduceNoiseSymbols()
        }
        af.min = l
        af.max = u

        //pythonVisualApprox(alpha,delta,noise,"np.log(x)")
        return af
    }

    /**
     * We do division by multiplying by inv(other) as suggested by Stolfi.
     * Division by zero returns infinity.
     */
    operator fun div(other: AffineForm): AffineForm =
        times(other.inv())

    /**
     * Reciprocal, a min-Range Approximations which gives us division.
     * Based on "Self-validated numerical methods and applications" by Stolfi and de Figueiredo (p.69-70 3.12 Reciprocal)
     */
    override fun inv(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            max.isInfinite() && min.isInfinite() -> return builder.AFReals
            isScalar() -> return if (central == 0.0) builder.AFEmpty
                else AffineForm(builder, (1/central).minusUlp() .. (1/central).plusUlp())
            min == 0.0 -> return AffineForm(builder, (1/max).minusUlp(), Double.POSITIVE_INFINITY)
            max == 0.0 -> return AffineForm(builder, Double.NEGATIVE_INFINITY, (1/min).plusUlp())
        }

        if (0.0 in this) return builder.AFReals // The result is infinity if 0 is included, but not in border
        val l = min(abs(min), abs(max))
        val u = max(abs(min), abs(max))
        var alpha = -1.0 / (u * u)
        //val auxLow = 2.0 / u
        //val auxUpp = 1.0 / l - alpha * l
        val den = if (min < 0.0) -2.0 else 2.0
        //val delta = (auxUpp + auxLow) / den
        //val noise = (auxUpp - auxLow) / 2.0
        var delta = (u+l)*(u+l)/(den*u*u*l)
        var noise = (u-l)*(u-l)/(2*u*u*l)
        noise += (noise.ulp + alpha.ulp + delta.ulp) + (u+l).ulp + (u-l).ulp

        if(builder.scheme==DDBuilder.ApproximationScheme.Chebyshev){
            alpha=-1.0/(max*min)
            var touchingPoint = sqrt(1/-alpha)
            if(min<0)touchingPoint*=-1
            delta = (1.0/min + 1.0/touchingPoint - alpha * (min + touchingPoint)) / 2.0
            noise = abs(1.0/touchingPoint - 1/min - alpha * (touchingPoint - min) )/ 2.0
        }
        //pythonVisualApprox(alpha,delta,noise,"1/x")
        val af: AffineForm

        when (Pair(first = builder.config.noiseSymbolsFlag, second = builder.config.originalFormsFlag)) {
            Pair(true,true)->{
                val base1 = this.builder.noiseVars.newOriginalForm(AffineForm(this), AffineForm(this), builder.AFEmpty)
                af = affine(alpha, delta, max(0.0, noise), GarbageVarMapping.INV, base1)
                af.min = min(1.0/max, 1/min).minusUlp().minusUlp()
                af.max = max(1.0/max, 1/min).plusUlp().plusUlp()
                addOriginalFormsMapping(af,base1)
                af.reduceNoiseSymbols()
                return af
            }
            Pair(true,false)-> {
                af = affine(alpha, delta, max(0.0, noise), GarbageVarMapping.INV)
            }
            else -> {
                af = affine(alpha, delta, max(0.0, noise))
            }
        }
        af.min = min(1.0/max, 1/min).minusUlp().minusUlp()
        af.max = max(1.0/max, 1/min).plusUlp().plusUlp()

        af.reduceNoiseSymbols()
        return af
    }


    /**
     * Returns a symbolic representation of the affine form
     * noise variables are e_i with 'i' being the index
     * Form: c+r+sum_i a_i e_i
     * */
    fun toSymbolicString(): String {
        var str = "${central}+${r}"
        for(noiseSymbol in xi) {
            str+= "+${noiseSymbol.value}e_${noiseSymbol.key}"
        }
        return str
    }

    companion object {
        val math: RoundingMath = IEEE754RoundingMath

        /**
         * Builds an (extended) Affine Form with canonical representation of special cases.
         * Parameters are the states of an Affine Form.
         * @param builder the builder
         * @param range closed range for the IA part
         * @param central central value of the AA part
         * @param r IA noise term of the Affine Form
         * @param xi Hashmap with the noise variables (index to Double)
         */
        fun buildAF(
            builder: DDBuilder,
            range: ClosedRange<Double> = -Double.MAX_VALUE .. Double.MAX_VALUE,
            central: Double,
            r: Double,
            xi: HashMap<Int, Double> = HashMap(300, 0.75F)
        ): AffineForm {
            var newCentral: Double = central
            var newR: Double = r
            var newMax = range.endInclusive
            var newMin = range.start

            // Compute radius to check for problems later
            var radius = 0.0
            xi.forEach {
                radius = math.add(abs(it.value), radius, Rounding.UP)
            }
            radius = math.add(newR, radius, Rounding.UP)

            // Ensure some invariants and canonical representation for special cases
            when {
                // for NaN or any other input for which no reasonable processing is possible,
                // we set the result to Reals including +/- Infinity.
                central.isNaN() || r.isNaN() || radius.isNaN() -> {
                    xi.clear()
                    newCentral = Double.NaN
                    newR = Double.POSITIVE_INFINITY
                }

                // for Infinite radius, use interval arithmetic and drop noise symbols xi.
                radius.isInfinite() -> {
                    xi.clear()
                    newCentral = Double.NaN
                    newR = Double.POSITIVE_INFINITY
                }

                // Something is really wrong; possibly a bug in code, if negative r is requested
                r < 0 -> throw DDInternalError("Constructor of AffineForm called with negative r; r must be > 0")
            }

            // Update min and max to the best approximation of IA and AA
            if (xi.isNotEmpty()) {
                newMin = max(range.start, math.sub(central, radius, Rounding.DOWN))
                newMax = min(range.endInclusive, math.add(central, radius, Rounding.UP))
            }

            // Bring scalars into canonical representation
            if (range.start == range.endInclusive) {
                newCentral = range.start
                xi.clear()
            }
            return AffineForm(builder, newMin, newMax, newCentral, newR, xi)
        }
    }
}
