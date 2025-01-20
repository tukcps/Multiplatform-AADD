package com.github.tukcps.aadd.lpsolver

/**
 * Interface representing solution of [LpProblem].
 */
sealed class LpSolution

/**
 * Returned by solver when linear programming problem has no solution.
 */
object NoSolution : LpSolution()

/**
 * Returned by solver when linear programming problem is unbounded.
 */
object Unbounded : LpSolution()

/**
 * Returned by solver when linear programming problem is solvable.
 * @param functionValue value or function
 * @param variablesValues values of variables that provide given function value
 */
data class Solved(
    val functionValue: Double,
    val variablesValues: Map<LpVariable, Double>
) : LpSolution()

/** Lp Solution Exception */
open class LpSolutionException(s:String):Exception(s)

/** No Solution Exception */
class NoSolutionException : LpSolutionException("Lp problem has no solution")

/** Unbounded Exception */
class UnboundedException : LpSolutionException("Lp problem is unbounded")