package examples.values

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import kotlin.test.Test
import kotlin.test.assertEquals

class AffineFormConstructorsTests {

    private val precision = 0.000001

    @Test
    fun testConstructorRangeWithNoiseSymbolNumber() {
        DDBuilder {
            val af = AffineForm(this, 1.0..2.0, 1)
            assertEquals(1.5, af.central, precision)
            assertEquals(2.0, af.max)
            assertEquals(1.0, af.min)
            assertEquals(0.5, af.radius, precision)
            assertEquals(0.0, af.r)
            assertEquals(1, af.xi.size)
            assertEquals(0.5, af.xi[1])
        }
    }

    @Test
    fun testConstructorRangeWithNoisSymbolNumberAndEqual() {
        DDBuilder {
            val af = AffineForm(this, 1.0..1.0, 1)
            assertEquals(1.0, af.central)
            assertEquals(1.0, af.max)
            assertEquals(1.0, af.min)
            assertEquals(0.0, af.radius)
            assertEquals(0.0, af.r)
            assertEquals(0, af.xi.size)
        }
    }
}