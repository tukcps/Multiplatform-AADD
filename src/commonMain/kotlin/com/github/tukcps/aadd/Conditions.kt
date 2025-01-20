package com.github.tukcps.aadd

import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.IntegerRange


/**
 * The conditions that refer to each index of a BDD or AADD.
 * The class assigns each index a condition (x), and the track the top and bottom indexes.
 * States of Boolean decision variables including X for unknown and AF for a constraint.
 * The set of all conditions is saved in the HashMap conditions.
 */
class Conditions(
    val builder: DDBuilder,
    var topIndex: Int = 0,
    var btmIndex: Int = 0,
    var indexes: HashMap<String, Int> = HashMap(),
    var x: HashMap<Int, DD<*> > = HashMap(),
    var introducedDecVars: HashMap<String, String> = HashMap(),
    var decVarsIntroducedBy: HashMap<String, HashSet<String>> = HashMap(),
    var isDecVar: HashMap<String, Boolean> = HashMap()
) {

    val conditions: HashMap<Int, DD<*>> get() = x
    /**
     * Adds a constraint in form of an affine form.
     * @return index of the new condition.
     */
    fun newConstraint(c: AffineForm, id: String = ""): Int {
        ++topIndex
        x[topIndex] = AADD.Leaf(builder, c)
        indexes[if (id == "") "var$topIndex" else id] = topIndex
        return topIndex
    }

    /**
     * Adds a constraint in form of an affine form.
     * @param c an integer range as constraint
     * @param id a sting for identification
     * @return index of the new condition.
     */
    fun newConstraint(c: IntegerRange, id: String = ""): Int {
        ++topIndex
        x[topIndex] = IDD.Leaf(builder, c)
        indexes[if (id == "") "var$topIndex" else id] = topIndex
        return topIndex
    }

    /**
     * Adds a Boolean decision variable with a documentation string.
     * @return index of the new condition.
     */
    fun newVariable(name: String = "", builder: DDBuilder, fromExpr: String = "noSourceExpression", isDecVar: Boolean = false): Int {
        if (indexes[name] != null)
            return indexes[name]!!
        if (x[indexes[name]] is AADD)
            throw DDInternalError("Index belongs to constraint, not variable")
        ++topIndex
        indexes[if (name == "") "var$topIndex" else name] = topIndex
        val tmpName = if (name == "") "var$topIndex" else name
        x[topIndex] = builder.Bool
        introducedDecVars[tmpName] = fromExpr
        if (decVarsIntroducedBy.contains(fromExpr))
            decVarsIntroducedBy[fromExpr]!!.add(tmpName)
        else {
            val tmpSet = HashSet<String>()
            tmpSet.add(tmpName)
            decVarsIntroducedBy[fromExpr] = tmpSet
        }
        this.isDecVar[tmpName] = isDecVar
        return topIndex
    }

    fun constrainVariable(name: String, dd: DD.Leaf<*>) {
        val index = indexes[name]
        if (index == null) throw DDInternalError("Variable not found: $name")
        else {
            x[index] = dd
        }
    }


    /**
     * The method gets the condition x_i from the set of conditions X
     * @param i index of X
     * @return x_i if x_i is an affine form, otherwise null.
     */
    fun getCondition(i: Int): DD<*>? {
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)
        return x[i]
    }

    fun getCondition(key: String): DD<*>? {
        val index = indexes[key]
        return if (index == null) null
        else getCondition(index)
    }

    fun getConstraint(i: Int): AADD.Leaf? {
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)
        return if (x[i] is AADD.Leaf) x[i] as AADD.Leaf
        else null
    }

    fun getConstraint(key: String): AADD.Leaf? {
        val index = indexes[key]
        return when {
            index == null -> null
            x[index] is AADD.Leaf -> x[index] as AADD.Leaf
            else -> null
        }
    }

    /**
     * The method gets a boolean condition x_i from X
     * @param i, an index
     * @return x_i if x_i is a boolean variable, otherwise null.
     */
    fun getVariable(i: Int): BDD.Leaf? {
        /**TODO require anstatt assert*/
        //require()
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)     { "index reserved for leaves: $i"}
        assert(x[i] != null)
        return if (x[i] is AADD.Leaf) null
        else x[i] as BDD.Leaf
    }

    private fun assert(b: Boolean) {
        if(b.not()) throw AADDAssertError("assertion error")
    }

    private fun assert(b: Boolean, function: () -> String) {
        if(b.not()) throw AADDAssertError(function())
    }

    fun setVariable(i: Int, v: DD.Leaf<*>) {
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)        { "index reserved for leaves: $i"}
        assert(x[i] != null)
        assert(x[i] is BDD.Leaf)
        x[i] = v
    }

    fun <K, V> HashMap<K, V>.getKey(value: V) =
        entries.firstOrNull { it.value == value }?.key

    /** Creates a string that documents the set of all conditions and constraints */
    override fun toString(): String {
        var s = "Conditions: ("
        for (i in x.keys) {
            s += " Index $i -> " + x[i]
            s += " name " + indexes.filterValues { it == i } + ", "
        }
        return ("$s)")
    }

    fun clone(): Conditions {
        val xClone: HashMap<Int, DD<*> > = hashMapOf()
        x.forEach { xClone[it.key] = it.value.clone() }

        val indexesClone: HashMap<String, Int> = hashMapOf()
        indexes.forEach { indexesClone[it.key] = it.value }

        val introducedDecVarsClone: HashMap<String, String> = hashMapOf()
        introducedDecVars.forEach { introducedDecVarsClone[it.key] = it.value }

        val decVarsIntroducedByClone: HashMap<String, HashSet<String>> = hashMapOf()
        // AADD:
        // decVarsIntroducedBy.forEach { decVarsIntroducedByClone[it.key] = it.value.clone() as HashSet<String> }
        decVarsIntroducedBy.forEach {
            val klon = hashSetOf<String>()
            klon.addAll(it.value)
            decVarsIntroducedByClone[it.key] = klon
        }

        val isDecVarClone: HashMap<String, Boolean> = hashMapOf()
        isDecVar.forEach { isDecVarClone[it.key] = it.value }

        return Conditions(builder, topIndex, btmIndex, indexesClone, xClone, introducedDecVarsClone, decVarsIntroducedByClone, isDecVarClone)
    }
}
