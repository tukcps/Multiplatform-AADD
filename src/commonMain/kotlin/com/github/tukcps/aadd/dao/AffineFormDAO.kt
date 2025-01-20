package com.github.tukcps.aadd.dao

import com.github.tukcps.aadd.values.AffineForm
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class AffineFormDAO (
    val min: Double,
    val max: Double,
    var central: Double,
    var r: Double,
    val xi: HashMap<Int, Double>
) {
    fun toJson(): String = json.encodeToString(this)
}

fun AffineForm.toDAO() = AffineFormDAO(
    min = min,
    max = max,
    central = central,
    r = r,
    xi = xi
)