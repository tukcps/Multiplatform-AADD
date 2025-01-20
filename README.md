# AADD

##### Symbolic Computation with Affine Arithmetic Decision Diagrams (AADDs)

(c) University Kaiserslautern-Landau, Chair of Cyber-Physical Systems


![Logo of AADD](doc/AADDLogo.png)

AADDs permit semi-symbolic computations on Ranges, Integers, and its interactions with control flow via predicates.
AADDs are a combination of reduced, ordered BDD that model discrete conditions and Affine Forms that model computations on real numbers.
Both interact via predicates that are structured in the form of a BDD.
Note that for purely Boolean problems, a SAT solver might be better suited.

This repository contains the *multi-platform* AADD library.
It implements *Affine Arithmetic Decision Diagrams* (AADD) for various platforms, including
binary shared libraries (for use from C/C++, ...), and the Java Virtual Machine platform.

The development environment is:
- Kotlin v2.1+ ; Java 19+ Byte code or various binary platforms
- Gradle 7.8+ as build tool.  
- Kotlin test for unit testing 

The Gradle build tool automatically downloads all dependencies; unlike in
previous versions, we use a simple LP solver.

For the very urgent that work with a Java, Kotlin or other Jar platform: 

1) Add the following repository to your Gradle repositories: 
```
repositories {
    //AADD
    maven("https://cpsgit.informatik.uni-kl.de/api/v4/projects/152/packages/maven") {
        name = "GitLab"
        credentials(HttpHeaderCredentials::class) {
            name = "Repository-Read-Token"
            value = "glpat-QgQBx35Gmo6jYfmMEPSP"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}
```
2) add the following dependency to your Gradle dependencies: 
```
        implementation("com.github.tukcps:aadd:0.1.8")
```
Gradle will get and use the respective version (i.e., 0.1.8), and you just can use it in your code.

To quickly try some pre-existing examples, it is suggested to use IntelliJ IDEA (https://www.jetbrains.com/idea/), and to import the Gradle project.
Navigate to "src/test," and right-click on a benchmark, or example to run it. 

### Semi-Symbolic Computations with AADD

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
With the conversion of the jAADD library into a Multiplatform Project, the option exists to generate a shared C library. This shared C library can then be utilized in C++/C code projects.
The work flow is as follows. First build the project as usual. The build process, gradle, will generate a shared library libnative.<.dylib ,.so ,.lib > under build/bin/native/<debugShared, releaseShared>.
Together with the header file inside of the same dirs the lib can be linked against in C/C++ projects. An example usage can be found below.
```
    libnative_ExportedSymbols* lib = libnative_symbols();
    libnative_kref_com_github_tukcps_aadd_DDBuilder builder = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.DDBuilder();
    libnative_KDouble x1_min = 0.0;
    libnative_KDouble x1_max = 1.0;
    const char* x1_name = "x1";
    libnative_kref_com_github_tukcps_aadd_AADD x1 = lib->kotlin.root.com.github.tukcps.aadd.DDBuilder.range_(builder,x1_min,x1_max,x1_name);
    std::cout << "x1 =[ " << lib->kotlin.root.com.github.tukcps.aadd.AADD.get_min(x1) << ", " << lib->kotlin.root.com.github.tukcps.aadd.AADD.get_max(x1) << " ]";
```
For more information on how to use a multiplatform shared library please visit the official kotlin page.

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
