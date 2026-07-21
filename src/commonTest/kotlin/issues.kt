import io.github.tukcps.aadd.values.real.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.math.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals

class issuesTests {

    @Test
    fun issueZeroRoundedWrong() {
        val nr = math.add(0.0, 0.0, Rounding.UP)
        assertEquals(0.0, nr)
    }
}