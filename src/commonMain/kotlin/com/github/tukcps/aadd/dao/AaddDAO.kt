package com.github.tukcps.aadd.dao
import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DD
import kotlinx.serialization.Serializable

/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class AaddDAO(
    val type: String,
    val index: Int? = DD.LEAF_INDEX,
    val value: AffineFormDAO? = null,
    val T: AaddDAO? = null,
    val F: AaddDAO? = null
)


fun AADD.toDAO(): AaddDAO = AaddDAO(
    type = if (this is AADD.Leaf) "AADD.Leaf" else "AADD.Internal",
    index = index,
    value = if (this is AADD.Leaf) value.toDAO() else null,
    T = if (this is AADD.Internal) T.toDAO() else null,
    F = if (this is AADD.Internal) F.toDAO() else null
)