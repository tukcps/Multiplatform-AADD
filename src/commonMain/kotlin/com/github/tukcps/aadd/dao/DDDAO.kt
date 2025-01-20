package com.github.tukcps.aadd.dao


import com.github.tukcps.aadd.DD
import kotlinx.serialization.Serializable


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class DDDAO(
    val type: String,
    val index: Int? = DD.LEAF_INDEX,
    val value: ValueDAO? = null,
    val T: AaddDAO? = null,
    val F: AaddDAO? = null
)

@Serializable
data class ValueDAO(
    val type: String,
    val affineForm: AffineFormDAO? = null,
    val string: String? = null,
    val xBool: String? = null
)

fun DD<*>.toValueDAO(): ValueDAO = ValueDAO (
    type = "AffineForm"
)