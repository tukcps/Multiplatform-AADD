package io.github.tukcps.aadd

import io.github.tukcps.aadd.DDBuilder.ApproximationScheme
import kotlinx.serialization.Serializable

/**
 * Allows setting parameters of various operations.
 * @param toStringVerbose whether toString reports simply a range, or the complete Shannon decomposition with ITE.
 * @param lpCallThreshold threshold below which the LP solver is not used to merge reduce over-approximation.
 * @param ddJoinLeavesThreshold "similar" numeric leaves can be joined. This allows merging leaves, reducing which reduces accuracy threshold of similarity for joining two leaves in a decision diagram which increases over-approximation.
 * the size of the DD; increases speed, reduces accuracy.
 * @param affineFormMaxNumberOfNoiseSymbols AADD library uses (constrained) affine forms that use symbols to models linear dependencies.
 * maxSymbols is the maximum size for the number of symbols; if the number is reached, symbols are reduced to have that size.
 */
@Serializable
data class DDBuilderSettings(
    var toStringVerbose:Boolean = false,
    var ddJoinLeavesThreshold:Double = 0.001,
    var affineFormMaxNumberOfNoiseSymbols: Int = 64,
    var lpCallThreshold: Double = 0.001,
    var affineFormHashMapSize: Int = 300,
    @Deprecated("Approximation scheme will be selected on split ranges depending on numerical properties")
    var affineFormLinearizationScheme: ApproximationScheme = ApproximationScheme.MinRange,
)
