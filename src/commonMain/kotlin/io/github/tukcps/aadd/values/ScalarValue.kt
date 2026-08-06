package io.github.tukcps.aadd.values

sealed interface ScalarValue

sealed interface NumericValue : ScalarValue
sealed interface BooleanValue : ScalarValue

interface RealValue : NumericValue
interface IntegerValue : NumericValue
interface XBoolValue: BooleanValue
interface StringValue : ScalarValue