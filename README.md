## Exec Plan and interpretations in Real Life

A discussion on http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf as Scala code

A key  to inspectable programs (example: An execution planner, a configuration DSL) is the basic concept of "programs as descriptions", and this is already widely known and almost everyone lives with it. Wherever the description exist (intially, or finally) it is a known problem that descriptions shouild exist at some level. However, this data should hold on to informations as constraints with types unless compromising parametric polymorphism. However encoding specific constraint isn't a scalable code due to modularity issues (Example: Oh! I need to print the value in compiler but I cannot print the credentials involved). Parametrising the program with constraints without compromising the modularity is the the "only" problem this project tries to experiment with using the above mentined paper that is in Haskell.


An excerpt from the paper:


"The key principle that underpins our idea is that implementation- specific constraints should be imposed at the point of use of a data type, not at the point of definition, i.e. it embodies the established principle that an interface should be separated from its implementation(s)."


The typelevel proofs **may** be readable only when readers try themselves. But it is mostly write once and forget.


