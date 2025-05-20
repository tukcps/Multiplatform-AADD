package com.github.tukcps.aadd

import com.github.tukcps.aadd.values.AffineForm

/**
 * This class manages the noise variables.
 * - provides unique indexes, starting with maxIndex
 * - maintains information on kind and documentation
 */
class NoiseVariables {
    /**
     * The maximum index.
     * We use index numbers from 1, each new index increases maxIndex.
     * So that the indexes stay unique (normal NoiseSymbols don't collide with Gaussian),
     * we need to define the beginning/first index of the gaussian
     */
    private var maxIndex = 0
    // Gaussian start also with 1 because it is a separate hashmap
    private var beginIndexGarbage = 10000000
    private var maxIndexGarbage  = beginIndexGarbage-1
    private val maxSize = 200
    private val freeSpace = 20
    /** Counts the number of times operations are mapped to the same symbol and the computations in which the error was reduced due to the mapping.*/
    private var usageCounter = 0

    /** A set of names for each noise variable index. */
    internal var names = HashMap<Int, String>()

    /** Mapping nonlinear operations and their operands to the resulting noise symbol */
    /** "overloaded" for round-off errors */
    internal var nonLinearNoise = HashMap <Int, Triple<AffineForm.GarbageVarMapping, AffineForm, AffineForm>>(300,0.75F)

    /** HashMap that keeps track how often a nonlinear noise mapping is used **/
    private var used = HashMap <Int, Int>(300,0.75F)

    /** Mapping affine forms that are multiplied with a scalar to their original affine form */
    private var originalAffineForm = HashMap <AffineForm, Pair<AffineForm, AffineForm>>(300,0.75F)

    /** HashMap that keeps track how often an original form mapping is used **/
    private var timesused = HashMap <AffineForm, Int>(300,0.75F)

    fun getCurrentMaxIndex() : Int { return maxIndex }

    fun getCurrentMaxIndexGarbage() : Int { return maxIndexGarbage }

    /** Returns a new index of a noise variable. */
    fun newNoiseVar(): Int {
        if (maxIndex<beginIndexGarbage){
            return ++maxIndex
        }else {
            beginIndexGarbage +=10000
            return ++maxIndex
        }
    }

    /** Returns a new noise variable with name. */
    fun newNoiseVar(n: String): Int {
        for ((index, name) in names)
            if (n == name) return index
        maxIndex += 1
        names[maxIndex] = n
        return maxIndex
    }

    fun newGarbageVar(): Int {
        maxIndexGarbage += 1
        return maxIndexGarbage
    }

    fun getBeginIndexGarbage():Int = beginIndexGarbage

    /** removes the least used mappings, when the maxsize of the HashMap is exceeded,
     * to ensure an efficient runtime */
    private fun reduceNonLinearMapping(){
        var i = 0
        val copyUsed = HashMap(used)
        var size = nonLinearNoise.size
        while (size>=maxSize){
            for ((k,number) in copyUsed) {
                if (number==i) {
                    nonLinearNoise.remove(k)
                    used.remove(k)
                    size--
                }
                if (size <= maxSize-freeSpace) return
            }
            i++
        }
    }
    private fun reduceOriginalMapping() {
        var i = 0
        val copyTimesUsed = HashMap(timesused)
        var size = this.originalAffineForm.size
        while (size >=maxSize){
            for ((k,number) in copyTimesUsed) {
                if (number==i) {
                    this.originalAffineForm.remove(k)
                    timesused.remove(k)
                    size--
                }
                if (size <= maxSize-freeSpace) return
            }
            i++
        }
    }
    /** Returns index and increases usage counter if GarbageVar already exists.
    * Otherwise, a new GarbageVar with usage counter and mapping is created and maxIndex is increased*/
    fun newGarbageVar(f: AffineForm.GarbageVarMapping, af1: AffineForm, af2: AffineForm): Int{
        if(nonLinearNoise.size>=maxSize){
            reduceNonLinearMapping()
        }
        for ((index, entry ) in nonLinearNoise)
            if (entry.first == f && entry.second == af1 && entry.third == af2){ //TODO: Forge if and else if into one "or"
                if (used[index] != null){
                    used[index] = used[index]!! + 1
                }
                else used[index] = 1
                usageCounter += 1
                return index
            }
            else if (entry.first == f && entry.second == af2 && entry.third == af1){
                if (used[index] != null){
                    used[index] = used[index]!! + 1
                }
                else used[index] = 1
                usageCounter += 1
                return index
            }
        //Works for the operations that are used at the moment.
        // Has to be adapted if, e.g., division gets its own mapping.
        maxIndexGarbage += 1
        nonLinearNoise[maxIndexGarbage] = Triple(f,af1,af2)
        used[maxIndexGarbage] = 0
        return maxIndexGarbage
    }

    fun getUsed(): Int{
        return usageCounter
    }

    fun addUsed(){
        usageCounter+=1
    }
    /** Returns original forms and increases usage counter if entry already exists
    * Otherwise, a new entry and counter is created */
    fun newOriginalForm(a: AffineForm, o1: AffineForm, o2: AffineForm): Pair<AffineForm,AffineForm> {
        if(originalAffineForm.size>=maxSize){
            reduceOriginalMapping()
        }
        for ((form, original) in originalAffineForm)
            if (a == form){
                if (timesused[a] != null){
                    timesused[a] = timesused[a]!! + 1
                }
                else timesused[a] = 1

                return original
            }
        originalAffineForm[a] = Pair(o1,o2)
        timesused[a] = 0
        return Pair(o1,o2)
    }

    override fun toString(): String {
        var s = "Noise variables: (max=$maxIndex): "
        for( (key, doc) in names) {
            s+=("$key->$doc, ")
        }
        return "$s)"
    }
}
