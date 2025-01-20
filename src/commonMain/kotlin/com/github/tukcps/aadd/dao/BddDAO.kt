package com.github.tukcps.aadd.dao

import com.github.tukcps.aadd.BDD
import kotlinx.serialization.Serializable


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class BddDAO(
    val type: String,
    val index: Int?,
    val value: String?,
    val T: BddDAO?,
    val F: BddDAO?
)

fun BDD.toDTO(): BddDAO = BddDAO(
    type = if (this is BDD.Leaf) "BDD.Leaf" else "BDD.Internal",
    index = index,
    value = if (this is BDD.Leaf) value.toString() else null,
    T = if (this is BDD.Internal) T.toDTO() else null,
    F = if (this is BDD.Internal) F.toDTO() else null
)