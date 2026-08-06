@file:Suppress("unused")

package io.github.tukcps.aadd.dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.StringValue

/**
 * ## Howto extend DD:
 * - We need first to extend the sealed Interface ScalarValue _indirectly_ via e.g. StringValue.
 * - Then define a new class that extends this interface, e.g., Str that has a "value".
 */
class Str(
    val str: String = ""
): StringValue

/**
 * ## String DD
 * A String DD ... a smart string builder.
 * Rather experimental to see how far one can go.
 *
 * - Empty = No String.
 * - Zero = Empty String
 * - One = Non-Empty String (not nice ... let's be a bit pragmatic)
 * - All = All possible Strings.
 *
 * The DD models the overall string built from the Strings at the leaves, depending on
 * the conditions.
 */
sealed class StrDD: DD<Str> {

    abstract override var builder: DDBuilder
    abstract override val index: Int

    override fun isZero(): Boolean { TODO("Not yet implemented") }
    override fun isOne(): Boolean { TODO("Not yet implemented") }

    override val infeasible get() = builder.Strings.Infeasible
    override val empty get() = builder.Strings.Empty
    override val one get() = builder.Strings.Empty
    override val zero get() = builder.Strings.Empty
    override val all get() = builder.Strings.Empty

    abstract override fun toIteString(): String
    abstract override fun clone(): StrDD

    /**
     * StrDD has exactly two internal subclasses: Leaf or Internal.
     * Leaves have a value of type String, the index DD.LEAF_INDEX and no leaves.
     */
    class Leaf(
        override var builder: DDBuilder,
        override var value: Str,
        override val status: DD.Status = DD.Status.NotSolved,
    ) : StrDD(), DD.Leaf<Str> {
        override val index: Int get() = DD.LEAF_INDEX
        override fun toIteString(): String = value.str
        override fun clone(): StrDD = builder.string(value.str)
        override fun toString(): String = value.str
    }

    /**
     * StrDD has exactly two internal subclasses: Leaf or Internal.
     * Internal nodes have an index and to child of type StrDD.
     */
    class Internal(
        override var builder: DDBuilder,
        override val index: Int,
        override val T: StrDD,
        override val F: StrDD,
        override val status: DD.Status = DD.Status.NotSolved
    ) : StrDD(), DD.Internal<Str> {
        override fun toIteString(): String = TODO("Not yet implemented")
        override fun clone(): StrDD = builder.internal(index, T.clone(), F.clone())
    }


    /**
     * Evaluate computes a StrDD that drops the conditions and internal nodes
     * that are set to True or False.
     */
    override fun evaluate(): StrDD = when (this) {
        is Leaf -> this //Do nothing on leaves
        is Internal -> {
            val cond = builder.conditions.getVariable(index)

            //IMPORTANT: DO NOT change this if-statement to a when-statement.
            //Due to casts case differentiation will not work properly!
            when {
                cond === builder.Bool.True -> T.evaluate()
                cond === builder.Bool.False -> F.evaluate()
                cond === builder.Bool.All -> builder.internal(index, T.evaluate(), F.evaluate())
                else -> builder.internal(index, T.evaluate(), F.evaluate())
            }
        }
    }

    fun equalValue(strDD: StrDD): BDD {
        return if (this is Leaf && strDD is Leaf) {
            if (this.value == strDD.value) {
                builder.Bool.True
            } else {
                builder.Bool.False
            }
        } else {
            builder.Bool.False
        }
    }

    /**
     * Applies a multiplication of the StrDD with a BDD passed as a parameter and returns result. The BDD is
     * interpreted as 1.0 for true and 0.0 for false. The result is an StrDD where the 0/1 is replaced with
     * 0/AffineForm of the AADD.
     * @param other parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
    operator fun times(other: BDD): StrDD =
        this.genericTimesBDD(other)

    operator fun plus(other: StrDD): StrDD = this.apply(other) { a: Str, b: Str -> builder.leaf(a.str + b.str) }
    infix fun intersect(other: StrDD): StrDD =
        this.apply(other) { a: Str, b: Str -> builder.leaf(a.str.filter { it in b.str }) }

    /**
     * Applies a function with two parameters on the IDD
     * @param op the function
     * @param other parameter to be applied on this.
     * @return result of binary operation on this and g.
     */
    private fun apply(other: StrDD, op: (Str, Str) -> StrDD): StrDD =
        this.applySplitGeneric(other, op)
}