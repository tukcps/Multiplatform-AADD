package com.github.tukcps.aadd.dao

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
var json = Json {
    prettyPrint = true
    allowSpecialFloatingPointValues = true
    explicitNulls = false
}

fun AaddDAO.toJson() = json.encodeToString(value = this)
fun BddDAO.toJson() = json.encodeToString(value = this)
