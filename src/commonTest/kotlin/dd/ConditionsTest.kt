package dd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import kotlin.test.*

class ConditionsTest {

    // Comparison operations create internal nodes of increasing index.
    @Test fun createCondTest() {
        DDBuilder {
            val a = real(1.0..3.0, "a")
            val b = real(2.0..4.0, "Symbol of b")
            val c = real(3.0..5.0, "Symbol o c")
            val top = conditions.topIndex
            val cond = a greaterThanOrEquals b
            assertEquals(top + 1, conditions.topIndex)
            assertEquals(1, cond.height())
            val cond2 = b lessThanOrEquals c
            assertEquals(top + 2, conditions.topIndex)
            assertEquals(1, cond2.height())
            conditions.newVariable("unknown Boolean", this)
            assertEquals(top + 3, conditions.topIndex)
        }
    }

    /**
     * There are two kind of conditions: boolean ones (True or False) or
     * Affine Forms that introduce constraints (af > 0)
     */
    @Test
    fun isBooleanConditionTest() {
        DDBuilder {
            val notACondition = Bool.True
            val a = real(1.0..3.0, "a")
            val b = real(2.0..4.0, "Symbol of b")
            val aCondition = a greaterThanOrEquals b // condition refers to (internal) variable not in the condition table.
            assertFalse(notACondition.isBoolCond())
            assertFalse(aCondition.isBoolCond())
        }
    }

    /**
     * There are two kind of conditions: boolean ones (True or False) or
     * Affine Forms that introduce constraints (af > 0)
     */
    @Test
    fun isBooleanConditionTest2() {
        DDBuilder{
            val a = boolean("a")
            assertTrue(a.isBoolCond())
        }
    }

    @Test
    fun testITEGT() {
        DDBuilder {
            val x = real(1.0 .. 8.0)
            val ge = x greaterThan  real(4.0)
            val evalDown = ge.ite(x, Reals.Empty)

            assertEquals(4.0, evalDown.getRange().min.toDouble(), 0.000001)
            assertEquals(8.0, evalDown.getRange().max.toDouble(), 0.000001)
        }
    }

    @Ignore // TODO: fix this test, it is not working as expected
    @Test
    fun testITEWithDiv() {
        DDBuilder {
            val x = real(1.0 .. 8.0)
            val y = real(8.0)
            val leftValue = y / x
            val ge = leftValue greaterThan  real(4.0)
            val evalDown = ge.ite(leftValue, Reals.Empty)

            assertEquals(4.0, evalDown.getRange().min.toDouble(), 0.000001)
            assertEquals(8.0, evalDown.getRange().max.toDouble(), 0.000001)
        }
    }
}