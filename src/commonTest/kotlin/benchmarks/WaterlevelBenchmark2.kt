package benchmarks

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.contains
import com.github.tukcps.aadd.values.Range
import kotlin.math.PI
import kotlin.math.ln
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class WaterLevelBenchmark2 {

    @Test
    @Ignore
    fun runtimeVerificationBenchmarkOutFlowAtBottom() {
        DDBuilder {
            val gravity = 9.81
            val c = 0.9 // constant for flow speed of the drain
            val areaOfOutflowPipe= kotlin.math.exp(ln(0.5)*2)* PI //A_0
            lpCalls = 0
            // val start = System.currentTimeMillis()
            // println("==== Stupid water level monitor runtime verification benchmark ====")
            // some constants with uncertain value.
            val inrate = real(0.6..1.0, "inrate")
            var level: AADD = real(1.0..11.0, "level")
            // outflow after Torricelli's law
            var dlevel = real(10.0)
            val aux = real(2*gravity) * dlevel
            val outrate = real(- c* areaOfOutflowPipe) * aux.sqrt()
            var rate = boolean("initial direction").ite(inrate, outrate)
            var drate = outrate
            var inrange: BDD = True
            var rlevel: AADD = real(1.0..11.0, (-1).toString())
            repeat (40) {
                // For discrete fault:
                // if (time > 22) dlevel = 2.0
                //print("  At time: $time sec. physical water level is: " + String.format("%.2f", dlevel))
                if (dlevel.max >= 10.0) drate = outrate
                if (dlevel.min < 2.0) drate = real(0.9)
                // For parametric fault:
                // if (time >= 19 && drate == 0.9) drate = 0.5
                dlevel += drate

                // println(" symbolic is: " + level.getRange() + " and has leaves: " + level.numLeaves())
                // Check the discrete state ...
                inrange = (level greaterThanOrEquals real(10.0)) and (level lessThanOrEquals real(10.0)) and inrange
                // ... or better: check with intersect:
                rlevel = rlevel.constrainTo(Range(10.0 - 0.01..10.0 + 0.01))
                assertTrue(level in 0.9..11.1)
                IF(level greaterThan real(10.0))
                rate = assign(rate, outrate)
                END()
                IF(level lessThanOrEquals real(2.0))
                rate = assign(rate, inrate)
                END()
                // println("                  feasible paths:  " + inrange.numTrue() + " that match physical data.")
                // println("                  feasible leaves: " + rlevel.numFeasible() + " with Range: " + rlevel)
                // println("                  feasible leaves: " + dlevel.numFeasible() + " with Range: " + dlevel)
                level += rate
                rlevel += rate
            }
            // val ptime = System.currentTimeMillis() - start
            // println("Ptime: $ptime mSec")
            // println("CspSolver calls: $lpCalls")

            // Should be ok for all somehow recent computers.
            // - MacPro, 3.7GHz e.g. +- 2000 (depends on temp, etc.)
            //Assertions.assertTrue(ptime in 1000..100000)
            assertTrue(lpCalls in 120000..1900000)
        }
    }

    @Test
    @Ignore
    fun runtimeVerificationBenchmark() {
        DDBuilder {
            lpCalls = 0
            //val start = System.currentTimeMillis()
            // println("==== Stupid water level monitor runtime verification benchmark ====")
            // some constants with uncertain value.
            val outrate = real(-1.0..-0.6, "outrate")
            val inrate = real(0.6..1.0, "inrate")
            var level: AADD = real(1.0..11.0, "level")
            var rate = boolean("initial direction").ite(inrate, outrate)
            var drate = 0.9
            var dlevel = 4.0
            var inrange: BDD = True
            var rlevel: AADD = real(1.0..11.0, (-1).toString())
            for (time in 0 .. 39) {
                // For discrete fault:
                // if (time > 22) dlevel = 2.0
                // print("  At time: $time sec. physical water level is: ${dlevel.toRoundedString(3)}")
                if (dlevel >= 10.0) drate = -.8
                if (dlevel < 2.0) drate = 0.9
                // For parametric fault:
                // if (time >= 19 && drate == 0.9) drate = 0.5
                dlevel += drate

                // println(" symbolic is: " + level.getRange() + " and has leaves: " + level.numLeaves())
                // Check the discrete state ...
                inrange = (level greaterThanOrEquals real(dlevel)) and (level lessThanOrEquals real(dlevel)) and inrange
                // ... or better: check with intersect:
                rlevel = rlevel.constrainTo(Range(dlevel - 0.01..dlevel + 0.01))
                assertTrue(level in 0.9..11.1)
                IF(level greaterThan real(10.0))
                rate = assign(rate, outrate)
                END()
                IF(level lessThanOrEquals real(2.0))
                rate = assign(rate, inrate)
                END()
                // println("                  feasible paths:  " + inrange.numTrue() + " that match physical data.")
                // println("                  feasible leaves: " + rlevel.numFeasible() + " with Range: " + rlevel)
                level += rate
                rlevel += rate
            }
            // val ptime = System.currentTimeMillis() - start
            // println("Ptime: $ptime mSec")
            // println("CspSolver calls: $lpCalls")

            // Should be ok for all somehow recent computers.
            // - MacPro, 3.7GHz e.g. +- 2000 (depends on temp, etc.)
            //Assertions.assertTrue(ptime in 1000..100000)
            assertTrue(lpCalls in 120000..1900000)
        }
    }

    @Test
    @Ignore
    fun runtimeVerificationBenchmarkWithoutR() {
        DDBuilder {
            this.config.noiseSymbolsFlag = true
            this.config.maxSymbols = 4
            this.config.mergeSymbols = 3
            this.config.xiHashMapSize = 10
            lpCalls = 0
            // val start = System.currentTimeMillis()
            // println("==== Stupid water level monitor runtime verification benchmark ====")
            // some constants with uncertain value.
            val outrate = real(-1.0..-0.6, "outrate")
            val inrate = real(0.6..1.0, "inrate")
            var level: AADD = real(1.0..11.0, "level")
            var rate: AADD = boolean("initial direction").ite(inrate, outrate)
            var drate = 0.9
            var dlevel = 4.0
            var inrange: BDD = True
            var rlevel: AADD = real(1.0..11.0, (-1).toString())
            for (time in 0 .. 39) {
                // For discrete fault:
                // if (time > 22) dlevel = 2.0
                // print("  At time: $time sec. physical water level is: ${dlevel.toRoundedString(2)}")
                if (dlevel >= 10.0) drate = -.8
                if (dlevel < 2.0) drate = 0.9
                // For parametric fault:
                // if (time >= 19 && drate == 0.9) drate = 0.5
                dlevel += drate

                // println(" symbolic is: " + level.getRange() + " and has leaves: " + level.numLeaves())
                // Check the discrete state ...
                inrange = (level greaterThanOrEquals real(dlevel)) and (level lessThanOrEquals real(dlevel)) and inrange
                // ... or better: check with intersect:
                rlevel = rlevel.constrainTo(Range(dlevel - 0.01..dlevel + 0.01))
                assertTrue(level in 0.9..11.1)
                IF(level greaterThan real(10.0))
                    rate = assign(rate, outrate)
                END()
                IF(level lessThanOrEquals real(2.0))
                    rate = assign(rate, inrate)
                END()
                // println("                  feasible paths:  " + inrange.numTrue() + " that match physical data.")
                // println("                  feasible leaves: " + rlevel.numFeasible() + " with Range: " + rlevel)
                level += rate
                rlevel += rate
            }
            // val ptime = System.currentTimeMillis() - start
            // println("Ptime: $ptime mSec")
            // println("CspSolver calls: $lpCalls")

            // Should be ok for all somehow recent computers.
            // - MacPro, 3.7GHz e.g. +- 2000 (depends on temp, etc.)
            //Assertions.assertTrue(ptime in 1000..100000)
            assertTrue(lpCalls in 120000..1900000)
        }
    }
}
