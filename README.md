## Exec Plan and interpretations in Real Life

A discussion on http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf as Scala code

**Parametrising the program with constraints without compromising the modularity is the the "only" problem this project tries to experiment with using the above mentined paper that is in Haskell.**


An excerpt from the paper:


_"The key principle that underpins our idea is that implementation- specific constraints should be imposed at the point of use of a data type, not at the point of definition, i.e. it embodies the established principle that an interface should be separated from its implementation(s)."_


The typelevel proofs **may** be readable only when readers try themselves. But it is mostly write once and forget.



## Context

The key to many inspectable programs such as an execution planner, a configuration DSL etc is the basic concept of "programs as descriptions", and this is already widely known and almost everyone lives with it. 

Wherever the description exist (Initially or final) it is a known problem that descriptions shouild exist at some point in the code (at some level). However, this data should hold on to informations as constraints with types unless compromising parametric polymorphism, which is used by compilers. 

The naive solution is parametrising your program(s) with specific constraint every now and then, but that isn't a scalable code due to modularity issues, which further result in the need for regression for every new capability added. 


