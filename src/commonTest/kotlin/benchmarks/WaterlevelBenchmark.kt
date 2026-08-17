package benchmarks
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.Real
import kotlin.test.Test
import kotlin.time.measureTime

class WaterLevelBenchmark {

    @Test
    fun waterLevelBenchmark() {
        DDBuilder {
            lpCalls = 0
            settings.affineFormMaxNumberOfNoiseSymbols = 128
            println("==============================================================")
            println("==== Stupid water level monitor runtime verification test ====")
            println("====  (level should be 1..11 plus/minus overapproximation ====")
            println("==============================================================")
            // some constants with uncertain value.
            val time = measureTime {
                val outrate = real(-1.0..-0.6, "outRate")
                val inRate = real(0.6..1.0, "inRate")
                var level: Real = real(1.0..11.0, "level")
                var rate: Real = boolean("initial direction").ite(inRate, outrate)
                for (time in 0..20) {
                    IF(level greaterThanOrEquals 10.0)
                      rate = assign(rate,outrate) // assign considers the path condition of IF(...)
                    END()
                    IF(level lessThanOrEquals 2.0)
                      rate = assign(rate, inRate)  // assign considers the path condition of IF(...)
                    END()
                    level += rate
                    println("for t = $time level = ${level.getRange()}")
                }
            }

            println("Ptime: $time mSec")
            println("LPSolver calls: ${lpCalls}")

            // Should be ok for all somehow recent computers.
            // - MacPro, 3.7GHz e.g. +- 400 (depends on temp, etc.)
            //assertTrue(ptime in 100..10000)
            //assertTrue(lpCalls in 3000..39000)
        }
    }
}
