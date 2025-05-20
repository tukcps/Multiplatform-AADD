package benchmarks
import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DDBuilder
import kotlin.test.Test

class WaterLevelBenchmark {

    @Test
    fun waterLevelBenchmark() {
        DDBuilder {
            lpCalls = 0
            //val start = Clock.System.now().epochSeconds
            // println("==== Stupid water level monitor runtime verification test ====")
            // some constants with uncertain value.
            val outrate = real(-1.0..-0.6, "outrate")
            val inrate = real(0.6..1.0, "inrate")
            var level: AADD = real(1.0..11.0, "level")
            var rate = boolean("initial direction").ite(inrate, outrate)
            for (time in 0..20) {
                IF (level greaterThanOrEquals real(10.0))
                    rate = assign(rate, outrate)
                END()
                IF (level lessThanOrEquals real(2.0))
                    rate = assign(rate, inrate)
                END()
                level += rate
                // println("for t = $time level = ${level.getRange()}")
            }


            //println("Ptime: $ptime mSec")
            //println("CspSolver calls: ${lpCalls}")

            // Should be ok for all somehow recent computers.
            // - MacPro, 3.7GHz e.g. +- 400 (depends on temp, etc.)
            //assertTrue(ptime in 100..10000)
            //assertTrue(lpCalls in 3000..39000)
        }
    }
}
