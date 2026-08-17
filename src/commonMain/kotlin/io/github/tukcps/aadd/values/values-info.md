## Values

The `values` package contains the mathematical value representations used by the AADD's leaves.
Values are not simple numbers. 
They represent **convex sets of possible values**. 
Operations are defined according to set semantics.
The main goal is safe constraint propagation: 
- all possible solutions must be preserved, and
- over-approximations are allowed. 

> Note that operations on values can result in splitting values into non-convex sets. 

### Design principle

The central principle of AADD values is:

> Values represent sets of possible solutions, and every operation must
> preserve all valid solutions.

Precision may be reduced by the chosen representation, but correctness must always be maintained.

### Set semantics

A value represents a set in an underlying mathematical domain.
For example, ```IntegerRange(1,5)``` represents ```{1,2,3,4,5}``` and ```RealRange(0.0,1.0)```
represents ```{x ∈ ℝ | 0 ≤ x ≤ 1}```. 

Operations on values therefore operate on sets.
For a mathematical function `f`, the _exact_ result is the image set:

```
    f(S) = { f(x) | x ∈ S and f(x) is defined }
```

A value representation may not be able to represent this set exactly.
Therefore, the _computed_ result is a safe over-approximation:

```
    R = f(S) ∪ Δ  
```
where
 - Δ ⊇ ∅ contains the additional values introduced by the chosen representation, or
 - Δ → ∅ is the objective of optimization methods (like LP-solving in AADD) or programming in values. 

### Constraint propagation semantics

AADD introduces in particular semantics that are suitable for **constraint propagation**.
The purpose of an operation is hence not to signal numerical errors, but to preserve all possible solutions.
Undefined values are therefore handled by restricting the valid input set.
An `Empty` set is produced only if no valid result exists.
Examples:

```
sqrt([-1,4]) = [0,2]
```

because the valid input subset is:

```
[0,4]
```

giving:

```
[0,2]
```

whereas:

```
sqrt([-4,-1]) = Empty
```

because no value has a real square root.

### Predefined Sets and Values

#### Empty (Set)
`Empty` ( ∅ ) represents cases like 
- for values, no valuation gives a valid solution, and set of possible solutions is ∅, or 
- for DDs, a contradiction in the path condition makes them not reachable (infeasible ∅)

It is not equivalent to:
- IEEE NaN and its traps, 
- undefined intermediate values, or 
- numerical overflow traps. 

It is simply a logical statement that the current constraints have no solution.

#### All (Set)

`All` simply means that, due to absence of knowledge, all values of a domain are in the set of possible 
results. This is for Bool the values { True, False }, for Reals the set of all Reals, and for Integers likewise.  

#### Zero and One (Element of Set)

`Zero` and `One` are specific elements of `All`. 
They can be the only ones, e.g. for `Bool`, or they are often special ones as they often act as neutral elements 
for some operations like addition or multiplication. 

### Bounds

Bounds represent single elements of an extended number line that are bounds of a range. 
Possible bounds are:
 - -∞
 - finite values
 - +∞
 - NaN

Note that `NaN` is used only where no mathematical value exists, 
and only internally for Bounds as Bounds are single values, not sets -- and hence cannot be empty. 
Remember, for constraint propagation, most operations prefer a conservative enclosure instead of producing `NaN`.

### Ranges

`NumberRange` is the common abstraction for ranges over ordered domains.
A range represents a subset `[min, max]` with `min ≤ max`
for non-empty sets. The invariant is: `min > max => Empty`
No special storage representation is required for the empty set.

## Convex representations

`IntegerRange` and `RealRange` represent **convex subsets** of their domains.

Example -- the set:

```
{1,2,3,4}
```
can be represented exactly by `[1,4]`, but `{1,3}`
requires the IDD class that represents it as `1 or 3`.
The smallest convex enclosure by a range is:
```
[1,3]
```
Note that the range-representation is conservative:
> Every possible value is included, but additional values may be introduced.

### Domains

The supported domains are mathematical sets (that are represented by suitable approximations).
Examples:

```
Integers.All = ℤ
Reals.All = ℝ
Booleans.All = {false,true}
```

A value can test whether it represents a complete domain.
Examples:

```kotlin
IntegerRange.Integers.isAll()
RealRange.Reals.isAll()
```

This is a set equality test:`value == complete domain` not merely a type check.

### Set operations

All value representations support set operations.

#### Containment

```
A contains x
```
means:
```
x ∈ A
```

and:

```
A contains B
```
means:
```
B ⊆ A
```

#### Intersection

```
A intersect B
```

returns:

```
A ∩ B
```

#### Join

```
A join B
```
returns the smallest supported set containing:
```
A ∪ B
```
For convex representations this is the convex hull.
Example:

```
[1,2] join [5,6]
```

results in: ```[1,6]``` whereas IDD would result in ```[1,2] or [5,6]```

### (Extended) Boolean results of comparisons

Comparisons do not return ordinary Booleans.
A comparison over sets may have several possible outcomes.
Example:

```
[1,5] > [3,7]
```

is neither always true nor always false. The result is represented by `XBool`:

```
Empty   (no possible evaluation, or contradiction)
One/True
Zero/False
All   (both True and Falls is possible; also: unknown, X)
```

This follows the same set semantics:

```
possible truth values of the formula
```


### Values versus (value)DD

The mathematical semantics are independent of the internal representation.
For example, consider the integers ℤ:

```
Subset of ℤ
├── IntegerRange
│   convex subsets
│
└── IDD (IntegerDecisionDiagram)
    arbitrary subsets
```

Both represent sets of integers.
The difference is only the precision of the representation.
Note, that the operations:

```
contains
intersect
join
union
compare
```

remain valid for all representations.
Details on AADD, IDD, BDD are explained in the documentation.