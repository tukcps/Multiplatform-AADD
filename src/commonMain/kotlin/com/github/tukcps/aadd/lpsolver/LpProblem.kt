package com.github.tukcps.aadd.lpsolver

/**
 * Interface indicating linear expression or it's parts. Consists of linear [terms] in form of mapping from variable
 * to its coefficient and an additional [free] constant.
 */
interface LpExpressionLike {
    /**
     * Terms of linear expression: mapping from variables to their coefficients.
     */
    val terms: Map<LpVariable, Double>
    /**
     * Freestanding term.
     */
    val free: Double
}

/**
 * Variable in a linear programming problem.
 * Has a unique [name].
 * By default, variables are limited not to be negative.
 * If needed, one can set [canBeNegative] to allow negative values by adding another internal variable responsible
 * for negative values.
 *
 * Implements [LpExpressionLike] interface so it can be used as a standalone linear expression.
 *
 * @param name Variable name.
 * @param canBeNegative Can this variable assume negative values, defaults to false.
 */
data class LpVariable(val name: String, val canBeNegative: Boolean = false): LpExpressionLike {
    override val terms: Map<LpVariable, Double>
        get() = mapOf(this to 1.0)
    override val free: Double
        get() = .0
}

/**
 * Class storing linear expressions to be used in constraints or as a function
 * in linear programming problem.
 */
class LpExpression(
    override val terms: Map<LpVariable, Double>,
    override val free: Double = 0.0
): LpExpressionLike

/**
 * Sign used in constraints.
 */
enum class LpConstraintSign {
    /**
     * Equality constraint.
     */
    EQUAL,
    /**
     * Less or equal constraint (linear expression is less or equal than constant).
     */
    LESS_OR_EQUAL,
    /**
     * Greater or equal constraint (linear expression is greater or equal than constant).
     */
    GREATER_OR_EQUAL
}

/**
 * Constraint in a linear programming problem in form "linearExpression sign constantValue".
 * @param expression expression
 * @param sign sign that tights expression and constant
 * @param constantValue constant
 */
class LpConstraint(val expression: LpExpressionLike, val sign: LpConstraintSign, val constantValue: Double)

/**
 * Required optimization of function in a linear programming problem.
 */
enum class LpFunctionOptimization {
    /**
     * Maximize optimized function.
     */
    MAXIMIZE,
    /**
     * Minimize optimized function.
     */
    MINIMIZE
}

/**
 * Function in a linear programming problem to be optimized.
 * @param expression Expression to be optimized.
 * @param optimization Optimization to perform.
 */
data class LpFunction(val expression: LpExpressionLike, val optimization: LpFunctionOptimization)

/**
 * Linear programming problem with given variables, constraints and a function to be maximized.
 *
 * All variables mentioned in constraints and function must be present in variables' list.
 * @param variables Variables of the problem.
 * @param constraints Constraints of the problem.
 * @param function Function to optimize of the problem.
 * */
data class LpProblem(
    val variables: List<LpVariable>,
    val constraints: List<LpConstraint>,
    val function: LpFunction
) {
    init {
        check(constraints.all { c -> c.expression.terms.keys.all { it in variables } }) {
            "Variables used in constraints must be present in variables list"
        }
        check(function.expression.terms.keys.all { it in variables }) {
            "Variables used in function must be present in variables list"
        }
    }

    @Suppress("unused")
    fun printProblem()
    {
        println("Problem:")
        println("--------")
        println("Variables:")
        for(variable in variables) println("ID: ${variable.name} canBeNegative: ${variable.canBeNegative}")
        println("Constraints:")
        for(constraint in constraints)
        {
            var constraintString = ""
            for(a in constraint.expression.terms) constraintString += "${a.value} ${a.key.name}+"
            constraintString += "${constraint.expression.free}"
            constraintString += when(constraint.sign) {
                LpConstraintSign.GREATER_OR_EQUAL -> ">="
                LpConstraintSign.LESS_OR_EQUAL -> "<="
                LpConstraintSign.EQUAL -> "="
            }
            constraintString+="${constraint.constantValue}"
            println(constraintString)
        }
    }
}