package benchmarks

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.real.aa.*
import io.github.tukcps.aadd.values.real.minus
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertTrue

class NoisetermCompressionBenchmark {

    @Test
    fun logIControlTestBounds(){
        DDBuilder{
            settings.affineFormMaxNumberOfNoiseSymbols = 64

            val setValMin = 1.4
            val setValMax = 1.6
            val setValAA= AffineForm.range(this, 1.4 .. 1.6)
            var isvalmin = 0.1
            var isvalmax = 0.2
            var isvalAA = AffineForm.range(this,  0.1 .. 0.2)
            var piOutMin = 1.4
            var piOutMax = 1.41
            var piOutAA = AffineForm.range(this, 1.4 .. 1.41)
            var invalMin: Double
            var invalMax: Double
            var invalAA: AffineForm

            var i =0
            val steps = 10000 // set it to 10000 for making benchmarks
            while ( i <= steps) {
                invalMin = setValMin - isvalmin
                invalMax = setValMax - isvalmax
                invalAA = setValAA - isvalAA

                if (invalAA.min.finiteValue > 0){
                    piOutAA = ln(invalAA*10.0) + piOutAA
                } else {
                    piOutAA = (invalAA * 0.05) + piOutAA
                }

                if(invalMin > 0){
                    piOutMin += ln(invalMin * 10.0)
                    piOutMax += ln(invalMax * 10.0)
                } else {
                    piOutMin += (invalMin * 0.05)
                    piOutMax += (invalMax * 0.05)
                }

                isvalmin = isvalmin * 0.5 + piOutMin * 0.5
                isvalmax = isvalmax * 0.5 + piOutMax * 0.5
                isvalAA = (isvalAA * 0.5) + (piOutAA * 0.5)

                assertTrue(isvalAA.min.finiteValue<=isvalmin && isvalAA.max.finiteValue>=isvalmax)
                val difMin = isvalmin-isvalAA.min
                val difMax = isvalAA.max-isvalmax

                i++
                // println("At $i$: ${pioutAA.xi.size}, ${isvalAA.xi.size}, max: ${isvalAA.builder.noiseVars.maxIndexGarbage}")
            }
        }
    }
}