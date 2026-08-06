package values.real

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.DoubleBound
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import kotlin.test.Test
import kotlin.test.assertEquals
import testutil.ddTest

class AffineFormConstructorsTests {

    private val precision = 0.000001

    @Test
    fun testConstructorRangeWithNoiseSymbolNumber() {
        DDBuilder {
            val af = AffineForm.range(this, 1.0 ..2.0, "1")
            assertEquals(1.5, af.central, precision)
            assertEquals(2.0, af.max.toDouble())
            assertEquals(1.0, af.min.toDouble())
            assertEquals(0.5, af.radius, precision)
            assertEquals(1, af.xi.size)
            assertEquals(0.5, af.xi[1])
        }
    }

    @Test
    fun testConstructorRangeWithNoisSymbolNumberAndEqual() {
        DDBuilder {
            val af = AffineForm.range(this, 1.0..1.0, "1")
            assertEquals(1.0, af.central)
            assertEquals(1.0, af.max.toDouble())
            assertEquals(1.0, af.min.toDouble())
            assertEquals(0.0, af.radius)
            assertEquals(0, af.xi.size)
        }
    }

    @Test
    fun testAffineFormSingletons() = ddTest {
        val reals = AF.All
        assertEquals(DoubleBound.NegativeInfinity ..DoubleBound.PositiveInfinity, reals.min..reals.max )
        assertEquals(Double.NaN, reals.central)

        val empty = AF.Empty
        assertEquals(DoubleBound.PositiveInfinity ..DoubleBound.NegativeInfinity, empty.min .. empty.max)
        assertEquals(Double.NaN, empty.central)
    }
}