package io.github.tukcps.aadd.values.bool


import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.DD
import io.github.tukcps.aadd.values.XBoolValue
import io.github.tukcps.aadd.values.bool.XBool.Companion.All
import io.github.tukcps.aadd.values.bool.XBool.Companion.Empty
import io.github.tukcps.aadd.values.bool.XBool.Companion.False
import io.github.tukcps.aadd.values.bool.XBool.Companion.True

/**
 * The interface XBool serves as an abstraction of a multivalued Boolean variable.
 * It can take the values
 * - True,
 * - False,
 * - XBool (for unknown in the sense that it can be refined to True or False),
 * - Empty (Not-a-Boolean; for the case that a value or dependency is not "Feasible" by a path condition)
 */
interface XBool: XBoolValue {
    // Value
    val value: XBool

    // The constants ...
    companion object {
        val Empty: XBool = XBoolImpl(XBoolImpl.XBoolEnum.Empty)
        val True: XBool = XBoolImpl(XBoolImpl.XBoolEnum.True)
        val False: XBool = XBoolImpl(XBoolImpl.XBoolEnum.False)
        @Deprecated("Replace with All", ReplaceWith("All"))
        val XBool: XBool get() = All
        val All: XBool = XBoolImpl(XBoolImpl.XBoolEnum.XBool)

        fun valueOf(s: String): XBool = when(s) {
            "True" -> True
            "False" -> False
            "X" -> All
            "All" -> All
            "NaB" -> Empty
            "Empty" -> Empty
            else -> TODO()
        }
    }

    override fun equals(other: Any?): Boolean
    fun intersect(other: XBool): XBool
    operator fun contains(other: XBool): Boolean

    fun valueOf(dd: DD<*>): XBool =
        when(dd) {
            dd.builder.Bool.True -> True
            dd.builder.Bool.False -> False
            dd.builder.Bool.All -> All
            dd.builder.Bool.Empty -> Empty
            else -> TODO(" Experimental NOT in use ")
        }

    fun bddLeafOf(builder: DDBuilder): BDD.Leaf =
        when(this) {
            True -> builder.Bool.True
            False -> builder.Bool.False
            All -> builder.Bool.All
            Empty -> builder.Bool.Empty
            else -> TODO()
        }
}

class XBoolImpl(
    private val xBoolEnum: XBoolEnum = XBoolEnum.XBool
): XBool {

    override val value: XBool get() =  when(xBoolEnum) {
        XBoolEnum.True -> True
        XBoolEnum.False -> False
        XBoolEnum.XBool -> All
        XBoolEnum.Empty -> Empty
    }

    enum class XBoolEnum  {
        True,
        False,              // False
        XBool,              // True or False, e.g., external unknown input
        Empty;              // Neither True nor False, e.g., Predicate on value that is NaN
    }

    override fun toString(): String =
        when(this.value) {
            True    -> "True"
            False   -> "False"
            Empty   -> "Contradiction"
            All   -> "Unknown"
            else    -> "BDD leaf: None of True, False, NaB, X"
        }

    override fun intersect(other: XBool): XBool =
        xBoolBoolIntersect[Pair(this, other)]!!

    override operator fun contains(other: XBool): Boolean =
        xBoolBoolContains[Pair(this, other)]!!
/*
    override infix fun and(other: XBool): XBool =
        xBoolBoolAnd[Pair(this, other)]!!

    override infix fun or(other: XBool): XBool =
        xBoolBoolOr[Pair(this, other)]!!

    override fun xor(other: XBool): XBool =
        xBoolBoolXor[Pair(this, other)]!!

    override fun nand(other: XBool): XBool = (this and other).not()

    override operator fun not(): XBool = when(this.xBoolEnum) {
            XBoolEnum.False -> True
            XBoolEnum.True -> False
            XBoolEnum.XBool -> XBool
            XBoolEnum.Empty -> Empty
    } */

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

val xBoolBoolContains: HashMap<Pair<XBool, XBool>, Boolean> = hashMapOf(
    Pair(True, True ) to true,
    Pair(True, False) to false,
    Pair(True, All) to false,
    Pair(True, Empty) to false,

    Pair(False, True ) to false,
    Pair(False, False) to true,
    Pair(False, All) to false,
    Pair(False, Empty) to false,

    Pair(All, True ) to true,
    Pair(All, False) to true,
    Pair(All, All) to true,
    Pair(All, Empty) to false,

    Pair(Empty, True ) to false,
    Pair(Empty, False) to false,
    Pair(Empty, All) to false,
    Pair(Empty, Empty) to true,
)

/** the intersect operation on two Xbool checks for the possible equality */
val xBoolBoolIntersect: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to Empty,
    Pair(True, All) to True,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to Empty,
    Pair(False, False) to False,
    Pair(False, All) to False,
    Pair(False, Empty) to Empty,

    Pair(All, True ) to True,
    Pair(All, False) to False,
    Pair(All, All) to All,
    Pair(All, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, All) to Empty,
    Pair(Empty, Empty) to Empty,
)

val xBoolBoolAnd: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to False,
    Pair(True, All) to All,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to False,
    Pair(False, False) to False,
    Pair(False, All) to False,
    Pair(False, Empty) to Empty,

    Pair(All, True ) to All,
    Pair(All, False) to False,
    Pair(All, All) to All,
    Pair(All, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, All) to Empty,
    Pair(Empty, Empty) to Empty,
)

val xBoolBoolOr: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to True,
    Pair(True, All) to True,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to True,
    Pair(False, False) to False,
    Pair(False, All) to False,
    Pair(False, Empty) to Empty,

    Pair(All, True ) to True,
    Pair(All, False) to All,
    Pair(All, All) to All,
    Pair(All, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, All) to Empty,
    Pair(Empty, Empty) to Empty,
)


val xBoolBoolXor: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to False,
    Pair(True, False) to True,
    Pair(True, All) to All,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to True,
    Pair(False, False) to False,
    Pair(False, All) to All,
    Pair(False, Empty) to Empty,

    Pair(All, True ) to All,
    Pair(All, False) to All,
    Pair(All, All) to All,
    Pair(All, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, All) to Empty,
    Pair(Empty, Empty) to Empty,
)
