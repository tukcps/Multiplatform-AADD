package com.github.tukcps.aadd.util

import com.github.tukcps.aadd.values.Range
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.ulp

/**
 * Not nice, but for MP
 */
fun Double.toRoundedString(numOfDec: Int): String {
    val intDigits = this.toLong()
    val floatDigits = ((this - intDigits) * 10f.pow(numOfDec)).roundToInt()
    return "${intDigits}.${floatDigits}"
}

/**
 * Converts a Double value into a Range that includes the value
 * @return Range with this+/-1 ulp
 */
fun Double.plusMinusUlp() = Range(this.minusUlp() ..this.plusUlp())
fun Double.plusUlp() = this + this.ulp
fun Double.minusUlp() =
    if (this.isInfinite())  this // Infinite - something would be NaN !
    else this - this.ulp

/** Function converting a String of a us represented real number
 * TODO: ISO conformity check
 * */
fun parseUSNumberString(strNum : String) : Double
{
    /** Only decimal positions */
    if(!strNum.contains("."))
    {
        val decSplit = strNum.split(",")
        var num = ""
        for(decBlock in decSplit) num+=decBlock
        return num.toDouble()
    }
    /** Case decimal and fractions */
    else
    {
        val fracDecSplit = strNum.split('.')
        if(fracDecSplit.size!=2) throw Error("Wrong Number Format Error")
        val decSplit = fracDecSplit[0].split(",")
        var num = ""
        for(decBlock in decSplit) num+=decBlock
        num+="."
        num+=fracDecSplit[1]
        return num.toDouble()
    }
}

/** Function converting a String of a us,eu or internal long number to a long datatype
 * TODO: Check for ISO conformity
 * */
fun parseUSLongString(strNum : String) : Long
{
    // Should be US format  e.g.: 123,321,123
    if(strNum.contains(","))
    {
        val splitIntoExpTriples = strNum.split(",")
        var internalString = ""
        for(expTriple in splitIntoExpTriples) internalString += expTriple
        return internalString.toLong()
    }
    // Should be EU format  e.g. : 123.321.123
    else if(strNum.contains("."))
    {
        val splitIntoExpTriples = strNum.split(".")
        var internalString = ""
        for(expTriple in splitIntoExpTriples) internalString += expTriple
        return internalString.toLong()
    }
    // Should be internal format  e.g. : 123321123
    else
    {
        return strNum.toLong()
    }
}