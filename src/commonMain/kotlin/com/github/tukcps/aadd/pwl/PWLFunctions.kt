package com.github.tukcps.aadd.pwl

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.values.AffineForm

/**
 * This is an implementation of the Rectified linear unit function. It works as follows: If the interval that is below
 * 0 of the input affine form is larger than then the given split_threshold a split is performed by introducing an internal
 * node with the constraint af >= 0. In this internal node the F sub tree is assigned 0 while the T subtree has af assigned.
 * In the case that the threshold is not surpassed the affine form is just straight passed through the Relu function as
 * a simple AADD Leaf with af as its value. If you want to always split simply set the threshold at 0
 *
 * @param af : Input affine form into the RELU function
 * @param builder : Context in which the RELU function is performed
 * @param split_threshold : Threshold at which a split is considered (Must be a value between 0 and 1 as it represents a % value)
 *
 * @return Either an internal node or a leaf node depending if a split has been performed or not
 * */
fun relu(af: AffineForm,builder:DDBuilder, split_threshold : Double = 0.1) : AADD {
    with(builder)
    {
        var res: AADD = AADD.Leaf(this, af)
        if (af.min <= 0)
        {
            val pct = (0.0 - af.min) / (af.max - af.min)
            if(pct < split_threshold)
            {
                // Case that our split threshold is not reached so we return simply the leaf
                return res
            }
            else
            {
                // Case that we are above the split threashold so we return the tree
                IF(res.greaterThanOrEquals(0.0))
                    res = assign(res, res)
                ELSE()
                    res = assign(res, real(0.0..0.0))
                END()
                return res
            }
        }
        else
        {
            // If the whole affine form is anyways above 0 we can just return the leaf again
            return res
        }
    }
}


/*
      /**
       *  piece-wise linearisation of the AffineForm standart non-linear functions.
       *  There will be a python-visulalisation created in ${ProcetDir}/out/ApproximationVisualisation/
       *  If this folders does not exist you need to create it first
       *  it can be call with python ${filename}.py. Times and DIV needs matplotlib installed
       * @param function enum of the function that should be applied
       * @param extraUncertainty the additional uncertainty, that is allowed for each interval
       * @param other in case of function = TIMES OR DIV other contains the secon operand
       * @return the AADD of the piece-wise linearisation of function(this)
       */
      // TODO piece wise stuff

      fun pwlWithVisualisation(function:EFunction, extraUncertainty: Double, other :AffineForm=this, extraIntervals:Int=-1,visualisation: Boolean =true):AADD{
          require(extraUncertainty>0){"The ExtraUncertainty has to be positiv"}
          val filename:String
          val fct: FunctionRepresentation?
          val IsAbsolutUncertainty: Boolean?

          when(function){
              EFunction.TIMES ->  {
                  filename = "mult(["+this.min+","+this.max+"],["+other.min+","+other.max+"])"
                  val config = ApproxMult()
                  val approx =config.multiplication(this,other,extraUncertainty,limetIntervals = extraIntervals,printTheResultVisualisation = visualisation)

                  config.finalisePlot(filename,false,this.min,this.max,other.min,other.max)

                  return approx
              }
              EFunction.DIV   ->  {
                  filename = "div(["+this.min+","+this.max+"],["+other.min+","+other.max+"])"
                  val reciprop = other.inv()
                  val config = ApproxMult()
                  val approx:AADD
                  if (other.min<=0.0 && other.max>=0.0) {

                      val DistToPole = this.builder.AFDivDistToPole
                      // make sure that there will no division through 0 calculated
                      val decisionGt0 = builder.leaf(other).greaterThan(DistToPole)
                      val decisionLt0 = builder.leaf(other).lessThan(-DistToPole)

                      // positiv reciprop definitiv positiv due to the above if
                      var l = DistToPole
                      var u = other.max
                      // is a min-Range approximation
                      var alpha = -1.0 / (u * u)
                      var delta = (u+l)*(u+l)/(2*u*u*l)
                      var noise = (u-l)*(u-l)/(2*u*u*l)
                      val recipropPositiv = other.affine(alpha,delta,noise)
                      // negativ reciprop
                      l = abs(- DistToPole)
                      u = abs(other.min)
                      alpha = -1.0 / (u * u)
                      delta = (u+l)*(u+l)/(-2*u*u*l)
                      noise = (u-l)*(u-l)/(2*u*u*l)
                      val extraIntervalsHigh :Int
                      if(extraIntervals/2.0>extraIntervals/2)
                          extraIntervalsHigh= extraIntervals/2+1
                      else
                          extraIntervalsHigh= extraIntervals/2

                      val recipropNegative = other.affine(alpha,delta,noise)
                      val AADDgt0 = config.multiplication(this, recipropPositiv, extraUncertainty, minY = max(DistToPole, recipropPositiv.min), maxY = max(1.048576e-6, recipropPositiv.max),limetIntervals = extraIntervalsHigh)
                      config.finalisePlot(filename + "(1)", false, this.min, this.max, DistToPole, other.max)

                      val AADDlt0 = config.multiplication(this, recipropNegative, extraUncertainty, minY = min(-DistToPole, recipropNegative.min), maxY = min(-1.048576e-6, recipropNegative.max),limetIntervals = extraIntervals/2)
                      config.finalisePlot(filename + "(2)", false, this.min, this.max, other.min, -DistToPole)


                      approx = decisionGt0.ite(AADDgt0, decisionLt0.ite(AADDlt0, builder.RealsNaN))
                  }else{
                      approx = config.multiplication(this,reciprop,printTheResultVisualisation = visualisation)
                  }
                  return approx
              }
              EFunction.EXP   -> {
                  //throw IllegalArgumentException("Currently not stable due to under-approximation")
                  //CURRENTLY has a Underapproximation, that needs to be fixed in the next version
                  filename ="exp(["+this.min+","+this.max+"]"
                  fct = GeneralExponential()
                  IsAbsolutUncertainty =false
              }
              EFunction.SQRT   -> {
                  filename ="sqrt(["+this.min+","+this.max+"]"
                  fct = Sqrt()
                  val approx = ApproximationScheme()

                  val definitionRange = this.builder.leaf(this).greaterThanOrEquals(0.0)
                  val approximation = approx.wrapperApproxLinearStraigthForward(fct,this,extraUncertainty,extraIntervals, true , max(0.0,this.min),max(0.0,this.max))

                  val result = definitionRange.ite(approximation,this.builder.RealsNaN)
                  if(visualisation)
                      approx.finalisePlot(filename,this.min,this.max,fct.pythonFunctionCode())

                  return result
              }
              EFunction.LOG   -> {
                  filename ="log(["+this.min+","+this.max+"]"
                  val approx = ApproximationScheme()
                  fct = Logarithm()

                  val boarderToPole = this.builder.AFLogDistToPole
                  // parameter for deciding the closeness to zero
                  val definitionRange = this.builder.leaf(this).greaterThan(boarderToPole)
                  val definitionFastGrowth = this.builder.leaf(this).greaterThan(1.0)
                  // the interval log(0) to log(1)
                  val fastGrow = approx.wrapperApproxLinearStraigthForward(fct,this,extraUncertainty,2*extraIntervals/3,false,max(boarderToPole,this.min),min(1.0,this.max))
                  val biggerThenOne = approx.wrapperApproxLinearStraigthForward(fct,this,1.0,extraIntervals/3,true,max(1.0,this.min),max(1.0,this.max),false)

                  val result = definitionRange.ite(definitionFastGrowth.ite(biggerThenOne,fastGrow),this.builder.RealsNaN)
                  if(visualisation)
                      approx.finalisePlot(filename,this.min,this.max,fct.pythonFunctionCode())

                  return result
              }
              EFunction.SIN   -> {
                  filename ="sin(["+this.min+","+this.max+"]"
                  fct = GeneralSin()
                  IsAbsolutUncertainty =true
              }
              EFunction.COS -> {
                  filename ="cos(["+this.min+","+this.max+"]"
                  fct = GeneralSin(1.0,1.0,PI/2)
                  IsAbsolutUncertainty =true
              }
          }
          val approx = ApproximationScheme()

          val approximation = approx.wrapperApproxLinearStraigthForward(fct,this,extraUncertainty,extraIntervals,useAbsoulteNoise = IsAbsolutUncertainty)
          if(visualisation)
              approx.finalisePlot(filename,this.min,this.max,fct.pythonFunctionCode())

          return approximation
      }*/
/*
/**
 * Creates a human-readable string that represents the affine form.
 * The string is by default only a short range representation.
 * If the field diagnostics_on is set to true, full information is given.
 * @return String
 */
// TODO string stuff with formating issue

override fun toString(): String {
    var af = super.toString()
    if (builder.config.toStringVerbose) {
        if (isScalar() || isRange()) {
            af += " \u2286 " + String.format("%.2f", central)
            xi.keys.forEach { af += " + "+ String.format("%.2f", xi[it]) + "\u03B5" + it }
            if (r != 0.0) af += " \u00B1 " + String.format("%.2f", r)
        }
    }
    return af
}*/
/*
    fun pythonVisualApprox(alpha: Double,delta: Double,noise: Double,orgfct: String){
        var plot = StringBuilder()
        plot.append("import matplotlib.pyplot as plt\n" +
                "import numpy as np\n" +
                "import math\n" +
                "######### plot approximation\n")
        plot.append("x=np.linspace(" + min + "," + max + "," +  max((ceil((max - min) * 500)).toInt(),100) + ")\n")

        plot.append("yUpper= $alpha*x + $delta +$noise\n")
        plot.append("yApprox= $alpha*x + $delta \n")
        plot.append("yLower= $alpha*x + $delta -$noise\n")
        plot.append("plt.plot(x,yUpper,color='k')\n")
        plot.append("plt.plot(x,yApprox,color='r')\n")
        plot.append("plt.plot(x,yLower,color='k')\n\n")
        plot.append("######## original function\n")
        plot.append("y=$orgfct\n")
        plot.append("\n")
        //ploting original function
        plot.append("plt.plot(x,y,color='b')\n\n")

        plot.append("plt.xlabel('x')\n" +
                "plt.ylabel('y')\n" +
                "plt.grid()\n" +
                "\n"+
                "plt.show()\n")


        print(plot.toString())
    }
    */
/*
    /**
     * cosine function as a piece-wise approximated AADD
     * @param extraUncertainty maximal overapproximation in the individual intervals. Default: 1
     * @return the AADD with the piece-wise approximation from the 'this' AffineForm
     */
    // TODO piece wise stuff

    fun cos(extraUncertainty: Double=0.3,extraIntervals:Int=-1): AADD{
        val cos = GeneralSin(1.0,1.0,PI/2)
        val approx = ApproximationScheme()
        val approximation = approx.wrapperApproxLinearStraigthForward(cos,this,extraUncertainty,extraIntervals)

        return approximation
    }*/
/*
       /**
        * sine-function as a piece-wise approximated AADD
        * @param extraUncertainty maximal overapproximation in the individual intervals. Default: 1
        * @return the AADD with the piece-wise approximation from the 'this' AffineForm
        */
       // TODO piece wise stuff

       fun sin(extraUncertainty: Double = 0.3, extraIntervals: Int = -1): AADD {
           val sin = GeneralSin()
           val approx = ApproximationScheme()

           return approx.wrapperApproxLinearStraigthForward(sin, this, extraUncertainty, extraIntervals)
       }*/
/*
   /**
    * div as a piece-wise approximated AADD
    * @param extraUncertainty maximal overapproximation in the induvidial intervals. Default: 1
    * @return the AADD with the piece-wise approximation from the 'this' AffineForm
    * TODO piecewise stuff
    */
   fun div(other: AffineForm, extraUncertainty: Double=10.0, extraIntervals: Int=4):AADD {
       val config = ApproxMult()
       val approx:AADD

       if (other.min<=0.0 && other.max>=0.0) {
           val distToPole = this.builder.AFDivDistToPole
           // make sure that there will no division through 0 calculated
           val decisionGt0 = builder.leaf(other).greaterThan(distToPole)
           val decisionLt0 = builder.leaf(other).lessThan(-distToPole)

           // positiv reciprop definitiv positiv due to the above if
           var l = distToPole
           var u = other.max
           // is a min-Range approximation
           var alpha = -1.0 / (u * u)
           var delta = (u+l)*(u+l)/(2*u*u*l)
           var noise = (u-l)*(u-l)/(2*u*u*l)
           val recipropPositiv = other.affine(alpha,delta,max(0.0,noise))
           //through the knowledge we know that 1/max is positiv and the smallest posible value
           recipropPositiv.min=1.0/other.max
           recipropPositiv.max=1.0/distToPole
           // negativ reciprop
           l = abs(- distToPole)
           u = abs(other.min)
           alpha = -1.0 / (u * u)
           delta = (u+l)*(u+l)/(-2*u*u*l)
           noise = (u-l)*(u-l)/(2*u*u*l)
           val recipropNegative = other.affine(alpha,delta,max(0.0,noise))
           recipropNegative.min=1.0/-distToPole
           recipropNegative.max=1.0/other.min

           val extraIntervalsHigh :Int
           if(extraIntervals/2.0>extraIntervals/2)
               extraIntervalsHigh= extraIntervals/2+1
           else
               extraIntervalsHigh= extraIntervals/2

           val AADDgt0 = config.multiplication(this, recipropPositiv, extraUncertainty, minY = max(distToPole, recipropPositiv.min), maxY = max(distToPole, recipropPositiv.max),limetIntervals = extraIntervalsHigh)
           val AADDlt0 = config.multiplication(this, recipropNegative, extraUncertainty, minY = min(-distToPole, recipropNegative.min), maxY = min(-distToPole, recipropNegative.max), limetIntervals = extraIntervals/2)

           approx = decisionGt0.ite(AADDgt0, decisionLt0.ite(AADDlt0, builder.RealsNaN))
       } else {
           val reciprop = other.inv()
           approx = config.multiplication(this,reciprop,limetIntervals = extraIntervals)
       }
       return approx
   }*/
/*
        /**
         * Natural logarithm as a pi
         * ece-wise approximated AADD
         * @param extraUncertainty maximal over-approximation in the individual intervals. Default: 1
         * @return the AADD with the piece-wise approximation from the 'this' AffineForm
         * TODO piecwise stuff
         */

        fun log(extraUncertainty : Double=0.5, extraIntervals : Int = -1) : AADD {
            val log = Logarithm()
            val approx = ApproximationScheme()

            val distToPole = this.builder.AFLogDistToPole
            // parameter for deciding the closeness to zero
            val definitionRange = this.builder.leaf(this).greaterThan(distToPole)
            val definitionFastGrowth = this.builder.leaf(this).greaterThan(1.0)
            // the interval log(0) to log(1)
            val fastGrow = approx.wrapperApproxLinearStraigthForward(log,this,extraUncertainty,2*extraIntervals/3,false,max(distToPole,this.min),min(1.0,this.max))
            val biggerThanOne = approx.wrapperApproxLinearStraigthForward(log,this,1.0, extraIntervals/3,true,max(1.0,this.min),max(1.0,this.max),false)

            val result = definitionRange.ite(definitionFastGrowth.ite(biggerThanOne,fastGrow),this.builder.RealsNaN)

            return result
        }*/
/**
 * e-function as a piece-wise approximated AADD
 * @param extraUncertainty maximal over-approximation in the individual intervals. Default: 1
 * @return the AADD with the piecewise approximation from the 'this' AffineForm
 *   CURRENTLY a slight over-approximation which isn't fixed
 * TODO piecewise stuff
 */
/*
fun exp(extraUncertainty: Double = 0.75, extraIntervals: Int = -1): AADD {
    val exp = GeneralExponential()
    val approx = ApproximationScheme()

    return approx.wrapperApproxLinearStraigthForward(
        exp,
        this,
        extraUncertainty,
        limitIntervals = extraIntervals,
        false
    )
}*/
/*
        fun simplification():AffineForm {
            val cutleveldivisor = 1000
            var minExternal: Double? = null
            var possum = 0.0
            var negsum = 0.0
            val xicopy = HashMap(xi)
    /*        for ((key,value) in xicopy){
                if (key<10000000){
                    if (minExternal == null){
                        minExternal = value
                    }
                    else {
                        if (abs(value) < abs(minExternal)){
                            minExternal = value
                        }
                    }
                }
            } */
            if (minExternal == null || minExternal <= 1.0/10000 ){
                minExternal = 1.0
            }
            val cutlevel = minExternal/(cutleveldivisor)
            for ((key,value) in xicopy){
                if (key>=10000000 && abs(value) < abs(cutlevel) && value > 0){
                    possum += value
                    this.xi.remove(key)
                }
                else if (key>=10000000 && abs(value) <abs(cutlevel) && value < 0){
                    negsum += value
                    this.xi.remove(key)
                }
            }
            if (possum != 0.0) {
                possum += possum.ulp
                xi[builder.noiseVars.newGarbageVar()] = possum
            }
            if (negsum != 0.0) {
                negsum -= negsum.ulp
                xi[builder.noiseVars.newGarbageVar()] = negsum
            }
            return this
        }
    */

enum class EFunction{
    TIMES,EXP,SQRT,LOG,DIV,SIN, COS
}