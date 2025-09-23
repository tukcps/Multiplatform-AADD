package io.github.tukcps.aadd.dao

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilderSettings
import kotlinx.serialization.Serializable


/**
 * A simple POJO class that can be handled directly for exchange of AADD.
 */
@Serializable
data class DDContextDAO (
   val conditions: ConditionsDAO? = null,
   //val noiseVars: NoiseVariables = NoiseVariables(),
   val settings: DDBuilderSettings = DDBuilderSettings()
)

fun DDBuilder.toDAO() = DDContextDAO(
   conditions = conds.toDAO(),
   // noiseVars = noiseVars,
)