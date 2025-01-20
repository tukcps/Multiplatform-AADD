package com.github.tukcps.aadd.dao

import com.github.tukcps.aadd.StrDD
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class StrDdDAO(
    val type: String,
    val index: Int?,
    val value: String?,
    val T: StrDdDAO?,
    val F: StrDdDAO?
) {
    fun toJson() = Json.encodeToString(value = this)
}

fun StrDD.toDTO(): StrDdDAO = StrDdDAO(
    type = if (this is StrDD.Leaf) "StrDD.Leaf" else "StrDD.Internal",
    index = index,
    value = if (this is StrDD.Leaf) value else null,
    T = if (this is StrDD.Internal) T.toDTO() else null,
    F = if (this is StrDD.Internal) F.toDTO() else null
)