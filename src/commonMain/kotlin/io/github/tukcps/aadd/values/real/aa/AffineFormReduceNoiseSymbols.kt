package io.github.tukcps.aadd.values.real

/*
enum class GarbageVarMapping{
    TIMES,EXP,SQRT,LOG,DIV, INV, roundingTIMES, roundingEXP, roundingLOG, roundingINV, roundingPLUS, roundingSCALARTIMES, roundingMINUS, roundingSCALARPLUS, POW, roundingPOW, NO, roundingSQRT
}

private fun AffineForm.addNonlinearNoise(aux: AffineForm, d: Double):AffineForm{
    aux.xi[builder.noiseVars.newGarbageVar()] = d
    aux.reduceNoiseSymbols()
    return aux
}

fun AffineForm.addNonlinearNoise(errorType: GarbageVarMapping, err: Double, nts: HashMap<Int,Double>, other: AffineForm) {
    if (this.builder.config.noiseSymbolsFlag) {
        if (this.builder.config.roundingErrorMappingFlag) {
            val newGarbageKey = this.builder.noiseVars.newGarbageVar(errorType, this, other)
            nts[newGarbageKey] = err
        } else {
            nts[builder.noiseVars.newGarbageVar()] = err
        }
    }
}

fun AffineForm.addOriginalFormsMapping(result: AffineForm, base1: Pair<AffineForm,AffineForm>){
    if (this.builder.config.originalFormsFlag){
        if (base1.second == builder.AFEmpty) {
            this.builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, builder.AFEmpty)
        }
        else (this.builder.noiseVars.newOriginalForm(AffineForm(result), base1.first, base1.second))
    }
}

fun AffineForm.reduceNoiseSymbols():AffineForm {               //When the mapping for rounding errors is active, this algorithm is reduced to combining the smallest GarbageVars (regardless of the error type)
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
} */