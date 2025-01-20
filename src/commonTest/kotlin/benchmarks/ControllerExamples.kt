@file:Suppress("UNUSED_VARIABLE", "unused")

package examples.benchmarks

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.math.*

class ControllerExamples {

    private fun encodeDoubleInWithComma(input:Double):String {
        var euroNumber = "" + input.toInt() + ","
        var decimalNumber: Int = if (input > 0)
            ((input - input.toInt()) * 1e6).toInt()
        else
            ((input + input.toInt()) * 1e6).toInt()

        decimalNumber = abs(decimalNumber)

        var size = 5
        while (size > -1) {
            if (decimalNumber < 10.0.pow(size))
                euroNumber += "0"
            else
                break
            size--
        }
        euroNumber += "" + decimalNumber

        return euroNumber
    }


    @Test
    fun piControlTestNumerical(){
        DDBuilder{
            config.noiseSymbolsFlag = true
            val setvalMin =0.4
            val setvalMax =0.6
            //val setvalAA= AF(0.4, 0.6, -1)
            var isvalMin =0.9
            var isvalMax =1.0
            //var isvalAA = AF( 0.9,1.0,-1)
            var pioutMin = 0.4
            var pioutMax = 0.41
            //var pioutAA = AF(0.4,0.41,-1)
            //var invalAA: AffineForm
            var invalMin :Double
            var invalMax :Double
            val europianExcel=true

            var i =0
            while ( i <=1000){
                invalMin = setvalMin - isvalMin
                invalMax = setvalMax - isvalMax
                pioutMin += (invalMin * 0.05)
                pioutMax += (invalMax * 0.05)
                isvalMin = (isvalMin * 0.5) + (pioutMin * 0.5)
                isvalMax = (isvalMax * 0.5) + (pioutMax * 0.5)


                i++
            }

            //bw.write(""+isvalAA.builder.noiseVars.newGarbageVar())

        }
    }


    @Test
    fun logIControlTestBounds(){
        DDBuilder{
            config.noiseSymbolsFlag = true

            val setvalmin = 1.4
            val setvalmax = 1.6
            val setvalAA= AffineForm(this, 1.4 .. 1.6)
            var isvalmin = 0.1
            var isvalmax = 0.2
            var isvalAA = AffineForm(this,  0.1 .. 0.2)
            var pioutmin = 1.4
            var pioutmax = 1.41
            var pioutAA = AffineForm(this, 1.4 .. 1.41)
            var invalmin: Double
            var invalmax: Double
            var invalAA: AffineForm
            val europeanExcel=true

            var i =0
            while ( i <=10000){
                invalmin = setvalmin - isvalmin
                invalmax = setvalmax - isvalmax
                invalAA = setvalAA - isvalAA

                if (invalAA.min>0){
                    pioutAA = (invalAA*10.0).log() + pioutAA
                } else {
                    pioutAA = (invalAA * 0.05) + pioutAA
                }

                if(invalmin > 0){
                    pioutmin += ln(invalmin * 10.0)
                    pioutmax += ln(invalmax * 10.0)
                } else {
                    pioutmin += (invalmin * 0.05)
                    pioutmax += (invalmax * 0.05)
                }

                isvalmin = isvalmin * 0.5 + pioutmin * 0.5
                isvalmax = isvalmax * 0.5 + pioutmax * 0.5
                isvalAA = (isvalAA * 0.5) + (pioutAA * 0.5)

                assertTrue(isvalAA.min<=isvalmin && isvalAA.max>=isvalmax)
                val difMin = isvalmin-isvalAA.min
                val difMax = isvalAA.max-isvalmax

                i++
            }
            //        bw.write(""+isvalAA.builder.noiseVars.newGarbageVar())
            //        bw.newLine()
            //        bw.write(""+isvalAA.xi.size)
        }
    }


    @Test
    fun logIControlTestNumerical(){
        val setvalmin = 1.4
        val setvalmax = 1.6
//        val setvalAA= AF(1.4, 1.6, -1)
        var isvalmin = 0.1
        var isvalmax = 0.2
//        var isvalAA = AF( 0.1,0.2,-1)
        var pioutmin = 1.4
        var pioutmax = 1.41
//        var pioutAA = AF(1.4,1.41,-1)
        var invalmin: Double
        var invalmax: Double
//        var invalAA: AffineForm
        val europianExcel=true

        var i =0
        while ( i <=10000){
            invalmin = setvalmin - isvalmin
            invalmax = setvalmax - isvalmax
//            invalAA = setvalAA - isvalAA
            if(invalmin > 0){
//            if (invalAA.min>0)
                pioutmin += ln(invalmin * 10.0)
                pioutmax += ln(invalmax * 10.0)
//                pioutAA = (invalAA*10.0).log() + pioutAA
            }

            else{
                pioutmin += (invalmin * 0.05)
                pioutmax += (invalmax * 0.05)
//                pioutAA = (invalAA * 0.05) + pioutAA
            }
            isvalmin = isvalmin * 0.5 + pioutmin * 0.5
            isvalmax = isvalmax * 0.5 + pioutmax * 0.5
//            isvalAA = (isvalAA * 0.5) + (pioutAA * 0.5)

            i++
        }

//        bw.write(""+isvalAA.builder.noiseVars.newGarbageVar())
//        bw.newLine()
//        bw.write(""+isvalAA.xi.size)

    }


    @Test
    fun piControlTest(){
        DDBuilder{

            config.noiseSymbolsFlag = true

            val setvalAA= AffineForm(this, 0.4 .. 0.6)
            var isvalAA = AffineForm(this,  0.9 .. 1.0)
            var pioutAA = AffineForm(this, 0.4 .. 0.41)
            var invalAA: AffineForm
            val europeanExcel=true

            var i =0
            while ( i <=1000){
                invalAA = setvalAA - isvalAA
                pioutAA = (invalAA * 0.05) + pioutAA
                isvalAA = (isvalAA * 0.5) + (pioutAA * 0.5)

                i++
            }
            val size = isvalAA.xi.size

        }
    }


    // TODO fix test
    @Test
    fun logIControlTest(){
        /*
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            this.config.roundingErrorMappingFlag = true
            this.config.reductionFlag = true
            this.config.thresholdFlag = true
            val setvalAA= AF(1.4, 1.6, -1)
            var isvalAA = AF( 0.1,0.2,-1)
            var pioutAA = AF(1.4,1.41,-1)
            var invalAA: AffineForm
            val europianExcel=true

            var i =0
            var newUsage = this.noiseVars.getUsed()
            while ( i <=1000){
                invalAA = setvalAA - isvalAA
                if (invalAA.min>0)
                    pioutAA = (invalAA*10.0).log() + pioutAA
                else
                    pioutAA = (invalAA * 0.05) + pioutAA
                isvalAA = (isvalAA * 0.5) + (pioutAA * 0.5)

                /*               if (i%1000 == 0){
                                   isvalAA.simplification()
                               }*/
                if (i%100 == 0){ //1000 iterations //Threshold 10^(-12)
                    val oldUsage = newUsage
                    newUsage = this.noiseVars.getUsed()
                    println(newUsage)
                    println("Differenz: "+ newUsage.minus(oldUsage))
                }
                i++
            }

            val usage = this.noiseVars.getUsed()
            println("Total number of usages: $usage")

            assertTrue(isvalAA.min in 1.39..1.4 && isvalAA.max in 1.6..1.61)
        }

         */
    }

    /** TODO: switch this into specific example file */
    /*
    private fun bweulerlpwm(e0:AffineForm):AffineForm
    {
        DDBuilder{
            // static vars defining the behaviour of the PID controller
            val kp = AF(1.0)
            val kd = AF(0.01)
            val ki = AF(0.05)
            val sampling_time = AF(0.01)
            val N = AF(20.0)
            var e1 = AF(0.0 , 0.0,"e1")
            var e2 = AF(0.0 , 0.0,"e2")
            var u2 = AF(0.0,0.0,"e3")
            var u1 = AF(0.0,0.0,"e4")
            var cpid_state = AF(0.0,0.0,-1)
            var cycle_counter = 0.0

            val a0 = (AF(1.0) + N * sampling_time)
            val a1 = AF(-1.0) * (AF(2.0) + N * sampling_time)
            val a2 = AF(1.0)

            val b0 = kp * (AF(1.0) + N * sampling_time) + ki * sampling_time * (AF(1.0) + N * sampling_time) + kd * N
            val b1 = AF(-1.0) * (kp * (AF(2.0) + N * sampling_time) + ki * sampling_time + AF(2.0) * kd * N)
            val b2 = kp + kd * N

            val ku1 = a1 / a0
            val ku2 = a2 / a0
            val ke0 = b0 / a0
            val ke1 = b1 / a0
            val ke2 = b2 / a0

            val ke0e0 = (ke0 * e0)
            val ke1e1 = (ke1 * e1)
            val ke2e2 = (ke2 * e2)
            val ku1u1 = (ku1 * u1)
            val ku2u2 = (ku2 * u2)

            val s1 = ke0e0 + ke1e1
            val s2 = s1 + ke2e2
            val s3 = s2 - ku1u1
            val con_out_val = s3 - ku2u2

            e2 = e1
            e1 = e0

            u2 = u1
            u1 = con_out_val

        }
        return con_out_val
    }

    @Test @Disabled
    fun testPIDcontrollerBackwardeulerPWM() {

        //IO channels
        DDBuilder {
            val setValue = AF(0.5, 0.7, "set")
            var isValue = AF(0.4, 0.6, "is")
            var controlloutput: AffineForm

            var err = AF(0.0)
            for (i in 1..400) {
                println("--------")
                //isValue = (tester.bwe(setValue-isValue))/scalar(100.0)
                err = setValue - isValue
                var pidout = bweulerlpwm(err)
                isValue += pidout * AF(0.1)
                if (i == 20)
                    println("20")
                if (i == 60)
                    println("60")
                if (i == 100)
                    println("100")
                if (i == 150)
                    println("150")
                if (i == 200)
                    println("200")
                if (i == 300)
                    println("300!!----")
                if (i == 350)
                    println("350!!----")
                if (i == 390)
                    println("390!!----")
                println("iteration:" + i + " " + isValue)

            }
        }
    }*/
}