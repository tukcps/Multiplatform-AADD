package com.github.tukcps.aadd

class SignalSet (val name:String) {

    var timeUnit : String = ""
    var sampleDelta : Double = 0.0

    var samples : MutableList<AADD> = mutableListOf()
    var timePoints : MutableList<Double> = mutableListOf()

    fun add(sample:AADD,t:Double)
    {
        samples.add(sample)
        timePoints.add(t)
    }

    fun flowPipe(): MutableList<Pair<Double,Double>>
    {
        var flowpipe = mutableListOf<Pair<Double,Double>>()

        for(sample in samples)
        {
            flowpipe.add(Pair(sample.min,sample.max))
        }
        return flowpipe
    }

}