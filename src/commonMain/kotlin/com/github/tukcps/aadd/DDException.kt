package com.github.tukcps.aadd

/**
 * An exception due to inappropriate input,
 * e.g. incompatible types.
 */
open class DDException(s: String): Exception(s)
class DDTypeCastError: DDException("Attempt to use incompatible DD types.")

/**
 * An internal error that is due to a bug;
 * e.g. a situation is detected that should normally never occur.
 */
class DDInternalError constructor(msg: String): Exception(msg)


class AADDAssertError(s:String):DDException(s)

/**
 * Exception type that gets thrown whenever some issue exists
 * with a variable inside the state tuples
 * */
class CDDVariableError(s:String):DDException(s)