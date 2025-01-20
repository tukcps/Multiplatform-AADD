package com.github.tukcps.aadd.dao

import com.github.tukcps.aadd.Conditions
import kotlinx.serialization.Serializable

/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class ConditionsDAO (
    val topIndex: Int = 0,
    val btmIndex: Int = 0,
    val indexes: HashMap<String, Int> = hashMapOf(),
    val x: List<Pair<Int, ValueDAO>> = listOf()
)

fun Conditions.toDAO() = ConditionsDAO(
    topIndex = topIndex,
    btmIndex = btmIndex,
    indexes = indexes,
    x = x.map { Pair(it.key, it.value.toValueDAO()) }
)
