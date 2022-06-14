## Exec Plan and interpretations in Real Life

A discussion on http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf as Scala code

A key thing to inspectable programs (example: An execution planner, a configuration DSL) is programs as descriptions. Wherever the description exist (intially, or finally) it is a known problem that descriptions shouild exist at some level, and it should hold informations as constraints - the problem this project tries to experiment with using the paper.


An excerpt from the paper:


"The key principle that underpins our idea is that implementation- specific constraints should be imposed at the point of use of a data type, not at the point of definition, i.e. it embodies the established principle that an interface should be separated from its implementation(s)."


The typelevel proofs **may** be readable only when readers try themselves. But it is mostly write once and forget.


