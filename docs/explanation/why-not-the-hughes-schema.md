---
id: why-not-the-hughes-schema
title: "Why Not the Hughes Schema?"
description: "Why the Hughes encoding cannot apply two interpreters with different constraints to the same expression."
keywords:
  - "Hughes schema"
  - "constraint kinds"
  - "Typeable"
  - "Deconstraining"
---

The Hughes schema is the best-known way to put constraints on the nodes of a GADT: the data type takes a type parameter `p` that stands for the interpreter, and each node requires a `Typeable p a` instance. It does not allow a single expression to be used by a compiler with multiple, different constraints. This page shows why, using excerpts from the paper [Deconstraining](https://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf). The snippets are Haskell, shown here for explanation; they are not compiled.

## The Encoding

The paper defines a type class `Typeable` that every interpreter provides, and one instance per interpreter. The first interpreter, `SM`, needs the constraint `IntBool a`:

```haskell
class Typeable p a where
  valueP :: a -> p a

newtype SM a = SM { fromSM :: Int }

instance IntBool a => Typeable SM a where
  valueP = SM . toInt
```

The second interpreter, `Pretty`, needs the constraint `Show a`:

```haskell
newtype Pretty a = Pretty { fromPretty :: String }

instance Show a => Typeable Pretty a where
  valueP = Pretty . show
```

The expression type carries the interpreter `p` as a parameter:

```haskell
data Exp p a where
  ValueE :: Typeable p a => a -> Exp p a
  CondE  :: Exp p Bool -> Exp p a -> Exp p a -> Exp p a
  EqE    :: Eq a => Exp p a -> Exp p a -> Exp p Bool
```

Each interpreter works on its own instantiation of `Exp`:

```haskell
pretty    :: Exp Pretty a -> String  -- works
compileSM :: Exp SM a -> String      -- works
```

## The Problem

Suppose we want to apply both functions to the same expression:

```haskell
f :: Exp p a -> ...
f e = ...(compileSM e)...(pretty e)...
```

This is impossible. The parameter `p` is fixed when `e` is built, so `e` is either an `Exp SM a` or an `Exp Pretty a`, and it cannot be both. Each `ValueE` node holds the `Typeable p a` instance for exactly one `p`, so an expression built for one interpreter cannot be handed to the other.

## How ZIO Constraintless Differs

In ZIO Constraintless the expression does not mention the interpreter at all. Its type parameter is a [`TypeList`](../reference.md#typelist) of allowed types, and each interpreter supplies its own [`Instances`](../reference.md#instances) when it runs. The same `Expr[As, A]` value can therefore be passed to `pretty` and to `toInt`. For a working version of this, see the [tutorial](../tutorial.md).
