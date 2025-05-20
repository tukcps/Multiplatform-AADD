package com.github.tukcps.aadd.values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.DDInternalError
import com.github.tukcps.aadd.util.minusUlp
import com.github.tukcps.aadd.util.plusUlp
import com.github.tukcps.aadd.util.plusMinusUlp
import kotlin.math.*


/**
 * An Affine Form. The main constructor is private as it is only for use by de-serialization.
 * Deserialization requires a default-builder that is a mandatory parameter for all other constructors.
 * @param builder: The factory class that builds affine forms and AADD
 * @param central: The central value of the affine form
 * @param xi The noise variables of the affine form; if it is an empty set, we only use min/max.
 * @param r: An interval for avoiding excessive use of noise variables
 * Note that the affine form also has an inherited range that holds min/max values of interval arithmetic
 * computations that are used to reduce over-approximation in particular for non-linear operations.
 */
class AffineForm(
    var builder: DDBuilder,
    range: ClosedRange<Double> = -Double.MAX_VALUE .. Double.MAX_VALUE,
    var central: Double,
    var r: Double,
    val xi: HashMap<Int, Double> = HashMap(300, 0.75F),
) : Range(range) {
    // Ensure some invariants and canonical representation for exceptions
    init {
        when {
            // for NaN or any other input for which no reasonable processing is possible,
            // we set the result to Reals including +/- Infinity.
            central.isNaN() || r.isNaN() || radius.isNaN() -> {
                xi.clear()
                central = Double.NaN
                r = Double.POSITIVE_INFINITY
            }

            // for Infinite radius, use interval arithmetic and drop noise symbols xi.
            radius.isInfinite() -> {
                xi.clear()
                central = Double.NaN
                r = Double.POSITIVE_INFINITY
            }

            // Something is really wrong; possibly a bug in code, if negative r is requested
            r < 0 -> throw DDInternalError("constructor of AffineForm called with negative r; r must be > 0")
        }

        // Update min and max to the best approximation of IA and AA
        if (xi.isNotEmpty()) {
            min = max(min, central-radius.plusUlp())
            max = min(max, central+radius.plusUlp())
        }
        // Bring scalars into canonical representation
        if (min == max) {
            central = min
            xi.clear()
        }
    }

    /**
     * Creates a representation of a scalar value with r = 0 and no xi.
     * Min and max are set to the respective value of.
     * @param builder the builder with the table of noise symbols.
     * @param scalar the scalar value that is represented.
     */
    constructor(builder: DDBuilder, scalar: Double):
            this(builder, range = scalar .. scalar, central = scalar, r = 0.0)

    /**
     * Creates a representation of a range without noise symbols.
     * No partial deviations are created. Only for internal use.
     * @param builder the builder with the table of noise symbols.
     * @param min the minimum of the range.
     * @param max the maximum of the range.
     */
    internal constructor(builder: DDBuilder, min: Double, max: Double):
            this(builder,
                range   = min..max,
                central = Double.NaN,
                r       = Double.POSITIVE_INFINITY
            )

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     * @param i the noise symbol to be used; if null, a new noise symbol is generated
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>, i: Int):
            this(builder, central = 0.0, r = 0.0, range = range) {
        central = if (min.isFinite() && max.isFinite()) max/2.0 + min/2.0 else 0.0
        if ( i == -1)
            throw DDInternalError("-1 as marker for generating new noise symbol is deprecated.")
        if (max != min)
            xi [i] = (max - min) / 2.0
    }

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>):
            this(builder, central = 0.0, r = 0.0, range = range) {
        central = if (min.isFinite() && max.isFinite()) max/2.0 + min/2.0 else 0.0
        if (max != min)
            xi [builder.noiseVars.newNoiseVar()] = (max - min) / 2.0
    }

    /**
     * Creates a range representation that uses, if possible, an affine form
     * @param builder the builder with the table of noise symbols
     * @param range the range to be represented
     * @param i the noise symbol to be used as a string
     */
    constructor(builder: DDBuilder, range: ClosedRange<Double>, i: String):
            this(builder, central = 0.0, r = 0.0, range = range) {
        central = if (min.isFinite() && max.isFinite()) max/2.0 + min/2.0 else 0.0
        if (max != min)
            xi [builder.noiseVars.newNoiseVar(i)] = (max - min) / 2.0
    }

    /**
     * Creates an affine form as a clone of an existing affine form.
     */
    constructor(builder: DDBuilder, af: AffineForm):
            this(builder, af, af.central, af.r, HashMap(af.xi))

    /**
     * Creates an affine form as a clone of an existing affine form,
     * but removes all garbage noise symbols that are
     * smaller than a fixed threshold, when a flag is set.
     */
    constructor(af: AffineForm): this(
        builder = af.builder,
        range = af,
        central = af.central,
        r = af.r,
        xi = if(af.builder.config.thresholdFlag){
            val xiCopy = HashMap(af.xi)
            for (entries in af.xi){
                if (entries.key >= 10000000 && abs(entries.value) <= abs(af.builder.config.threshold)) {
                    xiCopy.remove(entries.key)
                }
            }
            xiCopy
        } else {
            HashMap(af.xi)
        },
    )

    /**
     * The radius of the affine form.
     */
    val radius: Double
        get() {
            if (isEmpty()) return 0.0
            var rad = 0.0
            for (v in xi.values) {
                rad += abs(v); rad += rad.ulp
            }
            return rad+r
        }

    override fun clone(): AffineForm {
        if (isEmpty()) return this
        else return AffineForm(builder, Range(min .. max), central, r, xi)
    }

    override fun copy(min: Double?, max: Double?): AffineForm {
        if (isEmpty()) return this
        else  {
            val lb: Double = min?:this.min
            val ub: Double = max?:this.max
            return AffineForm(builder, Range(lb .. ub), central, r, xi)
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
        val nc = (central + other.central) / 2
        var nr = abs(central - other.central)
        nr = (nr + 2 * nr.ulp) / 2
        nr += r
        nr += nr.ulp
        nr += other.r
        nr += nr.ulp
        val nxi = HashMap<Int, Double>()
        for (i in xi.keys+other.xi.keys) {
            val xi = xi.getOrElse(i){0.0}
            val yi = other.xi.getOrElse(i){0.0}
            if (xi * yi > 0) {
                nxi[i] = min(abs(xi), abs(yi)) * sign(xi)
                nr += abs(xi - yi); nr += nr.ulp
            } else {
                nr += abs(xi); nr += nr.ulp
                nr += abs(yi); nr += nr.ulp //TODO: store value of nr as noise symbol and make sure r is zero?
            }
        }
        return AffineForm(builder, this as Range join other, nc, nr, nxi)
    }

    /** Adds two affine forms   */
    operator fun plus(other: AffineForm): AffineForm {
        when {
            isEmpty() || other.isEmpty() -> return builder.AFEmpty
            isReals() || other.isReals() -> return builder.AFReals
            this.isZero() -> return other
            other.isZero() -> return this
        }
        val nc = central + other.central
        var err = nc.ulp
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
            val sum = v1 + v2
            err += sum.ulp
            nts[i] = sum
        }
        var nr = r + other.r
        if (this.builder.config.noiseSymbolsFlag) {
            err += err.ulp
            addNonlinearNoise(GarbageVarMapping.roundingPLUS, err, nts, AffineForm(other))
        } else {
            nr += err
            nr += nr.ulp
        }
        val result = AffineForm(builder, this as Range + other, nc, nr, nts)
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
        val nc = central - other.central    // new central value
        var err = nc.ulp*2                  // rounding errors
        val nts = HashMap<Int, Double>(2 * this.builder.config.xiHashMapSize)    // new noise terms
        var nSum = 0.0
        for (i in xi.keys + other.xi.keys) {
            val v1 = xi.getOrElse(i){0.0}
            val v2 = other.xi.getOrElse(i){0.0}
            if (i >= this.builder.noiseVars.getBeginIndexGarbage() && abs(v1) >= builder.config.threshold && abs(v2) >= builder.config.threshold){
                this.builder.noiseVars.addUsed()
            }
            val dif = v1 - v2
            nSum += abs(dif)
            err += dif.ulp
            if (dif != 0.0)
                nts[i] = dif
        }
        val nRadius = Range(((nc-nSum-r-other.r-err) .. (nc+nSum+r+other.r+err)))
        val nRange = (this as Range - other as Range)
        val smallestRange = nRadius intersect nRange
        var nr = r + other.r
        if (this.builder.config.noiseSymbolsFlag){
            err += err.ulp
            addNonlinearNoise(GarbageVarMapping.roundingMINUS, err, nts, AffineForm(other))
        }
        else {
            nr += nr.ulp
            nr += err
            nr += nr.ulp
        }
        val result = AffineForm(builder, smallestRange, nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    /** Adds a (possibly negative) scalar to an affine form. */
    override operator fun plus(other: Double): AffineForm {
        if (isEmpty()) return builder.AFEmpty
        if (isReals()) return builder.AFReals
        if (other.isNaN()) return builder.AFEmpty
        if (other == Double.POSITIVE_INFINITY) return AffineForm(builder, Double.POSITIVE_INFINITY)
        if (other == Double.NEGATIVE_INFINITY) return AffineForm(builder, Double.NEGATIVE_INFINITY)
        val nc = central + other
        val nts = HashMap(xi)
        val err = 2 * nc.ulp
        var nr = r
        if (this.builder.config.noiseSymbolsFlag) {
            addNonlinearNoise(GarbageVarMapping.roundingSCALARPLUS, err, nts, builder.AFEmpty)
        }
        else {
            nr += 2 * nc.ulp // noise symbol modeling quantization error.
        }
        val result = AffineForm(builder, this as Range + Range(other), nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

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
                // Issue: some other functions might have under-approximation? The following is needed to pass tests.
            other == 1.0 -> return AffineForm(builder, this.copy(min, max + max.ulp))
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
            nR += nR.ulp + fpArithmeticR
        }
        val result = AffineForm(builder, this as Range * Range(other), nCentral, nR, nts)
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
        return AffineForm(builder, -Range(this), nc, nr, nts)
    }

    /**
     * Multiplication. Uses the simpler approximation proposed by Stolfi et al
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
                                               -> return AffineForm(builder, this as Range * other as Range)
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

            if(!contained){
                nts[newGarbageKey]=noise
            }

            /*        val newGarbageKey = this.builder.noiseVars.newGarbageVar(GarbageVarMapping.TIMES, this, other)
                    nts[newGarbageKey] = noise*/

            if (this.builder.config.roundingErrorMappingFlag){
                val roundingGarbageKey = this.builder.noiseVars.newGarbageVar(GarbageVarMapping.roundingTIMES, AffineForm(this), AffineForm(other))
                nts[roundingGarbageKey] = fpArithmeticR
            }
            else {
                nts[builder.noiseVars.newGarbageVar()] = fpArithmeticR
            }

            val result = AffineForm(builder, this as Range * other as Range, c, nr, nts)
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
            if (!isFinite()) return AffineForm(builder, Range(min..max), 0.0, Double.POSITIVE_INFINITY, hashMapOf())
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

            return AffineForm(builder, this as Range * other as Range, c, noise+fpArithmeticR, nts)
        }
    }


    /** Scalar addition, multiplication and noise increment on a single form */
    fun affine(alpha: Double, delta: Double, noise: Double): AffineForm {
        val nc = central * alpha + delta
        var nr = r * abs(alpha) + noise
        nr += nr.ulp + nc.ulp + central.ulp
        val nts = HashMap(xi)
        for (i in xi.keys) {
            val nval = xi[i]!! * alpha
            nr += nval.ulp
            nts[i] = xi[i]!! * alpha
        }

        var nMin = min * alpha + delta
        nMin -= nMin.ulp
        var nMax = max * alpha + delta
        nMax += nMax.ulp
        return AffineForm(builder, Range(min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)), nc, nr, nts)
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
                else -> {
                    error("Missing rounding error type in affine(alpha, delta, noise, fu) function of AffineForm.kt")
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
        val result =  AffineForm(builder, Range(min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)), nc, nr, nts)
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
        val result =  AffineForm(builder, Range(min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)), nc, nr, nts)
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

        val result = AffineForm(builder, Range(min(nMin - noise, nMax - noise),
            max(nMin + noise, nMax + noise)), nc, nr, nts)
        result.reduceNoiseSymbols()
        return result
    }

    private fun addNonlinearNoise(aux: AffineForm, d: Double):AffineForm{
        aux.xi[builder.noiseVars.newGarbageVar()] = d
        aux.reduceNoiseSymbols()
        return aux
    }

    private fun addNonlinearNoise(errorType: GarbageVarMapping, err: Double, nts: HashMap<Int,Double>, other: AffineForm):HashMap<Int,Double> {
        if (this.builder.config.roundingErrorMappingFlag){
            val newGarbageKey = this.builder.noiseVars.newGarbageVar(errorType, this, other)
            nts[newGarbageKey] = err
        }
        else {
            nts[builder.noiseVars.newGarbageVar()] = err
        }
        return nts
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
        val result =  AffineForm(builder, lb .. ub, c, r, xi)
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

        return AffineForm(builder, lb .. ub, c, r, xi)
    }

    /**
     * ceiling function for AFs
     */
    override fun ceilAsLong() : Long  = ceil(this.max).toLong()

    /**
     * ceiling function for AFs, also converts to IntegerRange
     * @return IntegerRange
     */
    fun ceiltoIntRange() : IntegerRange = IntegerRange(ceil(this.max).toLong())

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
            var touchingPoint = ln(alpha)
            delta = (fMin + alpha -  alpha * (min + touchingPoint)) / 2.0
            noise = abs((alpha - fMin - alpha * (touchingPoint-min )) / 2.0)
        }
        var aux = affine(alpha, delta, noise)
        if (this.builder.config.noiseSymbolsFlag) {
            aux = affine(alpha, delta, noise, GarbageVarMapping.EXP)
            aux.reduceNoiseSymbols()
        }
        //pythonVisualApprox(alpha,delta,noise,"np.exp(x)")
        /*if (aux.min.compareTo(fMin) > 0) {
            val d = aux.min - fMin
            var e = d
            // NOTE: PLOP uses central + d, but I think that's a typo/bug, as
            // we decrease min, so it doesn't make sense to increase central.
            if (this.builder.config.noiseSymbolsFlag){
                addNonlinearNoise(aux, d)
                e = 0.0
            }
            return AffineForm(builder, Range(fMin, aux.max), aux.central - d, aux.r + e, aux.xi)
        } else if (aux.min.compareTo(0.0) < 0) {
            val d = Double.MIN_VALUE - aux.min
            var e = d
            if (this.builder.config.noiseSymbolsFlag){
                addNonlinearNoise(aux,d)
                e = 0.0
            }
            return AffineForm(builder, Range(Double.MIN_VALUE, aux.max), aux.central + d, aux.r + e, aux.xi )
        }*/
        return AffineForm(builder, Range(fMin, fMax), aux.central , aux.r, aux.xi )
    }

    /**
     * Exponentiation
     */
    override fun pow(other : Double): AffineForm {
        when {
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
                    } else{
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
        return AffineForm(builder, Range(iaMin,iaMax),aux.central,aux.r,aux.xi)
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
            res = AffineForm(builder, Range(iaMin,iaMax),res.central+ diffToMax/2,res.r+ e,res.xi)
        }
        if(res.min>iaMin){
            val diffToMin= res.min-iaMin
            var e = diffToMin/2
            if(builder.config.noiseSymbolsFlag){
                res.xi[this.builder.noiseVars.newGarbageVar()] = e
                e = 0.0
            }
            return AffineForm(builder, Range(iaMin,iaMax),res.central- diffToMin/2,res.r+ e,res.xi)
        }
        return AffineForm(builder, Range(iaMin,iaMax),res.central,res.r,res.xi)
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
        return AffineForm(builder, lb..ub, c, r, xi)
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

        return AffineForm(builder, lb..ub, c, r, xi)
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

        val af: AffineForm
        if (this.builder.config.noiseSymbolsFlag){
            af = affine(alpha, delta, max(0.0, noise), GarbageVarMapping.SQRT)
        }
        else {
            af = affine(alpha, delta, max(0.0, noise))
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
      * TODO piecwise stuff
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
        af.min = l - l.ulp
        af.max = u + l.ulp

        //pythonVisualApprox(alpha,delta,noise,"np.log(x)")
        return af
    }

    /**
     * logarithm of a number using a specified base
     */
    fun log(base : Double): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return builder.AFReals
            // isScalar() -> return AffineForm(builder, ln(min).plusMinusUlp())
            min <= 0.0 && max.isFinite()
                      -> return AffineForm(builder, Double.NEGATIVE_INFINITY.. (ln(max / ln(base)).plusUlp()))
        }
        val l = (ln(min) / ln(base)).minusUlp()
        val u = (ln(max) / ln(base)).plusUlp()
        // It prevents however the division by 0 or near-0.
        // use min range approximation for small intervals
        val alpha = if ((max-min) < 0.00001 || builder.scheme==DDBuilder.ApproximationScheme.MinRange){
            1/(max*ln(base))
        } else {(u - l) / (max - min)}
        val touchingPoint = 1 / (alpha *ln(base))
        val ys = (touchingPoint - min) * alpha + l
        val logxs = ln(touchingPoint)/ln(base)
        val delta = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
            (ln(min*max)-min/max-1)/2
        else(logxs + l-alpha*(min+touchingPoint)) / 2
        val noise = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
            (abs(ln(max/min)+min/max-1)/2).plusUlp()
        else
            (abs(logxs - l - alpha * (touchingPoint - min)  ) / 2).plusUlp()
        val af =  affine(alpha, delta, noise)
        af.min = l
        af.max = u
        //pythonVisualApprox(alpha,delta,noise,"np.log(x)/np.log($base)")
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
     */
    override fun inv(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            maxIsInf && minIsInf -> return builder.AFReals
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
                af.min = min(1.0/max, 1/min)  //* min and max must be adapted before result is mapped to its original form */
                af.min -= 2.0 * af.min.ulp
                af.max = max(1.0/max, 1/min)
                af.max += 2.0 * af.max.ulp
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
        af.min = min(1.0/max, 1/min)
        af.min -= 2.0 * af.min.ulp
        af.max = max(1.0/max, 1/min)
        af.max += 2.0 * af.max.ulp

        af.reduceNoiseSymbols()
        return af
    }

    /**
     * absolute value define with abs(x)=if(x<0) return -x
     * taking the longes subinterval if 0.0 is contained and restraining the min max values
     */
    fun abs(): AffineForm{
        // in that cause the interval can only be reduced and the longest overlapping of corralation be used
        if(contains(0.0)){
            val highValue= max(max,-min)
            val res : AffineForm
            val neg = this * -1.0
            res = if (central<0.0) AffineForm(builder, 0.0 .. highValue, neg.central, neg.r, neg.xi)
            else AffineForm(builder, 0.0 .. highValue, central,r,xi)

            return res
        }else{
            return if (min>0) this
            else this * -1.0
        }
    }

    /**
     * computation of sin on the interval on this
     * If the range does not include an extrema the best linear function close to
     * 9 equal distant sample on sin is computed with the least square method and scaled with the affine method
     */
    fun sin():AffineForm{
        when {
            isEmpty()  -> return builder.AFEmpty
            isReals()  -> return AffineForm(builder, -1.0 .. 1.0)
            isScalar() -> return AffineForm(builder, sin(central).plusMinusUlp())
        }
        // ruling out some interval width that makes linear approximation uncorrelated to the actual direvation
        if (max - min > PI - 0.2)
            return AffineForm(builder,  Range(-1.0 .. 1.0), 0.0, 1.0, HashMap())
        val periodeK = floor(max / (2 * PI))
        // is the interval on the monotone rising part of sin (considered the periodicty ) or on the monotone falling part?
        if (min - periodeK * 2 * PI > -PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < PI / 2 +10.0.pow(-6) || min - periodeK * 2 * PI > PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < 3 * PI / 2 + 10.0.pow(-6) ) {
            val numSamples = 9
            //val f0 = DoubleArray(numSamples) { _ -> 1.0 }
            val f1 = DoubleArray(numSamples) { i -> min + i * (max - min) / (numSamples - 1) }
            var sumF1 = 0.0
            var squareSumF1 = 0.0
            //val f = arrayOf(f0, f1)
            val y = DoubleArray(numSamples) { i -> sin(f1[i]) }
            var sumY = 0.0
            var sumXY = 0.0
            for (i in f1.indices) {
                // F.transpose*F
                sumF1 += f1[i]
                squareSumF1 += f1[i] * f1[i]
                //F.transpose*y
                sumY += y[i]
                sumXY += f1[i] * y[i]
            }

            val FtxF0 = DoubleArray(2)
            val FtxF1 = DoubleArray(2)
            val FtxF = arrayOf(FtxF0, FtxF1)
            FtxF[0][0] = numSamples * 1.0
            FtxF[0][1] = sumF1
            FtxF[1][0] = sumF1
            FtxF[1][1] = squareSumF1
            val det = 1 / (FtxF[0][0] * FtxF[1][1] - FtxF[0][1] * FtxF[1][0])
            val inverse0 = DoubleArray(2)
            val inverse1 = DoubleArray(2)
            val inverse = arrayOf(inverse0, inverse1)
            inverse[0][0] = det * FtxF[1][1]
            inverse[0][1] = -det * FtxF[0][1]
            inverse[1][0] = -det * FtxF[1][0]
            inverse[1][1] = det * FtxF[0][0]
            // should be the approximation of alpha*x+delta
            val delta = inverse[0][0] * sumY + inverse[0][1] * sumXY
            val alpha = inverse[1][0] * sumY + inverse[1][1] * sumXY

            val caseLessPIHalv = min - periodeK * 2 * PI > -PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < PI / 2 +10.0.pow(-6)
            val maxValue = if(caseLessPIHalv)
                if(max>PI/2.0) PI/2.0
                else max
            else if (min<PI/2.0) PI/2.0
            else min
            val minValue = if(caseLessPIHalv)
                if(min<-PI/2.0) -PI/2.0
                else min
            else if(max>3*PI/2.0) 3*PI/2.0
            else max
            val noiseMax = max(0.0, (sin(maxValue) - (alpha * maxValue + delta)))
            val noiseMin = max(0.0, ((alpha * minValue + delta) - sin(minValue)))
            val noise = max(noiseMax, noiseMin)
            //println("max: " + maxValue+" "+ max + " sin(max): " + sin(maxValue) + " approx(max): " + (alpha * maxValue + delta) + "+ noise: " + noise)
            //println("min: " + minValue+" "+ min + " sin(min): " + sin(minValue) + " approx(min): " + (alpha * minValue + delta) + "- noise: " + noise)
            val res = affine(alpha, delta, noise)

            return AffineForm(builder,  Range(min(sin(min),sin(max)),max(sin(min),sin(max))), res.central, res.r, res.xi)
        } else return AffineForm(builder,  Range(-1.0 .. 1.0), 0.0, 1.0, HashMap())

    }

    fun cos(): AffineForm {
        return this.plus(PI/2.0).sin()
    }

    /**
     * Strictly monotonous growing, -1 .. 1 -> -PI/2 .. +PI/2
     */
    fun arcsin(): AffineForm {
        when {
            isEmpty() -> return builder.AFEmpty
            isReals() -> return AffineForm(builder, asin(-1.0).minusUlp() .. asin(1.0).plusUlp())
            isScalar() -> return AffineForm(builder, asin(central).plusMinusUlp())
        }
        val lb = max(min, -1.0)
        val ub = min(max, 1.0)

        val f = Range(asin(min), asin(max))
        /** Use the first four terms of the Taylor series of arcsin to approximate the function*/
        val alpha1 = 1 + central.pow(2.0).times(1.0/6.0) + central.pow(4.0).times(3.0/40.0) + central.pow(6.0).times(15.0/336.0)
        val alpha2 = 1 + lb.pow(2.0).times(1.0/6.0) + lb.pow(4.0).times(3.0/40.0) + lb.pow(6.0).times(15.0/336.0)
        val alpha3 = 1 + ub.pow(2.0).times(1.0/6.0) + ub.pow(4.0).times(3.0/40.0) + ub.pow(6.0).times(15.0/336.0)

        /** Choose the largest alpha to avoid under-approximation*/
        val alpha = max(max(alpha1,alpha2),alpha3)

        val noiseMax = max(0.0, (asin(ub)- ub * alpha))
        val noiseMin = max(0.0, (min * alpha - asin(min)))
        val noise = max(noiseMax, noiseMin)

        val res = affine(alpha, 0.0, noise)

        return AffineForm(builder,  Range(min(asin(lb),asin(ub)),max(asin(lb),asin(ub))), res.central, res.r, res.xi)
    }

    /**
     * arccos implementation; strictly monotonous falling, -1..1 -> 0 .. PI
     */
    fun arccos(): AffineForm = -(this.arcsin().minus(PI/2.0)) /* arccos(x) = PI/2 - arcsin(x) */

    /**
     * Comparison of affine forms:
     *  - We compare the range of this and other.
     *  - If this is for sure larger that the other, we return 1,
     *  - If the other is for sure larger that this, we return -1,
     *  - else we return 0.
     *
     *  Note that this comparison is uncertain for the result 0 as a a more
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
}

/** extension functions by Jack */
fun ceil(input : AffineForm) : AffineForm = input.ceil()
fun floor(input : AffineForm) : AffineForm = input.floor()
fun log(base : Double, arg : AffineForm) : AffineForm = arg.log(base)
fun pow(base : AffineForm, exp : Double) : AffineForm = base.pow(exp)
fun pow(base : AffineForm, exp : AffineForm) : AffineForm = base.pow(exp)
