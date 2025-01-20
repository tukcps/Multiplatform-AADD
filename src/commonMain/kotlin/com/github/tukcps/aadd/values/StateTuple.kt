package com.github.tukcps.aadd.values

import com.github.tukcps.aadd.CDDVariableError
import com.github.tukcps.aadd.DDBuilder

/**
 * A class that represents the Cartesian product of a set of AADDs and BDDs for a single path through the set.
 * An AADD represents a set of potential value ranges depending on internal decisions taken.
 * A BDDA represents a set of potential boolean values depending on internal decisions taken.
 *
 * */
class StateTuple(
    var builder: DDBuilder,

    /**
     * A hashmap that saves all the continuous variables that are represented in this tuple
     * The key is a string that is the id of the specific variable
     * The value is an affine form that represents the value range of this specific variable in this context
     * */
    private val continuousMap: MutableMap<String,AffineForm> = hashMapOf(),

    /**
     *  A hashmap that saves all the discrete variables that are represented in this tuple
     * The key is a string that is the id of the specific variable
     * The value is a bool that represents for this specific discrete variable its value in this specific context
     * */
    private val discreteMap: MutableMap<String,XBool> = hashMapOf()
)
{

    /**
     * Add a new continuous variable to the state tuple
     * @param id: The id of the variable whos value range is represented
     * @param value: The specific value range of the variable represented by an affine form
     * */
    fun addContinuousVar(id: String, value: AffineForm)
    {
        if (continuousMap.contains(id)) throw CDDVariableError("Variable already exists")
        else continuousMap[id] = value
    }

    /**
     * Add a new discrete variable to the state tuple
     * @param id: The id of the new discrete variable whos value is represented
     * @param value: The specific boolean value of this variable in this context
     * */
    fun addDiscreteVar(id: String, value: XBool)
    {
        if(discreteMap.contains(id)) throw CDDVariableError("Variable already exists")
        else discreteMap[id] = value
    }

    /**
     * Getter for the value of a specific continuous variable
     * @param id: The id of the variable
     * @return: The Affine form representing the value range of the specified variable in the given context
     * */
    fun getContinuousValue(id: String): AffineForm? {return continuousMap[id]}

    /**
     * Simple getter that returns a copy of the continuous variables map
     * */
    fun getContinuousValues():MutableMap<String,AffineForm>
    {
        return continuousMap.toMutableMap()
    }

    /**
     * Simple getter that returns a copy of the discrete variables map
     * */
    fun getDiscreteValues():MutableMap<String,XBool>
    {
        return discreteMap.toMutableMap()
    }

    /**
     * Getter for the value of a specific discrete variable
     * @param id: The id of the discrete variable
     * @return: The boolean value representing the value of the specified variable in the given context
     * */
    fun getDiscreteValue(id: String): XBool? {return discreteMap[id]}

    /**
     * Simple function that calls the hash maps that contain the continuous and discrete variables and returns true if the given
     * id of the variable is contained in either one of them
     * @param id: The id of the variable
     * @return
     * */
    fun exists(id:String) : Boolean
    {
        return continuousMap.contains(id)||discreteMap.contains(id)
    }

    /**
     * Returns a new object of type StateTuple that is a copy of this
     * @return
     * */
    fun clone(): StateTuple
    {
        return StateTuple(this.builder, continuousMap.toMutableMap(),discreteMap.toMutableMap())
    }

    /**
     * Simple getter for the number of saved continuous variables
     * @return number of saved continuous variables
     * */
    fun getNumSavedContinuousVariables() : Int { return continuousMap.size }

    /**
     * Simple getter for the number of saved discrete variables
     * @return number of saved discrete variables
     * */
    fun getNumSavedDiscreteVariables() : Int { return discreteMap.size }

    /**
     * Function that updates the value of an existing continuous variable in the continuousMap. If the given ID
     * doesn't exist it throws a CDDVariableError
     * @param id : The id of the continuous variable to be updated, must be equal to the id used in the continuousMap
     * @param value : The new value it should be updated to
     * */
    fun updateContinuousVariable(id:String,value:AffineForm)
    {
        if(exists(id)) continuousMap[id] = value
        else throw CDDVariableError("Update of continuous variable $id failed. Does not exist")
    }

    /**
     * Function that updates the value of an existing discrete variable in the discreteMap. If the given ID
     * doesn't exist it throws a CDDVariableError
     * @param id : The id of the discrete variable to be updated, must be equal to the id used in the discreteMap
     * @param value : The new value it should be updated to
     * */
    fun updateDiscreteVariable(id:String,value: XBool)
    {
        if(exists(id)) discreteMap[id] = value
        else throw CDDVariableError("Update of discrete variable $id failed. Does not exist")
    }

    /**
     * Function that takes the state tuple and creates a string representation of the following form:
     * [af_1,af_2,...,af_n,d_1,d_2,..,d_m]
     * Starting with the continuous variables represented by their affine form af_i followed by the discrete variables
     * with their boolean form d_j
     * [] chosen to differentiate better between the if else structure where this form will be embedded
     * */
    override fun toString() : String
    {
        var stringRepresentation = "["

        for(contVar in continuousMap)
        {
            stringRepresentation += "${contVar.toString()},"
        }

        for(discVar in discreteMap)
        {
            stringRepresentation += "${discVar.toString()},"
        }
        stringRepresentation = stringRepresentation.dropLast(1)
        stringRepresentation += "]"

        return stringRepresentation
    }

    /**
     * Returns all the affine forms in a symbolic representation
     * */
    fun toSymbolicString(): String {
        var cstr = ""
        for (variable in continuousMap) {
            cstr += ",${variable.key}=${variable.value.toSymbolicString()}"
        }
        cstr.removePrefix(",")
        cstr = "($cstr"
        var dstr = ""
        for (variable in discreteMap) {
            dstr += ",${variable.key}=${variable.value}"
        }
        return "$cstr$dstr)"
    }

}