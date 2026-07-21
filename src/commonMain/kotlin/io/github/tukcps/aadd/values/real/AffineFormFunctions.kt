package io.github.tukcps.aadd.values.real

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.util.minusUlp
import io.github.tukcps.aadd.util.plusMinusUlp
import io.github.tukcps.aadd.util.plusUlp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin


/**
 * Absolute value define with abs(x)=if(x<0) return -x
 * Taking the widest subinterval if 0.0 is contained and restraining the min max values
 */
fun AffineForm.abs(): AffineForm{
    // in that cause the interval can only be reduced and the longest overlapping of correlation be used
    if(contains(0.0)){
        val highValue= max(max, -min)
        val neg = this * -1.0
        val res = if (central<0.0)
            AffineForm.buildAF(builder, 0.0 .. highValue, neg.central, neg.r, neg.xi)
        else
            AffineForm.buildAF(builder, 0.0 .. highValue, central, r, xi)

        return res
    } else {
        return if (min>0) this
        else this * -1.0
    }
}


/**
 * logarithm of a number using a specified base
 */
fun AffineForm.log(base : Double): AffineForm {
    when {
        isEmpty() -> return builder.AFEmpty
        isReals() -> return builder.AFReals
        // isScalar() -> return AffineForm(builder, ln(min).plusMinusUlp())
        min <= 0.0 && max.isFinite()
            -> return AffineForm(builder, Double.NEGATIVE_INFINITY.. (ln(max / ln(base)).plusUlp()))
    }
    val l = (ln(min) / ln(base)).minusUlp()
    val u = (ln(max) / ln(base)).plusUlp()
    // It prevents however the division by 0 or near-0.
    // use min range approximation for small intervals
    val alpha = if ((max-min) < 0.00001 || builder.scheme==DDBuilder.ApproximationScheme.MinRange){
        1/(max*ln(base))
    } else {(u - l) / (max - min)}
    val touchingPoint = 1 / (alpha *ln(base))
    // val ys = (touchingPoint - min) * alpha + l
    val logxs = ln(touchingPoint)/ln(base)
    val delta = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
        (ln(min*max)-min/max-1)/2
    else(logxs + l-alpha*(min+touchingPoint)) / 2
    val noise = if ((max-min) < 0.00001|| builder.scheme==DDBuilder.ApproximationScheme.MinRange)
        (abs(ln(max/min)+min/max-1)/2).plusUlp()
    else
        (abs(logxs - l - alpha * (touchingPoint - min)  ) / 2).plusUlp()
    val af =  affine(alpha, delta, noise)
    af.min = l
    af.max = u
    //pythonVisualApprox(alpha,delta,noise,"np.log(x)/np.log($base)")
    return af
}


/**
 * Computation of sin on the interval on this
 * If the range does not include an extrema the best linear function close to
 * 9 equal distant sample on sin is computed with the least square method and scaled with the affine method
 */
fun AffineForm.sin():AffineForm{
    when {
        isEmpty()  -> return builder.AFEmpty
        isReals()  -> return AffineForm(builder, -1.0 .. 1.0)
        isScalar() -> return AffineForm(builder, sin(central).plusMinusUlp())
    }
    // ruling out some interval width that makes linear approximation uncorrelated to the actual direvation
    if (max - min > PI - 0.2)
        return AffineForm.buildAF(builder, RealRange(-1.0..1.0), 0.0, 1.0, HashMap())
    val periodeK = floor(max / (2 * PI))
    // is the interval on the monotone rising part of sin (considered the periodicty ) or on the monotone falling part?
    if (min - periodeK * 2 * PI > -PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < PI / 2 +10.0.pow(-6) || min - periodeK * 2 * PI > PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < 3 * PI / 2 + 10.0.pow(-6) ) {
        val numSamples = 9
        //val f0 = DoubleArray(numSamples) { _ -> 1.0 }
        val f1 = DoubleArray(numSamples) { i -> min + i * (max - min) / (numSamples - 1) }
        var sumF1 = 0.0
        var squareSumF1 = 0.0
        //val f = arrayOf(f0, f1)
        val y = DoubleArray(numSamples) { i -> sin(f1[i]) }
        var sumY = 0.0
        var sumXY = 0.0
        for (i in f1.indices) {
            // F.transpose*F
            sumF1 += f1[i]
            squareSumF1 += f1[i] * f1[i]
            //F.transpose*y
            sumY += y[i]
            sumXY += f1[i] * y[i]
        }

        val FtxF0 = DoubleArray(2)
        val FtxF1 = DoubleArray(2)
        val FtxF = arrayOf(FtxF0, FtxF1)
        FtxF[0][0] = numSamples * 1.0
        FtxF[0][1] = sumF1
        FtxF[1][0] = sumF1
        FtxF[1][1] = squareSumF1
        val det = 1 / (FtxF[0][0] * FtxF[1][1] - FtxF[0][1] * FtxF[1][0])
        val inverse0 = DoubleArray(2)
        val inverse1 = DoubleArray(2)
        val inverse = arrayOf(inverse0, inverse1)
        inverse[0][0] = det * FtxF[1][1]
        inverse[0][1] = -det * FtxF[0][1]
        inverse[1][0] = -det * FtxF[1][0]
        inverse[1][1] = det * FtxF[0][0]
        // should be the approximation of alpha*x+delta
        val delta = inverse[0][0] * sumY + inverse[0][1] * sumXY
        val alpha = inverse[1][0] * sumY + inverse[1][1] * sumXY

        val caseLessPIHalv = min - periodeK * 2 * PI > -PI / 2-10.0.pow(-6)  && max - periodeK * 2 * PI < PI / 2 +10.0.pow(-6)
        val maxValue = if(caseLessPIHalv)
            if(max>PI/2.0) PI/2.0
            else max
        else if (min<PI/2.0) PI/2.0
        else min
        val minValue = if(caseLessPIHalv)
            if(min<-PI/2.0) -PI/2.0
            else min
        else if(max>3*PI/2.0) 3*PI/2.0
        else max
        val noiseMax = max(0.0, (sin(maxValue) - (alpha * maxValue + delta)))
        val noiseMin = max(0.0, ((alpha * minValue + delta) - sin(minValue)))
        val noise = max(noiseMax, noiseMin)
        //println("max: " + maxValue+" "+ max + " sin(max): " + sin(maxValue) + " approx(max): " + (alpha * maxValue + delta) + "+ noise: " + noise)
        //println("min: " + minValue+" "+ min + " sin(min): " + sin(minValue) + " approx(min): " + (alpha * minValue + delta) + "- noise: " + noise)
        val res = affine(alpha, delta, noise)

        return AffineForm.buildAF(builder,
            RealRange(min(sin(min), sin(max)), max(sin(min), sin(max))), res.central, res.r, res.xi)
    } else
        return AffineForm.buildAF(builder, RealRange(-1.0..1.0), 0.0, 1.0, HashMap())

}

fun AffineForm.cos(): AffineForm {
    return this.plus(PI/2.0).sin()
}

/**
 * Strictly monotonous growing, -1 .. 1 -> -PI/2 .. +PI/2
 */
fun AffineForm.arcsin(): AffineForm {
    when {
        isEmpty() -> return builder.AFEmpty
        isReals() -> return AffineForm(builder, asin(-1.0).minusUlp() .. asin(1.0).plusUlp())
        isScalar() -> return AffineForm(builder, asin(central).plusMinusUlp())
    }
    val lb = max(min, -1.0)
    val ub = min(max, 1.0)

    // val f = RealRange(asin(min), asin(max))
    /** Use the first four terms of the Taylor series of arcsin to approximate the function*/
    val alpha1 = 1 + central.pow(2.0).times(1.0/6.0) + central.pow(4.0).times(3.0/40.0) + central.pow(6.0).times(15.0/336.0)
    val alpha2 = 1 + lb.pow(2.0).times(1.0/6.0) + lb.pow(4.0).times(3.0/40.0) + lb.pow(6.0).times(15.0/336.0)
    val alpha3 = 1 + ub.pow(2.0).times(1.0/6.0) + ub.pow(4.0).times(3.0/40.0) + ub.pow(6.0).times(15.0/336.0)

    /** Choose the largest alpha to avoid under-approximation*/
    val alpha = max(max(alpha1,alpha2),alpha3)

    val noiseMax = max(0.0, (asin(ub)- ub * alpha))
    val noiseMin = max(0.0, (min * alpha - asin(min)))
    val noise = max(noiseMax, noiseMin)

    val res = affine(alpha, 0.0, noise)

    return AffineForm.buildAF(builder, RealRange(min(asin(lb), asin(ub)), max(asin(lb), asin(ub))), res.central, res.r, res.xi)
}

/**
 * arccos implementation; strictly monotonous falling, -1..1 -> 0 .. PI
 */
fun AffineForm.arccos(): AffineForm = -(this.arcsin().minus(PI/2.0)) /* arccos(x) = PI/2 - arcsin(x) */

/**
 * Comparison of affine forms:
 *  - We compare the range of this and other.
 *  - If this is for sure larger that the other, we return 1,
 *  - If the other is for sure larger that this, we return -1,
 *  - else we return 0.
 *
 *  Note that this comparison is uncertain for the result 0 as a a more
 *  accurate result might turn 0 to -1 or 1 by solving constraint systems.
 *  @param other, the affine form with which we compare this
 *  @return 1 if this > other, -1 if this < other, 0 else.
 */
operator fun AffineForm.compareTo(other: AffineForm) = when {
    (this.min > other.max)  -> 1
    (other.min < this.max)  -> -1
    else  -> 0
}

fun ceil(input : AffineForm) : AffineForm = input.ceil()
fun floor(input : AffineForm) : AffineForm = input.floor()
fun log(base : Double, arg : AffineForm) : AffineForm = arg.log(base)
fun pow(base : AffineForm, exp : Double) : AffineForm = base.pow(exp)
fun pow(base : AffineForm, exp : AffineForm) : AffineForm = base.pow(exp)
