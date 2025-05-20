#pragma once
#ifndef AADDWRAPPER
#define AADDWRAPPER

#include "libnative_api.h"
#include <iostream>

/*
* Static Wrapper Build command for windows to utilize the dll (x64 Native Tools Command prompt tool from VS Compiler Suite)
* lib /def:libnative.def /out:libnative.lib
*/


class NumberRange {
public:
	NumberRange(libnative_kref_com_github_tukcps_aadd_values_NumberRange _numberRangeStruct, libnative_ExportedSymbols* _lib) {
		numberRangeStruct = _numberRangeStruct;
		lib = _lib;
	}

	~NumberRange() {
		lib->DisposeStablePointer(numberRangeStruct.pinned);
	}

	libnative_kref_com_github_tukcps_aadd_values_NumberRange getStruct() {
		return numberRangeStruct;
	}

private:
	libnative_kref_com_github_tukcps_aadd_values_NumberRange numberRangeStruct;
	libnative_ExportedSymbols* lib;
};


class BDD {

public:
	BDD(libnative_kref_com_github_tukcps_aadd_BDD _bddStruct, libnative_ExportedSymbols* _lib) {
		bddStruct = _bddStruct;
		lib = _lib;
	}

	/*
	~BDD() {
		lib->DisposeStablePointer(bddStruct.pinned);
	}*/

	/*
	 * Operator Overloads:
	 */

	BDD operator &&(BDD other) {
		return and_(other);
	}

	BDD operator ||(BDD other) {
		return or_(other);
	}

	BDD operator !() {
		return not_();
	}

	BDD and_(BDD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.and_(bddStruct, other.getStruct());
		return BDD(res,lib);
	}

	// TODO add xbool and__


	BDD evaluate() {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.evaluate(bddStruct);
		return BDD(res, lib);
	}

	BDD intersect(BDD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.intersect(bddStruct, other.getStruct());
		return BDD(res, lib);
	}
	/*
	AADD ite(AADD t, AADD e) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.ite(bddStruct, t.getStruct(), e.getStruct());
		return AADD(res, lib);
	}

	BDD ite(BDD t, BDD e) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.ite_(bddStruct, t.getStruct(), e.getStruct());
		return BDD(res, lib);
	}*/

	BDD nand(BDD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.nand(bddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD not_() {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.not_(bddStruct);
		return BDD(res, lib);
	}

	BDD or_(BDD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.or_(bddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD xor_(BDD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.BDD.xor_(bddStruct, other.getStruct());
		return BDD(res, lib);
	}

	bool satisfiable() {
		return lib->kotlin.root.com.github.tukcps.aadd.BDD.satisfiable(bddStruct);
	}

	int numFalse() {
		return lib->kotlin.root.com.github.tukcps.aadd.BDD.numFalse(bddStruct);
	}

	int numTrue() {
		return lib->kotlin.root.com.github.tukcps.aadd.BDD.numTrue(bddStruct);
	}

	const char* toIteString() {
		return lib->kotlin.root.com.github.tukcps.aadd.BDD.toIteString(bddStruct);
	}

	libnative_kref_com_github_tukcps_aadd_BDD getStruct() {
		return bddStruct;
	}

private:
	libnative_kref_com_github_tukcps_aadd_BDD bddStruct;
	libnative_ExportedSymbols* lib;
};

/* Wrapper for the AADD class */
class AADD {

public:


	AADD(libnative_kref_com_github_tukcps_aadd_AADD _aaddStruct, libnative_ExportedSymbols* _lib) {
		aaddStruct = _aaddStruct;
		lib = _lib;
	}

	/*
	 * Operator Overloads:
	 */

	// Addition Overloads:
	AADD operator + (const AADD& other) {
		return plus(other);
	}

	AADD operator +(const double& other) {
		return plus(other);
	}

	// Subtraction Overloads:
	AADD operator -(const AADD& other) {
		return minus(other);
	}

	AADD operator -(const double& other) {
		return minus(other);
	}

	// Times Overloads:
	AADD operator *(const AADD& other) {
		return times(other);
	}

	AADD operator *(const BDD& other) {
		return times(other);
	}

	AADD operator *(const double& other) {
		return times(other);
	}

	// Relationships Overloads:

	BDD operator <=(const double& other) {
		return lessThanOrEquals(other);
	}

	BDD operator <(const double& other) {
		return lessThan(other);
	}

	BDD operator >(const double& other) {
		return greaterThan(other);
	}

	BDD operator >=(const double& other) {
		return greaterThanOrEquals(other);
	}

	/*
	~AADD() {
		lib->DisposeStablePointer(aaddStruct.pinned);
	}*/

	libnative_kref_com_github_tukcps_aadd_AADD getStruct() {
		return aaddStruct;
	}

	AADD plus(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.plus(aaddStruct, other.getStruct());
		return AADD(res, lib);
	}

	int get_index() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_index(aaddStruct);
	}

	double get_max() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_max(aaddStruct);
	}

	bool get_maxIsInf() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_maxIsInf(aaddStruct);
	}

	bool get_maxIsNaN() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_maxIsNaN(aaddStruct);
	}

	double get_min() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_min(aaddStruct);
	}

	bool get_minIsInf() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_minIsInf(aaddStruct);
	}

	bool get_minIsNaN() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.get_minIsNaN(aaddStruct);
	}

	AADD ceil() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.ceil(aaddStruct);
		return AADD(res, lib);
	}

	long long ceilAsLong() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.ceilAsLong(aaddStruct);
	}

	AADD constrainTo(NumberRange other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.constrainTo(aaddStruct, other.getStruct());
		return AADD(res, lib);
	}

	bool contains(double value) {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.contains(aaddStruct, value);
	}

	AADD div(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.div(aaddStruct, other.getStruct());
		return AADD(res, lib);
	}

	AADD div(double other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.div__(aaddStruct, other);
		return AADD(res, lib);
	}

	AADD evaluate() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.evaluate(aaddStruct);
		return AADD(res, lib);
	}

	AADD exp() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.exp(aaddStruct);
		return AADD(res, lib);
	}

	BDD greaterThan(AADD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.greaterThan(aaddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD greaterThan(double other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.greaterThan__(aaddStruct, other);
		return BDD(res, lib);
	}

	BDD greaterThanOrEquals(AADD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.greaterThanOrEquals(aaddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD greaterThanOrEquals(double other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.greaterThanOrEquals__(aaddStruct, other);
		return BDD(res, lib);
	}

	AADD intersect(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.intersect(aaddStruct, other.getStruct());
		return AADD(res, lib);
	}

	AADD inv() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.inv(aaddStruct);
		return AADD(res, lib);
	}

	AADD invCeil() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.invCeil(aaddStruct);
		return AADD(res, lib);
	}

	AADD invFloor() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.invFloor(aaddStruct);
		return AADD(res, lib);
	}

	void join() {
		/*TODO*/
	}

	BDD lessThan(AADD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.lessThan(aaddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD lessThan(double other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.lessThan__(aaddStruct, other);
		return BDD(res, lib);
	}

	BDD lessThanOrEquals(AADD other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.lessThanOrEquals(aaddStruct, other.getStruct());
		return BDD(res, lib);
	}

	BDD lessThanOrEquals(double other) {
		libnative_kref_com_github_tukcps_aadd_BDD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.lessThanOrEquals__(aaddStruct, other);
		return BDD(res, lib);
	}

	AADD log() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.log(aaddStruct);
		return AADD(res, lib);
	}

	// TODO switch to new Form
	/*
	AADD log(double base) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.log_(aaddStruct, base);
		return AADD(res, lib);
	}*/

	AADD minus(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.minus(aaddStruct,other.getStruct());
		return AADD(res, lib);

	}

	AADD minus(double other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.minus__(aaddStruct,other);
		return AADD(res, lib);
	}

	AADD negate() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.negate(aaddStruct);
		return AADD(res, lib);
	}

	AADD floor() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.floor(aaddStruct);
		return AADD(res, lib);
	}

	AADD plus(double other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.plus__(aaddStruct,other);
		return AADD(res, lib);
	}

	AADD pow(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.pow(aaddStruct,other.getStruct());
		return AADD(res, lib);
	}

	AADD pow(double other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.pow___(aaddStruct,other);
		return AADD(res, lib);
	}

	AADD power(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.power(aaddStruct,other.getStruct());
		return AADD(res, lib);
	}

	AADD power2() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.power2(aaddStruct);
		return AADD(res, lib);
	}

	AADD sqrt() {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.sqrt(aaddStruct);
		return AADD(res, lib);
	}

	AADD times(AADD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.times(aaddStruct,other.getStruct());
		return AADD(res, lib);
	}

	AADD times(BDD other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.times_(aaddStruct,other.getStruct());
		return AADD(res, lib);
	}

	AADD times(double other) {
		libnative_kref_com_github_tukcps_aadd_AADD res = lib->kotlin.root.com.github.tukcps.aadd.AADD.times___(aaddStruct,other);
		return AADD(res, lib);
	}

	const char* toString() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.toString(aaddStruct);
	}

	const char* toIteString() {
		return lib->kotlin.root.com.github.tukcps.aadd.AADD.toIteString(aaddStruct);
	}

	void getRange() {
		lib->kotlin.root.com.github.tukcps.aadd.AADD.getRange(aaddStruct);
	}

private:
	libnative_kref_com_github_tukcps_aadd_AADD aaddStruct;
	libnative_ExportedSymbols* lib;
};



/* Wrapper for the DDBuilder Class */
class DDBuilder {

public:

	/* Constructors */

	DDBuilder(libnative_ExportedSymbols* _lib) {
		lib = _lib;
		// Builder Noise Vars Creation
		libnative_kref_com_github_tukcps_aadd_NoiseVariables noiseVars = lib->kotlin.root.com.github.tukcps.aadd.NoiseVariables.NoiseVariables();
		// Initialize our Builder Struct
		builderStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.DDBuilder__(noiseVars,false);
	}

	/* Destructors */

	~DDBuilder() {
		lib->DisposeStablePointer(builderStruct.pinned);
	}


	AADD range(double min, double max, int index) {
		libnative_kref_com_github_tukcps_aadd_AADD aaddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.range(builderStruct, min, max, index);
		return AADD(aaddStruct, lib);
	}

	AADD range(double min, double max, const char* id) {
		libnative_kref_com_github_tukcps_aadd_AADD aaddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.range_(builderStruct, min, max, id);
		return AADD(aaddStruct, lib);
	}

	AADD scalar(double value) {
		libnative_kref_com_github_tukcps_aadd_AADD aaddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.scalar(builderStruct, value);
		return AADD(aaddStruct, lib);
	}

	AADD assign(AADD old,AADD new_) {
		libnative_kref_com_github_tukcps_aadd_AADD aaddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.assign(builderStruct, old.getStruct(), new_.getStruct());
		return AADD(aaddStruct, lib);
	}

	BDD assign(BDD old, BDD new_) {
		libnative_kref_com_github_tukcps_aadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.assign_(builderStruct, old.getStruct(), new_.getStruct());
		return BDD(bddStruct, lib);
	}

	BDD IF(BDD cond) {
		libnative_kref_com_github_tukcps_aadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.IF(builderStruct, cond.getStruct());
		return BDD(bddStruct, lib);
	}

	BDD END() {
		libnative_kref_com_github_tukcps_aadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.END(builderStruct);
		return BDD(bddStruct, lib);
	}

	BDD ELSE() {
		libnative_kref_com_github_tukcps_aadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.ELSE(builderStruct);
		return BDD(bddStruct, lib);
	}

private:
	libnative_kref_com_github_tukcps_aadd_DDBuilder builderStruct;
	libnative_ExportedSymbols* lib;

};

#endif // !AADDWRAPPER