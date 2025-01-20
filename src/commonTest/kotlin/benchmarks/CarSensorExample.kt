@file:Suppress("unused", "UnusedVariable")

package examples.benchmarks
import com.github.tukcps.aadd.DDBuilder
import kotlin.test.Test

class CarSensorExample {

    var obstacleS1  = doubleArrayOf(70.0,50.5,33.0,12.4,17.0)
    // correct value:yes,   no      , no,       no      ,yes
    var obstacleS2  = doubleArrayOf(70.02314,51.52571,33.54623,12.52996,17.09503)

    fun init(){

    }
    @Test
    fun main(){
        DDBuilder {
            config.noiseSymbolsFlag=true
            config.originalFormsFlag=true
            //noise symbols of the distance Sensor
            val lightDisturbance = 1.2
            val processNoise1 = 0.1
            val processNoise2 = 0.3
            val distant1Range = real(obstacleS1[0]-lightDisturbance .. obstacleS1[0]+lightDisturbance).
            plus(this.real(-processNoise1..processNoise1, "2"))

            val distant2Range = real(obstacleS2[0]-lightDisturbance .. obstacleS2[0]+lightDisturbance).
            plus(this.real(-processNoise2..processNoise2, "2"))
            val calculatedDistance2Square = distant1Range * distant1Range + 2.89
            val calculatedDistance2 = calculatedDistance2Square.sqrt()

            val closeRange = real(30.0) greaterThan calculatedDistance2Square
            val error = (distant2Range - calculatedDistance2 )
            val severityOfError = error greaterThan real(-3.0 .. 3.0)
        }
    }
}