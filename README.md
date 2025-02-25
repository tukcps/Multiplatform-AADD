# AADD

##### Symbolic Computation with Affine Arithmetic Decision Diagrams (AADDs)

(c) University Kaiserslautern-Landau, Chair of Cyber-Physical Systems


![Logo of AADD](doc/AADDLogo.png)

## What are AADDs?  
AADDs permit semi-symbolic computations on Ranges, Integers, and its interactions with control flow via predicates.
AADDs are a combination of reduced, ordered BDD that model discrete conditions and Affine Forms that model computations on real numbers.
Both interact via predicates that are structured in the form of a BDD.
The overview and control of the interactions between discrete and continuous variables might be useful in many problems, 
e.g. the development of  

- Methods for symbolic execution of software that has computations on Double or Integer numbers,
- Methods for runtime-verification, 
- Methods for verification of neural networks, 
- Constraint propagation or SMT solvers where discrete and continuous solutions are tightly entangled.

To learn more:

- Grimm et al., EPTCS 247, 2017, pp. 1-17; https://doi.org/10.4204/EPTCS.247.1
- Zivkovic et al., IEEE TCAD 38/10 2019;  https://ieeexplore.ieee.org/document/8428606
- Zivkovic et al., DATE 2019; http://dx.doi.org/10.23919/DATE.2019.8715278


> For purely Boolean problems, optimized BDD packages or a SAT solver are likely better suited.

## Contents and Use of the AADD Library
This repository contains the *multi-platform* AADD library.
It implements *Affine Arithmetic Decision Diagrams* (AADD) for various platforms, including
binary shared libraries (for use from C/C++, ...), and the Java Virtual Machine platform.

The development environment is:
- Kotlin v2.1+ which compiles to Java 19+ Byte code or various binary platforms, 
- Kotlin test for unit testing, 
- Gradle 7.8+ as build tool. 

The Gradle build tool automatically downloads all dependencies.
The multi platform version includes a simple LP solver (which is ok as most LP problems in AADD are small ones, where the overhead for starting a complex solver is expensive).
For the JVM platforms, other solvers for LP/MILP problems like OjAlgo will be used in the next updates.

To include the AADD library in an application, add the following dependency to your Gradle dependencies: 
```
    implementation("io.github.tukcps:aadd:0.1.8") 
```
Gradle will get and use the respective version (i.e., 0.1.8), and you just can use it in your code.

To quickly try some pre-existing examples, it is suggested to use IntelliJ IDEA (https://www.jetbrains.com/idea/), and to import the Gradle project.
Navigate to "src/test," and right-click on a benchmark, or example to run it. 


## Semi-Symbolic Computations with AADD

The AADD library allows users execute code in a semi-symbolic way.
For this purpose, it provides representations of variables and constants of the types

- Real 
- Integer 
- String 
- Boolean 

Variables and constants are created a factory and builder class instance ```DDBuilder```. 
This object maintains all information on dependencies and interactions. 
Representations. 
To create variables/constants, the builder must be instantiated. 
Then, its methods can be used to create new variables and constants, e.g., in Kotlin: 
```
   import com.github.tukcps.aadd.*

   fun main() {
      val bulder = DDBuilder() 	
      val x = builder.real(-1.0 .. 1.0, "x")
      val f = builder.ite(x greaterEquals 0.0, x-100.0, x+100.0)
      println(" f = $f")
      // f = ITE(1, [-100,00; -99,00], [99,00; 100,00])
   }
}
```
For other platforms, the respective functions must be called in the respective language, e.g., Java, or C++. 
Note that a DDBuilder has a single abstract method as parameter. 
This allows users to pass lambda parameters that are executed, e.g., for configuration or direct computations.
To do computations, AADD provides a vast set of arithmetic and boolean functions.
For Kotlin, also overloaded operators are provided. 
Below an example: 
```
   DDBuilder {
      val a = real(-1.0 .. 1.0, "a")
      val y = a - a + 1.0
   }
```
The results of the computations can be accessed via the field ```.value``` resp. ```.min``` and ```.max``` as 
the respective type (Double, Long integer, Bool, or String). 
Furthermore, the method `.toString` returns a suitable string. 
````
      println("a = $a")    //    -1.0 .. 1.0
      println("y = $y")    //    1.0 
````

### Configuration parameters of DDBuilder 

Computations on affine forms lead to internal errors due to rounding and approximation of the results of nonlinear operations.
To not distort the actual result interval, these errors need to be stored in the affine form.

The implementation offers two different ways of storing internal errors: 
Either in one single error term with interval semantics (r) or in additional noise symbols (xi).
While the first option offers shorter computation times, the second option reduces exponential error growth in long iterative computations.

All default configurations can be found in the configuration file of DDBuilder (jAADDConfig.json):
```
   "noiseSymbolsFlag": false,
   "originalFormsFlag": false,
   "maxSymbols": 200,
   "mergeSymbols": 10
```
By default, ```noiseSymbolsFlag``` is set to false, which means that all errors are stored in a single error term.
If it is switched to true, every internal error is stored in an individual noise symbol and approximation errors are mapped to the operation they resulted from. 
This may allow error cancellation in further computations, but also increases the number of the affine form's noise symbols and thus the computation time.

Therefore, the number of noise symbols per affine form is limited by the reduceNoiseSymbols function. 
Whenever the number of noise symbols in an affine form exceeds "maxSymbols," the function replaces exactly 
"mergeSymbols" many of them by a new noise variable that has the value of the replaced ones.
A detailed description of the reduceNoiseSymbols function can be found in section 2.2 of the User Guide.

The ```originalFormsFlag``` enables an additional mapping for times and inverse operations for the detection of linear dependencies between approximation errors that resulted from operations on scalar dependent affine forms. 
Thus, enabling the originalFormsFlag without the noiseSymbolsFlag has no effect.

According to the use case, the configuration of a DDBuilder's instance can be adapted, 
as shown in the following example:
```
DDBUilder{
   config.noiseSymbolsFlag = true
   config.originalFormsFlag = true
   config.maxSymbols = 100
   config.mergeSymbols = 5   
}
```

### AADD and BDD combined and DSL

Imagine the following program: 
```
    var a = local.range(-1.0, 1.0)
    if (a > 0.0) 
        a=a+10.0 
    else 
        a = a-10.0
    println("a = "+a)
```
We can symbolically execute it by using IF, ELSE, END and assignS.
With the help of the class DDBuilder that provides DSL features, we can write
```
    DDBuilder {
         var a = range(-1.0, 1.0)
         IF(a greaterOrEquals 0.0) 
             a=a.assignS(a+10.0) 
         ELSE()
             a=a.assignS(a-10.0)
         END ()
         println("a = "+a)
    }
```
More complete documentation is in the folder doc. 

### Multiplatform Utilization
The `jAADD` library has been converted into a Multiplatform Project, enabling the generation of a shared C library. This library can then be used within C++/C code projects. Here's how the workflow operates:

1. **Build the Project**: 
   - Use Gradle to build the project as usual. This process will generate a shared library:
     - `libnative.dylib` on macOS
     - `libnative.so` on Linux
     - `libnative.lib` on Windows
   - The library will be located in `build/bin/native/<debugShared, releaseShared>`.

2. **Linking the Library**: 
   - Alongside the shared library, a header file is available in the same directory. Use this to link against your C/C++ projects.

Here's an example of how to use the library in C++:

```cpp
#include <iostream>
#include "libnative.h"

int main() {
    libnative_ExportedSymbols* lib = libnative_symbols();
    libnative_kref_com_github_tukcps_aadd_DDBuilder builder = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.DDBuilder();
    libnative_KDouble x1_min = 0.0;
    libnative_KDouble x1_max = 1.0;
    const char* x1_name = "x1";
    libnative_kref_com_github_tukcps_aadd_AADD x1 = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.range_(builder, x1_min, x1_max, x1_name);
    
    std::cout << "x1 = [" << lib->kotlin.root.com.github.tukcps.aadd.AADD.get_min(x1) 
              << ", " << lib->kotlin.root.com.github.tukcps.aadd.AADD.get_max(x1) << " ]" << std::endl;

    return 0;
}
```
For more detailed information on utilizing multiplatform shared libraries, please refer to [the official Kotlin documentation.](https://kotlinlang.org/docs/native-dynamic-libraries.html)

### API Changelog

##### Versions MP-AADD 0.1+
- In DDBuilder: range and scalar replaced by real, integer, string.

##### Versions MP-AADD 0.0.1-0.0.9
- Transition of the project to a multi-platform project 
- refactor jaadd to aadd in several classes/package names to match new project name that is AADD only (no j for Java)

##### Version 3.8+ (jAADD)
- last jaadd versions; added new builder functions from MP-AADD to make transition easier

##### Version 3.0
- Common interface ```NumberRange``` for all classes that model ranges.
- ```BDD``` leaves hold ```XBool``` values, not ```Boolean``` values.
- Sealed classes and interfaces for ```DDref```, ```BDD```, ```AADD```, ```IDD```, ```StrDD``` allow complete modeling without nullable references.
- IA can continue computation in case of Infinite/NaN bounds and open intervals

##### Version 2.8, 2.9
tests and operations on IDD and StrDD classes. 

##### Version 2.7pure
just the AADD classes, without constraint-net, parser, ui, etc.

##### Version 2.6
AADDLeaf, AADD become sealed class AADD with AADD.Leaf and AADD.Internal

##### Version 2.5
Simplified language of parser, everything via def: statement. 
Property part of parser and symbol table

##### Version 2.4
Replaced Context by DDBuilder that includes DSL elements; combines builder and factory patterns. 

##### Version 2.2, 2.3
None. 
Only interface declarations were added which allows delegation and nicer use of AADD Context. 

##### Version 2.1 
The version 2.1 introduces different factories (Context) which permits different independent instances of AADD/BDD. 

##### Version 2.0 (Kotlin JVM)
Version 2.0 comes with some modifications in the API
to permit interoperability with Kotlin, or to clean up the API.
The following are the changes:

1. Use of getter/setter methods and adapted names following Java naming conventions for all fields. The following fields are concerned:

    * getMin() and getMax() replace the fields min and max fields of Range and AADD that are private now.
    * getValue() replaces Value() as getter for the field value in BDD and AADD.
    * getResult() replaces in the expression parser the field result that is private now. 
    * several methods, e.g., range(...), scalar(...) now start with a small letter as usual in Java.

2. Renaming of arithmetic functions to their respective operator names.
For example:
    * x.sum(y) has become x.plus(y)
    * x.mul(y) has become x.times(y)

3. The method names in the factories for BDD and AADD have been renamed. 
    * BDD.constant(boolean) replaces BDD.newLeaf(Boolean)
    * BDD.variable("X") replaces BDD.Bool("X").

4. AADDMgr has been split into static fields or methods of the respective classes AADD or BDD.
Only the AADD and BDD streams remain in the class AADDstreams.


##### Version 1.0-1.3 (Java version)
Initial proof-of-concept written in Java
