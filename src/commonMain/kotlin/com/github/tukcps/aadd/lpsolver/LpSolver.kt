package com.github.tukcps.aadd.lpsolver
import com.github.tukcps.aadd.lpsolver.LpConstraintSign.*
import com.github.tukcps.aadd.lpsolver.LpFunctionOptimization.*
import com.github.tukcps.aadd.lpsolver.Verdict.*
import kotlin.math.abs


/**
 * Solve linear programming problem.
 *
 * This function handles encoding the problem into internal state and decoding a solution back.
 * Actual work is done below.
 */
fun solve(problem: LpProblem): LpSolution {
    var variablesCount = 0
    val variablesMapping: Map<LpVariable, Int> = mutableMapOf<LpVariable, Int>().also { map ->
        problem.variables.forEach {
            map[it] = variablesCount
            variablesCount += if (it.canBeNegative) 2 else 1
        }
    }

    var constraintsCount = 0
    val constraintsMapping: Map<LpConstraint, Int> = mutableMapOf<LpConstraint, Int>().also { map ->
        problem.constraints.forEach {
            map[it] = constraintsCount
            constraintsCount += if (it.sign == EQUAL) 2 else 1
        }
    }

    val initialState = LpSolverState(variablesCount, constraintsCount)
    fun writeTerms(terms: Map<LpVariable, Double>, target: DoubleArray, sign: Int) {
        terms.forEach { (v, k) ->
            val id = variablesMapping[v]!!
            target[id] = sign * k
            if (v.canBeNegative) {
                target[id + 1] = -sign * k
            }
        }
    }
    problem.constraints.forEach {
        fun writeConstraint(c: LpConstraint, row: Int, sign: Int) {
            writeTerms(c.expression.terms, initialState.a[row], sign)
            initialState.b[row] = sign * (c.constantValue - c.expression.free)
        }

        val id = constraintsMapping[it]!!
        when (it.sign) {
            EQUAL -> {
                writeConstraint(it, id, +1)
                writeConstraint(it, id + 1, -1)
            }
            LESS_OR_EQUAL ->
                writeConstraint(it, id, +1)
            GREATER_OR_EQUAL ->
                writeConstraint(it, id, -1)
        }
    }
    writeTerms(problem.function.expression.terms, initialState.c, if (problem.function.optimization == MAXIMIZE) +1 else -1)

    val (finalState, verdict) = solve(initialState)

    return when (verdict) {
        UNBOUNDED -> Unbounded
        NO_SOLUTION -> NoSolution
        SOLVED -> {
            val variables = problem.variables.associate { v ->
                val id = variablesMapping[v]!!
                v to finalState.x[id] - if (v.canBeNegative) finalState.x[id + 1] else .0
            }
            var target = problem.function.expression.free
            val sign = if (problem.function.optimization == MAXIMIZE) +1 else -1
            problem.function.expression.terms.forEach { (v, k) -> target += sign * k * variables[v]!! }

            Solved(target, variables)
        }
    }
}

/**
 * Actually solve linear programming problem.
 * @param initial initial state, containing the encoded problem. Left intact.
 * @return pair of final state and solver verdict.
 */
private fun solve(initial: LpSolverState): Pair<LpSolverState, Verdict> {
    val solving = LpSolverState(initial.n, initial.m)
    initial.copyTo(solving)

    if (!solving.bootstrap()) return solving to NO_SOLUTION

    // FIXME for now, these are just copied, however, they don't *feel* right
    for (i in 0 until solving.n) {
        solving.c[i] = .0
    }
    for (j in 0 until solving.s) {
        val pj = initial.posN[j]
        if (pj == -1) continue
        if (solving.posN[j] != -1) {
            solving.c[solving.posN[j]] += initial.c[pj]
            continue
        }
        solving.v += initial.c[pj] * solving.b[solving.posB[j]]
        for (k in 0 until initial.s) {
            val pk = solving.posN[k]
            if (pk == -1) continue
            solving.c[pk] += initial.c[pj] * -solving.a[solving.posB[j]][pk]
        }
    }
    // FIXME over

    val succeeded = solving.doSimplex()
    return solving to if (succeeded) SOLVED else UNBOUNDED
}

private enum class Verdict {
    SOLVED,
    UNBOUNDED,
    NO_SOLUTION
}

private const val EPS = 1e-9

/**
 * This class represents internal solver state. State represents some linear programming problem at some point during
 * simplex method, as described in "Introduction to Algorithms, CLRS". Here, small explanation of storage format is
 * given, however having the book is strongly recommended.
 *
 * First of all, problem is expected to be
 * . maximization one,
 * . without constant addition in function,
 * . all constraints are inequalities of less-or-equals-to-constant form, and
 * . all variables are non-negative.
 * High-level API lifts these restrictions by rewriting the problem before the solution process and rewriting once more
 * later to give the result.
 *
 * Every constraint receives it's own variable that represents the gap between the constraint limit and actual
 * expression value. One more variable is given for optimized function. After that, we actually have a system of
 * equalities.
 *
 * At any point, all variables (except one for the function) are split into basis ones and non-basis ones.
 * The system is rewritten in a way that you have freestanding basic variables on the right sides and non-basis
 * variables expressions on the left side:
 *
 *        v =    + 3 x1 + 1 x2 + 2 x3
 *      x_4 = 30 - 1 x1 - 1 x2 - 3 x3
 *      x_5 = 24 - 2 x1 - 2 x2 - 5 x3
 *      x_6 = 36 - 4 x1 - 1 x2 - 2 x3
 * (example from "Introduction to Algorithms")
 *
 * Simplex method consists of pivot operations, that exchange one basis variable with non-basis one and rewriting the
 * system accordingly.
 *
 * In this implementation, we will not store m*n matrix for equations coefficients. Instead, we will use bigger
 * (n+m)*(n+m) matrix, with one row and column per each variable. We will use two additional posN and posB arrays, that
 * for each variable store where it's column (fon non-basis) or row (for basic) is, or -1 if there is no.
 * Of course, out of pair (`posN[i]`, `posB[i]`) only one is -1. That gives easier and faster pivot operations.
 * Further, we store coefficients of equations as in original inequalities (negated compared to example above).
 * Example above could be stored like this:
 *
 *     posN: x1 -> 0, x2 -> 1, x3 -> 2; others -1
 *     posB: x4 -> 0, x5 -> 1, x6 -> 2; others -1
 *     a:                        b:
 *     +1  +1  +3  --  --  -- | 30
 *     +2  +2  +5  --  --  -- | 24
 *     +4  +1  +2  --  --  -- | 36
 *     --  --  --  --  --  -- | --
 *     --  --  --  --  --  -- | --
 *     --  --  --  --  --  -- | --
 *     c:                       v:
 *     +3  +1  +2  --  --  -- |  0
 *
 * TODO: consider merging `b`, `c` and `v` into `a` matrix.
 * TODO: rewrite using clearer Kotlin primitives.
 *
 * It can be private, it's not to provide debug helpers.
 */
internal class LpSolverState internal constructor(val n: Int, val m: Int) {
    val s: Int
        get() = n + m

    val a = Array(s) { DoubleArray(s) }
    val b = DoubleArray(s)
    val c = DoubleArray(s)
    var v: Double = 0.0
    var x = DoubleArray(s)
    val posN = IntArray(s) { i -> if (i < n) i else -1 }
    val posB = IntArray(s) { i -> if (i < n) -1 else i - n }

    /**
     * Copies data to another state instance.
     */
    internal fun copyTo(target: LpSolverState) {
        check(target.s >= s)

        for (i in 0 until s) {
            for (j in 0 until s) {
                target.a[i][j] = a[i][j]
            }
            target.b[i] = b[i]
            target.c[i] = c[i]
            target.posN[i] = posN[i]
            target.posB[i] = posB[i]
        }
        target.v = v
    }

    /**
     * Pivot operation.
     * @param l basis variable
     * @param e non-basis one
     */
    private fun pivot(l: Int, e: Int) {
        //println("double pivot")
        val pl = posB[l]
        val pe = posN[e]

        check(pl != -1)
        check(pe != -1)

        val t = a[pl][pe]
        //println("1:$t")
        //println("2:${b[pl]}")
        b[pl] /= t
        //println("3:${b[pl]}")
        //println("dtr:")
        for (j in 0 until s) {
            val pj = posN[j]
            if (pj == -1 || j == e) continue
            a[pl][pj] /= t
            //print("${a[pl][pj]},")
        }
        //println()
        a[pl][pe] = 1 / t
        //println("4:${a[pl][pe]}")
        for (i in 0 until s) {
            val pi = posB[i]
            if (pi == -1 || i == l) continue
            val t1 = a[pi][pe]
            b[pi] -= t1 * b[pl]
            //print("${b[pi]},")
            for (j in 0 until s) {
                val pj = posN[j]
                if (pj == -1 || j == e) continue
                a[pi][pj] -= t1 * a[pl][pj]
                //print("${a[pi][pj]},")
            }
            a[pi][pe] = -t1 * a[pl][pe]
            //print("${a[pi][pe]},")
        }
        //println()
        for (j in 0 until s) {
            val pj = posN[j]
            if (pj == -1 || j == e) continue
            c[pj] -= c[pe] * a[pl][pj]
            //print("${c[pj]}, ")
        }
        //println()
        v += c[pe] * b[pl]
        //println("v:$v")
        c[pe] = -c[pe] * a[pl][pe]
        //println("cpiv:$${c[pe]}")
        posN[e] = -1
        posN[l] = pe
        posB[l] = -1
        posB[e] = pl
        //println("double pivot")
    }

    /**
     * Simplex loop.
     * @return true if solution was found
     */
    fun doSimplex(): Boolean {
        var its = 0
        while (true) {
            its+=1
            //println("double iteration:$its")
            /*
            for(line in a) {
                for(dv in line)
                {
                    print("${dv} ")
                }
                println()
            }*/
            //println()

            //for(xz in b)print("$xz,")
            //println()
            var e = -1
            var pe = -1
            for (i in 0 until s) {
                val pi = posN[i]
                if (pi == -1) continue
                if (c[pi] > EPS) {
                    e = i
                    pe = pi
                    break
                }
            }
            if (e == -1) break
            var l = -1
            var pl = -1
            for (i in 0 until s) {
                val pi = posB[i]
                if (pi == -1 || a[pi][pe] < EPS) continue
                if (l == -1 || b[pl] / a[pl][pe] > b[pi] / a[pi][pe]) {
                    l = i
                    pl = pi
                }
            }
            check(l == -1 || abs(a[pl][pe]) > EPS)
            if (l == -1) return false
            pivot(l, e)
        }
        for (i in 0 until s)
            x[i] = if (posB[i] != -1) b[posB[i]] else .0
        return true
    }

    /**
     * Bootstrap the state: check if zeroes are acceptable for initial solution,
     * else create another state with additional variable, solve it,
     * and use it's solution to provide initial solution.
     *
     * State used internally always has zeroes as acceptable solution and always
     * has finite solution, no infinite recursion.
     */
    fun bootstrap(): Boolean {
        var k = -1
        var pk = -1

        for (i in 0 until s) {
            // FIXME use the fact that the structure is known? until
            if (posB[i] != -1 && (k == -1 || b[pk] > b[posB[i]])) {
                k = i
                pk = posB[i]
            }
        }
        if (b[pk] >= -EPS) {
            return true
        }

        val startState = LpSolverState(n + 1, m).also {
            copyTo(it)
            for (i in 0 until it.s) {
                it.posB[i] = -1
                it.posN[i] = -1
            }
            it.posN[0] = n
            for (i in 0 until n) {
                it.posN[i + 1] = i
            }
            for (i in 0 until it.m) {
                it.posB[n + i + 1] = i
                it.a[i][n] = -1.0
            }
            for (i in 0 until it.s) {
                it.c[i] = .0
            }
            it.c[n] = -1.0
        }

        startState.apply {
            pivot(k + 1, 0)
            check(doSimplex())
            if (abs(x[0]) > EPS) return false
            if (posB[0] != -1) {
                val p0 = posB[0]
                var i = -1
                for (j in 0 until s) {
                    val pj = posN[j]
                    if (pj != -1 && abs(a[p0][pj]) > EPS) {
                        i = j
                        break
                    }
                }
                check(i != -1)
                pivot(0, i)
            }
        }

        for (i in 0 until s) {
            posN[i] = -1
            posB[i] = startState.posB[i + 1]
            b[i] = startState.b[i]
        }
        var freeC = 0
        for (i in 0 until s) {
            if (startState.posN[i + 1] == -1) continue
            val pi = freeC++
            posN[i] = pi
            for(j in 0 until s)
                a[j][pi] = startState.a[j][startState.posN[i + 1]]
            c[pi] = startState.c[startState.posN[i + 1]]
        }
        return true
    }
    /*
        private object LpSolverStatePrinter {
            internal fun print(state: LpSolverState): String = state.run { buildString {
                append("state: n = $n m = $m\n")
                append("| B:")
                (0 until s).filter { posB[it] != -1 }.forEach { append(" $it->${posB[it]}") }
                append("\n")
                append("| N:")
                (0 until s).filter { posN[it] != -1 }.forEach { append(" $it->${posN[it]}") }
                append("\n")
                append("| ", "i/j".td, " | ")
                (0 until s).filter { posN[it] != -1 }.forEach { append((it to posN[it]).td, " ") }
                append("| ", "b".td, "\n")
                (0 until s).filter { posB[it] != -1 }.forEach { i ->
                    val pi = posB[i]
                    append("| ", (i to pi).td, " | ")
                    (0 until s).filter { posN[it] != -1 }.forEach { j -> append(a[pi][posN[j]].td, " ") }
                    append("| ", b[pi].td, "\n")
                }
                append("| ", "c".td, " | ")
                (0 until s).filter { posN[it] != -1 }.forEach { append(c[posN[it]].td, " ") }
                append("| ", v.td)
            } }

            //private val Double.td: String get() =
            //    LpSolverStatePrinterFormatter.format("%9.4f", this)
            //private val String.td: String get() =
            //    LpSolverStatePrinterFormatter.format("%9s", this)
            //private val Pair<Int, Int>.td: String get() =
            //    LpSolverStatePrinterFormatter.format("%3d (%3d)", this.first, this.second)
        }

        override fun toString(): String = LpSolverStatePrinter.print(this)*/
}
/*
internal expect object LpSolverStatePrinterFormatter {
    fun format(format: String, vararg args: Any): String
}*/