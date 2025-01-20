package com.github.tukcps.aadd.dao

import com.github.tukcps.aadd.IDD
import kotlinx.serialization.Serializable


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class IddDAO(
    val type: String,
    val index: Int?,
    val value: String?,
    val T: IddDAO?,
    val F: IddDAO?
)


fun IDD.toDTO(): IddDAO = IddDAO(
    type = if (this is IDD.Leaf) "IDD.Leaf" else "IDD.Internal",
    index = index,
    value = if (this is IDD.Leaf) value.toString() else null,
    T = if (this is IDD.Internal) T.toDTO() else null,
    F = if (this is IDD.Internal) F.toDTO() else null
)