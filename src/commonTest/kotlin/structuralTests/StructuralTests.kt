package structuralTests

import io.github.tukcps.aadd.DDBuilder
import kotlin.test.*


class StructuralTests {
    @Test @Ignore
    fun bddSubTreeTest() {
        val builder = DDBuilder()
        val tru = builder.Bool.True
        val fal = builder.Bool.False

        val test1 = builder.Bool.All

        assertTrue(test1.containsSubDD(tru))
        assertTrue(test1.containsSubDD(fal))

        val test2 = builder.Bool.All.or(test1)

        assertTrue(test2.containsSubDD(test1))

        val infb = builder.Bool.Infeasible

        assertFalse(test1.containsSubDD(infb))
        assertFalse(infb.containsSubDD(tru))
        assertFalse(tru.containsSubDD(infb))
        assertFalse(infb.containsSubDD(fal))
        assertFalse(fal.containsSubDD(infb))
    }
}