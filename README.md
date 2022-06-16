## ConstraintLess

| Release | Scaladex | Twitter |
| --- | --- | --- | 
| [![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] |[![Badge-Scaladex-page]][Link-Scaladex-page] |[![Badge-Twitter]][Link-Twitter] |


Allows you to build programs as mere descriptions with maximum polymorphism, maximum modularity, zero abstraction leakage, and zero usnafe/safe casting.

It is **a Scala take on the following paper in Haskell, on parametrising the program with logical constraints at every node, without compromising modularity**

http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf

An excerpt from the paper:


_"The key principle that underpins our idea is that implementation- specific constraints should be imposed at the point of use of a data type, not at the point of definition, i.e. it embodies the established principle that an interface should be separated from its implementation(s)."_


## Context

The key to many inspectable programs such as an execution planner, a configuration DSL etc is the basic concept of "programs as descriptions", but this idea comes with limitations.

This description (or data) can easily turn out to be a Generalised ADTs (rather than a simple ADTs) that can also be recursive, such that compiler has to traverse through the unknown types (existential) and for the compiler to do any advanced/useful stuff with it, it needs to know more about these types.

The obvious implication of having to handle "unknown" is that, the data should hold on to informations as constraints (that are relevant to implementation) on types at the definition site. A possible solution is to compromise on parametric polymorphism, or fall back to relying unsafe/safe (relative) casting (asInstanceOf).

This naive approach imposes modularity issues, and possible runtime crashes. The reasonsing and solution is given in the above paper, and this project solves the exact problem in scala.


## Why not the scheme of Hughes?

It doesn't allow you to have a compiler with multiple constraints.


A few excerpts from the paper on why it doesn't work:

```scala
class Typeable p a valueP :: a → p a
```

```scala
newtype SM a = SM {fromSM :: Int}

instance IntBool a ⇒ Typeable SM a where
  valueP = SM · toInt
```

```scala

newtype Pretty a = Pretty {fromPretty :: String}

instance Show a ⇒ Typeable Pretty a where valueP = Pretty · show

```

```scala
data Exp p a where
ValueE::Typeable p a ⇒ a → Exp p a

CondE ::Expp Bool→Exp p a → Exp p a → Exp p a 

EqE :: Eq a ⇒ Exp p a → Exp p a → Exp p Bool
```


```scala
pretty :: Exp Pretty a → String // works
compileSM :: Exp SM a → String // works


```

 However, now suppose that we wish to apply the two functions to the same expression, as in:

```scala
f :: Exp p a → . . .
f e = ...(compileSM e)...(pretty e)..
```

and that's impossible

[Badge-Scaladex-page]: https://index.scala-lang.org/afsalthaj/constraintless/latest.svg "Scaladex"
[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/io.github.afsalthaj/constraintless_2.12.svg "Sonatype Releases"
[Badge-Twitter]: https://img.shields.io/twitter/follow/zioscala.svg?style=plastic&label=follow&logo=twitter

[Link-Scaladex-page]: https://index.scala-lang.org/zio/zio-config/zio-config "Scaladex"
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/io/github/afsalthaj/constraintless_2.12/ "Sonatype Releases"
[Link-Twitter]: https://twitter.com/afsalt2
