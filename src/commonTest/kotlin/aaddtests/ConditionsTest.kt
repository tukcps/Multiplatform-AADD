package aaddtests

import com.github.tukcps.aadd.DDBuilder
import kotlin.test.*


class ConditionsTest {

    // Comparison operations create internal nodes of increasing index.
    @Test fun createCondTest() {
        DDBuilder {
            val a = real(1.0..3.0, "a")
            val b = real(2.0..4.0, "Symbol of b")
            val c = real(3.0..5.0, "Symbol o c")
            val top = conds.topIndex
            val cond = a greaterThanOrEquals b
            assertEquals(top + 1, conds.topIndex)
            assertEquals(1, cond.height())
            val cond2 = b lessThanOrEquals c
            assertEquals(top + 2, conds.topIndex)
            assertEquals(1, cond2.height())
            conds.newVariable("unknown Boolean", this)
            assertEquals(top + 3, conds.topIndex)
        }
    }

    /**
     * There are two kind of conditions: boolean ones (True or False) or
     * Affine Forms that introduce constraints (af > 0)
     */
    @Test
    fun isBooleanConditionTest() {
        DDBuilder {
            val notACondition = True
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
}