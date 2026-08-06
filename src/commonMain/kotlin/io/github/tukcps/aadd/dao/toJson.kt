package io.github.tukcps.aadd.dao

import kotlinx.serialization.json.Json

var json = Json {
    prettyPrint = true
    allowSpecialFloatingPointValues = true
    explicitNulls = false
}

fun AaddDAO.toJson() = json.encodeToString(value = this)
fun BddDAO.toJson() = json.encodeToString(value = this)
