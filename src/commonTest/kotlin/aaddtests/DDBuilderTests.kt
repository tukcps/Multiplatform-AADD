package aaddtests

import com.github.tukcps.aadd.DDBuilder
import kotlin.test.Test
import kotlin.test.assertEquals


class DDBuilderTests {

    @Test
    fun switchConfig() {
        DDBuilder {
            val newConfigJsonString = "{"+
                    "\"kotlinLPFlag\" : true,\n" +
                    " \"noiseSymbolsFlag\": true,\n" +
                    "\"originalFormsFlag\": true,\n" +
                    "\"roundingErrorMappingFlag\": true,\n" +
                    "\"joinTh\": 0.002 ,\n" +
                    "\"lpCallTh\": 0.002,\n"+
                    "\"toStringVerbose\": true,\n" +
                    "\"maxSymbols\": 201,\n"+
                    "\"mergeSymbols\": 11,\n"+
                    "\"xiHashMapSize\": 301, \n" +
                    "\"lpCallsBeforeOutput\": true \n" +
                    "\"reductionFlag\": false \n" +
                    "\"thresholdFlag\": true \n" +
                    "\"threshold\": 0.000000000002 \n" +
                    "}"
            assertEquals(this.config.kotlinLPFlag,false)
            assertEquals(this.config.noiseSymbolsFlag,false)
            assertEquals(this.config.joinTh,0.001)
            assertEquals(this.config.lpCallTh,0.001)
            assertEquals(this.config.toStringVerbose,false)
            assertEquals(this.config.originalFormsFlag,false)
            assertEquals(this.config.roundingErrorMappingFlag, false)
            assertEquals(this.config.maxSymbols,200)
            assertEquals(this.config.mergeSymbols,10)
            assertEquals(this.config.xiHashMapSize,300)
            assertEquals(this.config.lpCallsBeforeOutput,false)
            assertEquals(this.config.reductionFlag,true)
            assertEquals(this.config.thresholdFlag,false)
            assertEquals(this.config.threshold,0.000000000001)

            this.setExternalConfig(newConfigJsonString)

            assertEquals(this.config.kotlinLPFlag,true)
            assertEquals(this.config.noiseSymbolsFlag,true)
            assertEquals(this.config.joinTh,0.002)
            assertEquals(this.config.lpCallTh,0.002)
            assertEquals(this.config.toStringVerbose,true)
            assertEquals(this.config.originalFormsFlag,true)
            assertEquals(this.config.roundingErrorMappingFlag, true)
            assertEquals(this.config.maxSymbols,201)
            assertEquals(this.config.mergeSymbols,11)
            assertEquals(this.config.xiHashMapSize,301)
            assertEquals(this.config.lpCallsBeforeOutput,true)
            assertEquals(this.config.reductionFlag,false)
            assertEquals(this.config.thresholdFlag,true)
            assertEquals(this.config.threshold,0.000000000002)
        }
    }
}