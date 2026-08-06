package io.github.tukcps.aadd.values.bool


import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.DD
import io.github.tukcps.aadd.values.XBoolValue
import io.github.tukcps.aadd.values.bool.XBool.Companion.Empty
import io.github.tukcps.aadd.values.bool.XBool.Companion.False
import io.github.tukcps.aadd.values.bool.XBool.Companion.True
import io.github.tukcps.aadd.values.bool.XBool.Companion.XBool

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
        val True: XBool = XBoolImpl(XBoolImpl.XBoolEnum.True)
        val False: XBool = XBoolImpl(XBoolImpl.XBoolEnum.False)
        val XBool: XBool = XBoolImpl(XBoolImpl.XBoolEnum.XBool)
        val Empty: XBool = XBoolImpl(XBoolImpl.XBoolEnum.Empty)

        fun valueOf(s: String): XBool = when(s) {
            "True" -> True
            "False" -> False
            "X" -> XBool
            "NaB" -> Empty
            else -> TODO()
        }
    }

    override fun equals(other: Any?): Boolean
    fun intersect(other: XBool): XBool
    operator fun contains(other: XBool): Boolean

    /*
    operator fun not(): XBool
    infix fun and(other: XBool): XBool
    infix fun or(other: XBool): XBool
    infix fun xor(other: XBool): XBool
    infix fun nand(other: XBool): XBool */

    fun valueOf(dd: DD<*>): XBool =
        when(dd) {
            dd.builder.Bool.True -> True
            dd.builder.Bool.False -> False
            dd.builder.Bool.All -> XBool
            dd.builder.Bool.Empty -> Empty
            else -> TODO(" Experimental NOT in use ")
        }

    fun bddLeafOf(builder: DDBuilder): BDD.Leaf =
        when(this) {
            True -> builder.Bool.True
            False -> builder.Bool.False
            XBool -> builder.Bool.All
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
        XBoolEnum.XBool -> XBool
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
            XBool   -> "Unknown"
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
    Pair(True, XBool) to false,
    Pair(True, Empty) to false,

    Pair(False, True ) to false,
    Pair(False, False) to true,
    Pair(False, XBool) to false,
    Pair(False, Empty) to false,

    Pair(XBool, True ) to true,
    Pair(XBool, False) to true,
    Pair(XBool, XBool) to true,
    Pair(XBool, Empty) to false,

    Pair(Empty, True ) to false,
    Pair(Empty, False) to false,
    Pair(Empty, XBool) to false,
    Pair(Empty, Empty) to true,
)

/** the intersect operation on two Xbool checks for the possible equality */
val xBoolBoolIntersect: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to Empty,
    Pair(True, XBool) to True,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to Empty,
    Pair(False, False) to False,
    Pair(False, XBool) to False,
    Pair(False, Empty) to Empty,

    Pair(XBool, True ) to True,
    Pair(XBool, False) to False,
    Pair(XBool, XBool) to XBool,
    Pair(XBool, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, XBool) to Empty,
    Pair(Empty, Empty) to Empty,
)

val xBoolBoolAnd: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to False,
    Pair(True, XBool) to XBool,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to False,
    Pair(False, False) to False,
    Pair(False, XBool) to False,
    Pair(False, Empty) to Empty,

    Pair(XBool, True ) to XBool,
    Pair(XBool, False) to False,
    Pair(XBool, XBool) to XBool,
    Pair(XBool, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, XBool) to Empty,
    Pair(Empty, Empty) to Empty,
)

val xBoolBoolOr: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to True,
    Pair(True, False) to True,
    Pair(True, XBool) to True,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to True,
    Pair(False, False) to False,
    Pair(False, XBool) to False,
    Pair(False, Empty) to Empty,

    Pair(XBool, True ) to True,
    Pair(XBool, False) to XBool,
    Pair(XBool, XBool) to XBool,
    Pair(XBool, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, XBool) to Empty,
    Pair(Empty, Empty) to Empty,
)


val xBoolBoolXor: HashMap<Pair<XBool, XBool>, XBool> = hashMapOf(
    Pair(True, True ) to False,
    Pair(True, False) to True,
    Pair(True, XBool) to XBool,
    Pair(True, Empty) to Empty,

    Pair(False, True ) to True,
    Pair(False, False) to False,
    Pair(False, XBool) to XBool,
    Pair(False, Empty) to Empty,

    Pair(XBool, True ) to XBool,
    Pair(XBool, False) to XBool,
    Pair(XBool, XBool) to XBool,
    Pair(XBool, Empty) to Empty,

    Pair(Empty, True ) to Empty,
    Pair(Empty, False) to Empty,
    Pair(Empty, XBool) to Empty,
    Pair(Empty, Empty) to Empty,
)
