#ifndef SYMBOLICSYSTEMC
#define SYMBOLICSYSTEMC

#include <ostream>
#include <systemc>
#include <systemc-ams>
#include "libnative_api.h"

typedef libnative_kref_com_github_tukcps_jaadd_values_NumberRange range_t;
typedef libnative_kref_com_github_tukcps_jaadd_BDD BDD_t;
typedef libnative_kref_com_github_tukcps_jaadd_AADD AADD_t;
typedef libnative_kref_com_github_tukcps_jaadd_Conditions conditions_t;
typedef libnative_kref_com_github_tukcps_jaadd_NoiseVariables noiseVariables_t;
typedef libnative_kref_com_github_tukcps_jaadd_DDBuilder builder_t;

class nr_s {
public:
	nr_s(range_t _numberRangeStruct, libnative_ExportedSymbols* _lib) {
		numberRangeStruct = _numberRangeStruct;
		lib = _lib;
	}

	~nr_s() {
		lib->DisposeStablePointer(numberRangeStruct.pinned);
	}

	range_t getStruct() {
		return numberRangeStruct;
	}

private:
	range_t numberRangeStruct;
	libnative_ExportedSymbols* lib;
};

class bool_s {

public:

	bool_s(BDD_t _bddStruct, libnative_ExportedSymbols* _lib) : bddStruct(_bddStruct),lib(_lib)
	{}

	// Base BDD functions

	/*
	 * Operator Overloads:
	 */

	bool_s operator &&(bool_s other) {
		return and_(other);
	}

	bool_s operator ||(bool_s other) {
		return or_(other);
	}

	bool_s operator !() {
		return not_();
	}

	bool_s and_(bool_s other) {
		libnative_kref_com_github_tukcps_jaadd_BDD res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.and_(bddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s evaluate() {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.evaluate(bddStruct);
		return bool_s(res, lib);
	}

	bool_s intersect(bool_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.intersect(bddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s nand(bool_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.nand(bddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s not_() {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.not_(bddStruct);
		return bool_s(res, lib);
	}

	bool_s or_(bool_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.or_(bddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s xor_(bool_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.BDD.xor_(bddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool satisfiable() {
		return lib->kotlin.root.com.github.tukcps.jaadd.BDD.satisfiable(bddStruct);
	}

	int numFalse() {
		return lib->kotlin.root.com.github.tukcps.jaadd.BDD.numFalse(bddStruct);
	}

	int numTrue() {
		return lib->kotlin.root.com.github.tukcps.jaadd.BDD.numTrue(bddStruct);
	}

	const char* toIteString() {
		return lib->kotlin.root.com.github.tukcps.jaadd.BDD.toIteString(bddStruct);
	}

	libnative_kref_com_github_tukcps_jaadd_BDD getStruct() {
		return bddStruct;
	}

	/* SystemC and AMS required functions */

	bool operator ==(const bool_s& other) {
		return false;
	}

	bool_s& operator=(const bool_s& other) {
		bddStruct = other.bddStruct;
		return *this;
	}

	friend std::ostream& operator<<(std::ostream& os, bool_s& val);

protected:
	BDD_t bddStruct;
	libnative_ExportedSymbols* lib;

};

class double_s {

public:


	double_s() {
		lib = libnative_symbols();
		conditions_t conds = lib->kotlin.root.com.github.tukcps.jaadd.Conditions.Conditions__();
		// Builder Noise Vars Creation
		noiseVariables_t noiseVars = lib->kotlin.root.com.github.tukcps.jaadd.NoiseVariables.NoiseVariables_();
		// Initialize our Builder Struct
		builder_t builderStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.DDBuilder___(conds, noiseVars);
		aaddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.range_(builderStruct, 0.0, 0.0, "a");
	}


	double_s(AADD_t _aaddStruct, libnative_ExportedSymbols* _lib) : aaddStruct(_aaddStruct),lib(_lib)
	{}

	// Base AADD Functions
	/*
	 * Operator Overloads:
	 */

	 // Addition Overloads:
	double_s operator + (const double_s& other) {
		return plus(other);
	}

	double_s operator +(const double& other) {
		return plus(other);
	}

	// Subtraction Overloads:
	double_s operator -(const double_s& other) {
		return minus(other);
	}

	double_s operator -(const double& other) {
		return minus(other);
	}

	// Times Overloads:
	double_s operator *(const double_s& other) {
		return times(other);
	}

	double_s operator *(const bool_s& other) {
		return times(other);
	}

	double_s operator *(const double& other) {
		return times(other);
	}

	// Relationships Overloads:
	bool_s operator <=(const double_s& other) {
		return le(other);
	}

	bool_s operator <=(const double& other) {
		return lessThanOrEquals(other);
	}

	bool_s operator <(const double_s& other) {
		return lt(other);
	}

	bool_s operator <(const double& other) {
		return lessThan(other);
	}

	bool_s operator >(const double_s& other) {
		return gt(other);
	}

	bool_s operator >(const double& other) {
		return greaterThan(other);
	}

	bool_s operator >=(const double_s& other) {
		return ge(other);
	}

	bool_s operator >=(const double& other) {
		return greaterThanOrEquals(other);
	}

	/*
	~AADD() {
		lib->DisposeStablePointer(aaddStruct.pinned);
	}*/

	AADD_t getStruct()  {
		return aaddStruct;
	}

	double_s plus(double_s other)  {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.plus(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	int get_index()  {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_index(aaddStruct);
	}

	double get_max()  {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_max(aaddStruct);
	}

	bool get_maxIsInf()  {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_maxIsInf(aaddStruct);
	}

	bool get_maxIsNaN()  {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_maxIsNaN(aaddStruct);
	}

	double get_min()  {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_min(aaddStruct);
	}

	bool get_minIsInf() {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_minIsInf(aaddStruct);
	}

	bool get_minIsNaN() {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.get_minIsNaN(aaddStruct);
	}

	double_s ceil() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.ceil(aaddStruct);
		return double_s(res, lib);
	}

	long long ceilAsLong() {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.ceilAsLong(aaddStruct);
	}

	double_s constrainTo(nr_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.constrainTo(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	bool contains(double value) {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.contains(aaddStruct, value);
	}

	double_s div(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.div(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s div(double other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.div__(aaddStruct, other);
		return double_s(res, lib);
	}

	double_s evaluate() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.evaluate(aaddStruct);
		return double_s(res, lib);
	}

	double_s exp() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.exp(aaddStruct);
		return double_s(res, lib);
	}

	bool_s ge(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.ge(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s greaterThan(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.greaterThan(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s greaterThan(double other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.greaterThan__(aaddStruct, other);
		return bool_s(res, lib);
	}

	bool_s greaterThanOrEquals(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.greaterThanOrEquals(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s greaterThanOrEquals(double other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.greaterThanOrEquals__(aaddStruct, other);
		return bool_s(res, lib);
	}

	bool_s gt(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.gt(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	double_s intersect(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.intersect(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s inv() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.inv(aaddStruct);
		return double_s(res, lib);
	}

	double_s invCeil() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.invCeil(aaddStruct);
		return double_s(res, lib);
	}

	double_s invFloor() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.invFloor(aaddStruct);
		return double_s(res, lib);
	}

	void join() {
		/*TODO*/
	}

	bool_s le(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.le(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s lessThan(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.lessThan(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s lessThan(double other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.lessThan__(aaddStruct, other);
		return bool_s(res, lib);
	}

	bool_s lessThanOrEquals(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.lessThanOrEquals(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	bool_s lessThanOrEquals(double other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.lessThanOrEquals__(aaddStruct, other);
		return bool_s(res, lib);
	}

	double_s log() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.log(aaddStruct);
		return double_s(res, lib);
	}

	bool_s lt(double_s other) {
		BDD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.lt(aaddStruct, other.getStruct());
		return bool_s(res, lib);
	}

	double_s minus(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.minus(aaddStruct, other.getStruct());
		return double_s(res, lib);

	}

	double_s minus(double other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.minus__(aaddStruct, other);
		return double_s(res, lib);
	}

	double_s negate() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.negate(aaddStruct);
		return double_s(res, lib);
	}

	double_s floor() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.floor(aaddStruct);
		return double_s(res, lib);
	}

	double_s plus(double other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.plus__(aaddStruct, other);
		return double_s(res, lib);
	}

	double_s pow(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.pow(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s pow(double other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.pow___(aaddStruct, other);
		return double_s(res, lib);
	}

	double_s power(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.power(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s power2() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.power2(aaddStruct);
		return double_s(res, lib);
	}

	double_s sqrt() {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.sqrt(aaddStruct);
		return double_s(res, lib);
	}

	double_s times(double_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.times(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s times(bool_s other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.times_(aaddStruct, other.getStruct());
		return double_s(res, lib);
	}

	double_s times(double other) {
		AADD_t res = lib->kotlin.root.com.github.tukcps.jaadd.AADD.times___(aaddStruct, other);
		return double_s(res, lib);
	}

	const char* toString() {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.toString(aaddStruct);
	}
	// TODO rewrite so it converts the sus pointer to std::String
	const char* toIteString() const {
		return lib->kotlin.root.com.github.tukcps.jaadd.AADD.toIteString(aaddStruct);
	}

	void getRange() {
		lib->kotlin.root.com.github.tukcps.jaadd.AADD.getRange(aaddStruct);
	}

	/* SystemC and AMS required functions */

	bool operator ==(const double_s& other) {
		return false;
	}

	double_s& operator=(const double_s& other) {
		aaddStruct = other.aaddStruct;
		return *this;
	}

	friend std::ostream& operator<<(std::ostream& os,const double_s& val);

protected:
	AADD_t aaddStruct;
	libnative_ExportedSymbols* lib;
};

class context_s {

public:

	std::string ID;

	context_s(libnative_ExportedSymbols* _lib,std::string ID_) : lib(_lib),ID(ID_) {
		// Builder Conditions Creation
		libnative_kref_com_github_tukcps_jaadd_Conditions conds = lib->kotlin.root.com.github.tukcps.jaadd.Conditions.Conditions__();
		// Builder Noise Vars Creation
		libnative_kref_com_github_tukcps_jaadd_NoiseVariables noiseVars = lib->kotlin.root.com.github.tukcps.jaadd.NoiseVariables.NoiseVariables_();
		// Initialize our Builder Struct
		builderStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.DDBuilder___(conds, noiseVars);
	}
	
	~context_s() {
		lib->DisposeStablePointer(builderStruct.pinned);
	}

	double_s range(double min, double max, int index) {
		AADD_t aaddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.range(builderStruct, min, max, index);
		return double_s(aaddStruct, lib);
	}

	double_s range(double min, double max, const char* id) {
		AADD_t aaddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.range_(builderStruct, min, max, id);
		return double_s(aaddStruct, lib);
	}

	double_s scalar(double value) {
		AADD_t aaddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.scalar(builderStruct, value);
		return double_s(aaddStruct, lib);
	}

	double_s assign(double_s old, double_s new_) {
		AADD_t aaddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.assign(builderStruct, old.getStruct(), new_.getStruct());
		return double_s(aaddStruct, lib);
	}

	bool_s assign(bool_s old, bool_s new_) {
		libnative_kref_com_github_tukcps_jaadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.assign_(builderStruct, old.getStruct(), new_.getStruct());
		return bool_s(bddStruct, lib);
	}

	bool_s IF(bool_s cond) {
		BDD_t condstruct = cond.getStruct();
		libnative_kref_com_github_tukcps_jaadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.IF(builderStruct, condstruct);
		return bool_s(bddStruct, lib);
	}

	bool_s END() {
		libnative_kref_com_github_tukcps_jaadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.END(builderStruct);
		return bool_s(bddStruct, lib);
	}

	bool_s ELSE() {
		libnative_kref_com_github_tukcps_jaadd_BDD bddStruct = lib->kotlin.root.com.github.tukcps.jaadd.DDBuilder.ELSE(builderStruct);
		return bool_s(bddStruct, lib);
	}

protected:
	libnative_kref_com_github_tukcps_jaadd_DDBuilder builderStruct;
	libnative_ExportedSymbols* lib;
	
};

// Required Global overloads

// overloads for double_s

inline std::ostream& operator <<(std::ostream& os,const double_s& val) {
	const char* s = val.toIteString();
	os << s << std::endl;
	val.lib->DisposeString(s);
	return os;
}

inline void sc_trace(sc_trace_file*& f, double_s& val, std::string name) {
	sc_trace(f, val.toString(), name);
}

// overloads for bool_s

inline std::ostream& operator<<(std::ostream& os, bool_s& val) {
	const char* s = val.toIteString();
	os << s << std::endl;
	val.lib->DisposeString(s);
	return os;
}

inline void sc_trace(sc_trace_file*& f, bool_s& val, std::string name) {
	sc_trace(f, val.toIteString(), name);
}



// Macros for code readability:

/*
#define IF(X,Y) Y.IF(X);
#define ELSE(Y) Y.ELSE();
#define END(Y) Y.END();
*/
// TODO IF ELIF() MACRO


#endif // !SYMBOLICSYSTEMC
