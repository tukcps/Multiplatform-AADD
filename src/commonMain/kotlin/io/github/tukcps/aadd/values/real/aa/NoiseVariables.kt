package io.github.tukcps.aadd.values.real.aa

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDException
import kotlin.math.abs

/**
 * This class manages the noise variables.
 * - provides unique indexes, starting with maxIndex
 * - maintains information on kind and documentation
 * @param builder The builder for dependency injection
 */
class NoiseVariables(val builder: DDBuilder) {

    /**
     * The maximum index for noise terms that define concrete values.
     * We use index numbers from 1, each new index increases maxIndex.
     */
    private var maxIndex: Long = 0L

    /**
     * The maximum index for noise terms that stem from approximation and linearization.
     * We use index numbers from 1, each new index increases maxIndex.
     */
    private var maxIndexGarbage: Long = 0L

    /**
     * String-based ids for each noise variable index.
     */
    private var names = HashMap<Long, String>()

    /** HashMap that keeps track how often a nonlinear noise mapping is used **/
    private var used = HashMap <Long, Int>(300, 0.75F)

    /** Mapping affine forms that are multiplied with a scalar to their original affine form */
    private var originalAffineForm = HashMap <AffineForm, Pair<AffineForm, AffineForm>>(300,0.75F)

    /** HashMap that keeps track how often an original form mapping is used **/
    private var timesused = HashMap <AffineForm, Int>(300,0.75F)


    /**
     * Returns a new noise variable with optional id String.
     * To be used for user-defined values or intermediate values where value is modeled.
     * @param id String for identification, e.g., a UUID or a name.
     * @return index of the noise variable.
     */
    fun newNoiseVar(id: String? = null): Long {
        if (id == null) {
            if (maxIndex < Long.MAX_VALUE){
                return ++maxIndex
            } else {
                throw DDException("max index exceeds maximum length (Long.MAX_VALUE)")
            }
        }
        for ((index, name) in names)
            if (id == name) return index
        maxIndex += 1
        names[maxIndex] = id
        return maxIndex
    }

    /**
     * Returns a new noise variable that models rounding and approximation errors.
     * @return index of the noise variable, will be negative.
     */
    fun newGarbageVar(): Long {
        if (maxIndexGarbage > Long.MIN_VALUE)
            return --maxIndexGarbage
        else
            throw DDException("max index exceeds maximum length (Long.MIN_VALUE)")
    }

    /**
     * Reduces the number of "garbage" noise variables.
     * @param xi The map of noise terms of an affine form.
     */
    fun compressGarbageVariables(xi: HashMap<Long, Double>) {
        class NoiseTerm(val id: Long, val coefficient: Double, val magnitude: Double)
        val max = builder.settings.affineFormMaxNumberOfNoiseSymbols
        if (xi.size <= max) return

        val targetSize = max / 2
        val mergeCount = xi.size - targetSize + 1

        val candidates = ArrayList<NoiseTerm>()

        for ((id, coefficient) in xi) {
            if (id < 0L && coefficient.isFinite()) {
                candidates.add(NoiseTerm(id = id, coefficient = coefficient, magnitude = abs(coefficient)))
            }
        }

        val actualCount = minOf(mergeCount, candidates.size)
        if (actualCount < 2) return

        if (actualCount < candidates.size) {
            candidates.sortWith(compareBy<NoiseTerm> { it.magnitude }.thenBy { it.id })
        }

        var mergedRadius = 0.0

        for (i in 0 until actualCount) {
            val term = candidates[i]
            xi.remove(term.id)
            mergedRadius += term.magnitude
        }

        xi[newGarbageVar()] = mergedRadius
    }

    override fun toString(): String {
        var s = "Noise variables: (max=$maxIndex): "
        for( (key, doc) in names) {
            s+=("$key->$doc, ")
        }
        return "$s)"
    }
}
