package examples.cddtests
import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import com.github.tukcps.aadd.values.StateTuple
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CDDTests
{
    /**
     * A Test designed to check that new continuous variables can be added to a StateTuple
     * */
    @Test
    fun addingContinuousVariableTest()
    {
        DDBuilder {
            val testTuple = StateTuple(this)
            val varName = "x"
            val varValue = AffineForm(this, 1.0 .. 2.0)
            testTuple.addContinuousVar(varName,varValue)
            assertEquals(testTuple.getNumSavedContinuousVariables(),1)
        }
    }

    /**
     * A Test designed to check that new discrete variables can be added to a StateTuple
     * */
    @Test
    fun addingDiscreteVariableTest()
    {
        DDBuilder {
            val testTuple = StateTuple(this)
            val varName = "d"
            val varValue = True
            testTuple.addDiscreteVar(varName,varValue)
            assertEquals(testTuple.getNumSavedDiscreteVariables(),1)
        }
    }

    /**
     * A Test designed to check if the clone function returns a new object, and the MutableHashMaps are also
     * fresh
     * */
    @Test
    fun cloneTest()
    {
        DDBuilder {
            val testTuple = StateTuple(this)
            val contVarName = "x"
            val contVarValue = AffineForm(this, 1.0 .. 2.0)

            val discVarName = "d"
            val discVarValue = True

            testTuple.addContinuousVar(contVarName,contVarValue)
            testTuple.addDiscreteVar(discVarName,discVarValue)

            val copyTuple = testTuple.clone()

            val updatedContVarValue = AffineForm(this, 3.0 .. 4.0)
            val updatedDiscVarValue = False

            copyTuple.updateContinuousVariable(contVarName,updatedContVarValue)
            copyTuple.updateDiscreteVariable(discVarName,updatedDiscVarValue)

            assertNotEquals(copyTuple.getDiscreteValue(discVarName),testTuple.getDiscreteValue(discVarName))
            assertNotEquals(copyTuple.getContinuousValue(contVarName),testTuple.getContinuousValue(contVarName))
        }
    }

    /**
     * Test to check the stringification of the state tuple class
     * TODO Make test quantizable, general Fix me up
     * */
    @Test
    fun stateTupleStringifyTest()
    {
         DDBuilder {
            val testTuple = StateTuple(this)
            val contVar1ID = "x1"
            val contVar1Val = AffineForm(this, 1.0 .. 2.0)

            val contVar2ID = "x2"
            val contVar2Val = AffineForm(this, 3.5 .. 4.0)

            val discVar1ID = "d1"
            val discVar1Val = True

            testTuple.addContinuousVar(contVar1ID,contVar1Val)
            testTuple.addContinuousVar(contVar2ID,contVar2Val)
            testTuple.addDiscreteVar(discVar1ID,discVar1Val)

            // println("Out: $testTuple")
        }
    }

    /**
     * A Test designed to check if the constructor of a CDD works as expected
     * */
    @Test
    fun CDDConstructorTest() {
        DDBuilder {
            // Leaf and internal elements
            var d1 = variable("1")
            var d2 = variable("2")
            var d3 = variable("3")

            val af1 = AffineForm(this, 1.0 .. 2.0)
            val af2 = AffineForm(this, 2.0 .. 3.0)
            val af3 = AffineForm(this, 3.0 .. 4.0)
            val af4 = AffineForm(this, 4.0 .. 5.0)
            val af5 = AffineForm(this, 5.0 .. 6.0)
            val af6 = AffineForm(this, 6.0 .. 7.0)

            // build first variable x1
            val x1Name = "x1"
            val lx11 = AADD.Leaf(this,af1)
            val lx12 = AADD.Leaf(this,af2)
            val itx1d31 = AADD.Internal(this,d3.index,lx11,lx12)
            val lx13 = AADD.Leaf(this,af3)
            val itx1d21 = AADD.Internal(this,d2.index,itx1d31,lx13)
            val lx14 = AADD.Leaf(this,af4)
            val x1root = AADD.Internal(this,d1.index,itx1d21,lx14)

            // build second variable x2
            val x2Name = "x2"
            val lx21 = AADD.Leaf(this,af5)
            val lx22 = AADD.Leaf(this,af6)
            val x2root = AADD.Internal(this,d2.index,lx21,lx22)

            // build discrete variable d1
            val d1Name = "d1"
            val ld12 = BDD.Leaf(this,False)
            val ld13 = BDD.Leaf(this,True)
            val itd1d31 = BDD.Internal(this,d3.index,ld12,ld13)
            val ld11 = BDD.Leaf(this,False)
            val d1root = BDD.Internal(this,d2.index,ld11,itd1d31)


            val variables = mutableMapOf<String,DD<*>>()
            variables[x1Name] = x1root
            variables[x2Name] = x2root
            variables[d1Name] = d1root


            val cddRoot = generateCDD(variables,StateTuple(this),this)
            println("CDD:${cddRoot.toIteString()}")
            println("Height:${cddRoot.height()}")
            println(cddRoot.gatherLeafsWithPaths())
            println("Symbolic Representation:${cddRoot.toSymbolicString()}")
        }
    }

    /***/
    @Test
    fun symbolicRepresentationTest()
    {
        DDBuilder {
            val af = AffineForm(this, 1.0 .. 2.0)
            println(af.toSymbolicString())
        }
    }


}