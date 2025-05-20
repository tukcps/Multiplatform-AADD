package solvertests
import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DDBuilder
import kotlin.test.Test
import com.github.tukcps.aadd.lpsolver.*
import kotlin.math.ulp
import kotlin.test.assertEquals


class LPSolverTests {
    @Test
    fun noiseVariableUpperTest(){
        val noiseVar = LpVariable("x0",canBeNegative = true)
        val upperConstraint = LpConstraint(LpExpression(mapOf(noiseVar to 1.0)),LpConstraintSign.LESS_OR_EQUAL,1.0)
        val optF = LpFunction(LpExpression(mapOf(noiseVar to 1.0)), LpFunctionOptimization.MAXIMIZE)
        val problem = LpProblem(listOf(noiseVar), listOf(upperConstraint),optF)
        val solution = solve(problem)
        val x0Value = (solution as Solved).variablesValues[noiseVar]
        assertEquals(x0Value,1.0)
    }
    @Test
    fun noiseVariableLowerTest(){
        val noiseVar = LpVariable("x0",canBeNegative = true)
        val upperConstraint = LpConstraint(LpExpression(mapOf(noiseVar to 1.0)),LpConstraintSign.GREATER_OR_EQUAL,-1.0)
        val optF = LpFunction(LpExpression(mapOf(noiseVar to 1.0)), LpFunctionOptimization.MINIMIZE)
        val problem = LpProblem(listOf(noiseVar), listOf(upperConstraint),optF)
        val solution = solve(problem)
        val x0Value = (solution as Solved).variablesValues[noiseVar]
        assertEquals(x0Value,-1.0)
    }

    @Test
    fun smallBoxLpProblemTest(){
        val x0 = LpVariable("x0",canBeNegative = true)
        val x1 = LpVariable("x1",canBeNegative = true)
        val c0 = LpConstraint(LpExpression(mapOf(x0 to 1.0)),LpConstraintSign.LESS_OR_EQUAL,1.0)
        val c1 = LpConstraint(LpExpression(mapOf(x0 to 1.0)),LpConstraintSign.GREATER_OR_EQUAL,-1.0)
        val c2 = LpConstraint(LpExpression(mapOf(x1 to 1.0)),LpConstraintSign.LESS_OR_EQUAL,1.0)
        val c3 = LpConstraint(LpExpression(mapOf(x1 to 1.0)),LpConstraintSign.GREATER_OR_EQUAL,-1.0)
        val optFMax = LpFunction(LpExpression(mapOf(x0 to 1.0,x1 to 1.0)),LpFunctionOptimization.MAXIMIZE)
        val optFMin = LpFunction(LpExpression(mapOf(x0 to 1.0,x1 to 1.0)),LpFunctionOptimization.MINIMIZE)
        val problemMax = LpProblem(listOf(x0,x1),listOf(c0,c1,c2,c3),optFMax)
        val problemMin = LpProblem(listOf(x0,x1), listOf(c0,c1,c2,c3),optFMin)

        val maxSol = solve(problemMax)
        val minSol = solve(problemMin)
        assertEquals(2.0, (maxSol as Solved).functionValue, 2.0.ulp)
        assertEquals( 2.0, (minSol as Solved).functionValue, 2.0.ulp)
    }

    @Test
    fun posSmallBoxLpProblemTest(){
        var x0 = LpVariable("x0",canBeNegative = false)
        var x1 = LpVariable("x1",canBeNegative = false)

        val c0 = LpConstraint(LpExpression(mapOf(x0 to 1.0)),LpConstraintSign.LESS_OR_EQUAL,2.0)
        val c1 = LpConstraint(LpExpression(mapOf(x0 to 1.0)),LpConstraintSign.GREATER_OR_EQUAL,1.0)
        val c2 = LpConstraint(LpExpression(mapOf(x1 to 1.0)),LpConstraintSign.LESS_OR_EQUAL,2.0)
        val c3 = LpConstraint(LpExpression(mapOf(x1 to 1.0)),LpConstraintSign.GREATER_OR_EQUAL,1.0)

        val optFMax = LpFunction(LpExpression(mapOf(x0 to 1.0,x1 to 1.0)),LpFunctionOptimization.MAXIMIZE)
        val optFMin = LpFunction(LpExpression(mapOf(x0 to 1.0,x1 to 1.0)),LpFunctionOptimization.MINIMIZE)
        val problemMax = LpProblem(listOf(x0,x1),listOf(c0,c1,c2,c3),optFMax)
        val problemMin = LpProblem(listOf(x0,x1), listOf(c0,c1,c2,c3),optFMin)

        val maxSol = solve(problemMax)
        val minSol = solve(problemMin)
        assertEquals(4.0, (maxSol as Solved).functionValue)
        assertEquals(-2.0, (minSol as Solved).functionValue)
    }


    @Test
    fun simpleTest(){
        val builder = DDBuilder()
        with(builder){
            var r: AADD = real(0.0 .. 1.0)
            IF(r.greaterThanOrEquals(0.5))
                r = assign(r,r+1.0)
            END()
            r.getRange()
            // println(r.toIteString())
        }
    }
}