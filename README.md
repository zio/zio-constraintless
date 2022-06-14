## Exec Plan and interpretations in Real Life


**A Scala take on below Haskell paper, on parametrising the program with logical constraints at every node, without compromising modularity**

http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf

An excerpt from the paper:


_"The key principle that underpins our idea is that implementation- specific constraints should be imposed at the point of use of a data type, not at the point of definition, i.e. it embodies the established principle that an interface should be separated from its implementation(s)."_


## Context

The key to many inspectable programs such as an execution planner, a configuration DSL etc is the basic concept of "programs as descriptions", and this is already widely known and almost everyone lives with it. 

Wherever the description exist (Initially or finally) it is guaranteed that these descriptions (introspectable programs) exist at some point in the code (at some level). Furthermore, this data can easily turn out to be a Generalised ADTs (rather than a simple one) that are recursive. The implication is compiler has to traverse through the unknown types (unknown yet existing ==> existential). 

The obvious implication of having to handle "unknown" is, the DSL should hold on to informations as constraints (that are relevant to implementation) on types at the definition site, unless we compromise on parametric polymorphism. This naive approach is not a good idea and it imposes modularity issues. The reasonsing and solution is given in the above paper, and this project solves the exact problem in scala.


## Why not the scheme of Hughes?

It doesn't allow you to have a compiler with multiple constraints.

With Hughe's scheme we have a Typeable interface where all expression nodes return `F[A]` where `F[_]` has an instance of `Typeable`. Note htat this `F[_]` needs to be different if the constraints are different. This means if a compiler needs multiple constraints, typechecking becomes almost impossible.


```scala
class Typeable p a valueP :: a → p a
```

