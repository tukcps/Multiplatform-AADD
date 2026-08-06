import io.github.tukcps.aadd.values.real.aa.AffineForm.Companion.math
import io.github.tukcps.aadd.values.real.rounding.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals

class IssuesTests {

    @Test
    fun issueZeroRoundedWrong() {
        val nr = math.add(0.0, 0.0, Rounding.UP)
        assertEquals(0.0, nr)
    }
}