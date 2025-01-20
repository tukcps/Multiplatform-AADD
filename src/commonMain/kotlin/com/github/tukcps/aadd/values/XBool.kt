package com.github.tukcps.aadd.values


import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.XBool.Companion.False
import com.github.tukcps.aadd.values.XBool.Companion.NaB
import com.github.tukcps.aadd.values.XBool.Companion.True
import com.github.tukcps.aadd.values.XBool.Companion.X
import kotlinx.serialization.Serializable

/**
 * The interface XBool serves as an abstraction of a multivalued Boolean variable.
 * It can take the values
 * - True,
 * - False,
 * - X (for unknown in the sense that it can be refined to True or False),
 * - NaB (Not-a-Boolean; for the case that a value or dependency is not "Feasible" by a path condition)
 */
interface XBool {
    // Value
    val value: XBool

    // The constants ...
    companion object {
        val True: XBool = XBoolImpl(XBoolImpl.XBoolEnum.True)
        val False: XBool = XBoolImpl(XBoolImpl.XBoolEnum.False)
        val X: XBool = XBoolImpl(XBoolImpl.XBoolEnum.X)
        val NaB: XBool = XBoolImpl(XBoolImpl.XBoolEnum.NaB)

        fun valueOf(s: String): XBool = when(s) {
            "True" -> True
            "False" -> False
            "X" -> X
            "NaB" -> NaB
            else -> TODO()
        }
    }

    override fun equals(other: Any?): Boolean
    fun intersect(other: XBool): XBool
    operator fun contains(other: XBool): Boolean
    operator fun not(): XBool
    infix fun and(other: XBool): XBool
    infix fun or(other: XBool): XBool


    fun valueOf(dd: DD<*>): XBool =
        when(dd) {
            dd.builder.True -> True
            dd.builder.False -> False
            dd.builder.Bool -> X
            dd.builder.NaB -> NaB
            else -> TODO(" Experimental NOT in use ")
        }

    fun bddLeafOf(builder: DDBuilder): BDD.Leaf =
        when(this) {
            True -> builder.True
            False -> builder.False
            X -> builder.Bool
            NaB -> builder.NaB
            else -> TODO()
        }
}


@Serializable
class XBoolImpl(private val xBoolEnum: XBoolEnum = XBoolEnum.X): XBool {

    override val value: XBool get() =  when(xBoolEnum) {
        XBoolEnum.True -> True
        XBoolEnum.False -> False
        XBoolEnum.X -> X
        XBoolEnum.NaB -> NaB
    }
    enum class XBoolEnum  {
        True,
        False,              // False
        X,                  // True or False, e.g., external unknown input
        NaB;                // Neither True nor False, e.g., Predicate on value that is NaN
    }

    override fun toString(): String =
        when(this.value) {
            True   -> "True"
            False  -> "False"
            NaB    -> "Contradiction"
            // InfeasibleB -> "Infeasible"
            X   -> "Unknown"
            else  -> "BDD leaf: None of True, False, NaB, X"
        }

    override fun intersect(other: XBool): XBool =
        xBoolIntersect[Pair(this, other)]!!

    override operator fun contains(other: XBool): Boolean =
        xBoolContains[Pair(this, other)]!!

    override infix fun and(other: XBool): XBool =
        xBoolAnd[Pair(this, other)]!!

    override infix fun or(other: XBool): XBool =
        xBoolOr[Pair(this, other)]!!

    override fun not(): XBool = when(this.xBoolEnum) {
            XBoolEnum.False -> True
            XBoolEnum.True -> False
            XBoolEnum.X -> X
            XBoolEnum.NaB -> NaB
    }

    override fun equals(other: Any?): Boolean = when {
        (this === other)   -> true
        (other is BDD)     -> other.value == this
        (other is Boolean) -> if (other) this == True else this == False // Handles comparison with Boolean (true, false)
        else -> false
    }

    override fun hashCode(): Int {
        return xBoolEnum.hashCode()
    }
}

val xBoolContains: HashMap<Pair<XBool, XBool>, Boolean> = hashMapOf(
    Pair(True, True ) to true,
    Pair(True, False) to false,
    Pair(True, X) to false,
    Pair(True, NaB) to false,

    Pair(False, True ) to false,
    Pair(False, False) to true,
    Pair(False, X) to false,
    Pair(False, NaB) to false,

    Pair(X, True ) to true,
    Pair(X, False) to true,
    Pair(X, X) to true,
    Pair(X, NaB) to false,

    Pair(NaB, True ) to false,
    Pair(NaB, False) to false,
    Pair(NaB, X) to false,
    Pair(NaB, NaB) to true,
)

/** the intersect operation on two Xbool checks for the possible equality */
val xBoolIntersect: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to NaB,
    Pair(True, X) to True,
    Pair(True, NaB) to NaB,

    Pair(False, True ) to NaB,
    Pair(False, False) to False,
    Pair(False, X) to False,
    Pair(False, NaB) to NaB,

    Pair(X, True ) to True,
    Pair(X, False) to False,
    Pair(X, X) to X,
    Pair(X, NaB) to NaB,

    Pair(NaB, True ) to NaB,
    Pair(NaB, False) to NaB,
    Pair(NaB, X) to NaB,
    Pair(NaB, NaB) to NaB,
)

val xBoolAnd: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to False,
    Pair(True, X) to X,
    Pair(True, NaB) to NaB,

    Pair(False, True ) to False,
    Pair(False, False) to False,
    Pair(False, X) to False,
    Pair(False, NaB) to NaB,

    Pair(X, True ) to X,
    Pair(X, False) to False,
    Pair(X, X) to X,
    Pair(X, NaB) to NaB,

    Pair(NaB, True ) to NaB,
    Pair(NaB, False) to NaB,
    Pair(NaB, X) to NaB,
    Pair(NaB, NaB) to NaB,
)


val xBoolOr: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to True,
    Pair(True, X) to True,
    Pair(True, NaB) to NaB,

    Pair(False, True ) to True,
    Pair(False, False) to False,
    Pair(False, X) to False,
    Pair(False, NaB) to NaB,

    Pair(X, True ) to True,
    Pair(X, False) to X,
    Pair(X, X) to X,
    Pair(X, NaB) to NaB,

    Pair(NaB, True ) to NaB,
    Pair(NaB, False) to NaB,
    Pair(NaB, X) to NaB,
    Pair(NaB, NaB) to NaB,
)
