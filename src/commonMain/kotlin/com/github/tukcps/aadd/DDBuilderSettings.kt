package com.github.tukcps.aadd

import kotlinx.serialization.Serializable


/**
 * Allows setting some options
 *
 * @param kotlinLPFlag
 *
 * @param noiseSymbolsFlag Whether to generate new noise symbols or use existing ones.
 *
 * @param joinTh threshold of similarity for joining two leaves in a decision diagram which increases over-approximation.
 * @param lpCallTh threshold below which the LP solver is used to merge reduce over-approximation.
 */

@Serializable
data class DDBuilderSettings(
    var kotlinLPFlag: Boolean = false,
    var noiseSymbolsFlag: Boolean = false,
    var joinTh:Double = 0.001,
    var lpCallTh: Double = 0.001,
    var toStringVerbose:Boolean = false,
    var originalFormsFlag:Boolean = false,
    var maxSymbols: Int = 200,
    var mergeSymbols: Int = 10,
    var xiHashMapSize: Int = 300,
    var lpCallsBeforeOutput: Boolean = false,
    var roundingErrorMappingFlag: Boolean = false,
    var reductionFlag: Boolean = true,
    var thresholdFlag: Boolean = false,
    var threshold: Double = 0.000000000001
)
