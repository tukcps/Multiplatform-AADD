package io.github.tukcps.aadd.dao

import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.DoubleBoundMath.toDouble
import kotlinx.serialization.Serializable


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class AffineFormDAO (
    val min: Double,
    val max: Double,
    var central: Double,
    val xi: HashMap<Long, Double>
) {
    fun toJson(): String = json.encodeToString(this)
}

fun AffineForm.toDAO() = AffineFormDAO(
    min = min.toDouble(),
    max = max.toDouble(),
    central = central,
    xi = xi
)