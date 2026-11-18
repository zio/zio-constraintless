---
id: index
title: "Understanding ZIO Constraintless"
sidebar_label: "Concepts"
description: "Why programs as descriptions need type constraints at the point of use, and how ZIO Constraintless provides them."
keywords:
  - "programs as descriptions"
  - "generalised algebraic data types"
  - "type constraints"
---

This page explains the problem ZIO Constraintless solves and the idea behind its solution. It does not teach the API; for that, see [Getting Started](../getting-started.md) and the [Reference](../reference.md).

## Context

The key to many inspectable programs, such as an execution planner or a configuration DSL, is the idea of "programs as descriptions". Instead of running a computation directly, we build a data structure that describes it and run an interpreter over that data later.

A description like this often turns out to be a generalised algebraic data type (GADT) that is recursive. When an interpreter pattern matches on a node, the compiler knows very little about the types inside it: they are existential. To do anything useful with a value of such a type, such as printing it, comparing it or converting it, the compiler needs more information about the type.

## The Naive Solutions

There are three ways to give an interpreter that information without this library, and each has a cost.

The first is to store the constraints in the data at the definition site, for example a `Show[A]` next to every value. The description is then tied to one interpreter: a second interpreter that needs `ToInt[A]` cannot be written without changing every node, and every constraint any interpreter might ever need ends up in the definition.

The second is to duplicate the whole data type once per interpreter. The third is to give up on parametric polymorphism and fall back to `asInstanceOf`. That moves the safety of the program from the compiler to the author of the interpreter, and a mistake becomes a runtime crash.

## The Idea

The paper behind this library puts it in one sentence: constraints belong at the point of use of a data type, not at the point of definition. In ZIO Constraintless the definition of the data type records only which types are allowed in it, using a [`TypeList`](../reference.md#typelist). Each node carries [`IsElementOf`](../reference.md#iselementof) evidence that its type is one of them.

An interpreter then asks for [`Instances`](../reference.md#instances) of its own type class for the whole list. Because every node's evidence says "my type is in the list", the interpreter can look up the instance for that node's type with no cast at the call site. The following diagram contrasts the definition site with the point of use:

```
              definition site                         point of use
        ┌──────────────────────────┐          ┌─────────────────────────────┐
        │ Expr[As <: TypeList, A]  │          │ pretty(e)(Instances[Show, As])│
        │   node: A IsElementOf As │ ───────▶ │ toInt(e)(Instances[ToInt, As])│
        └──────────────────────────┘          └─────────────────────────────┘
          knows only "which types"              brings its own type class
```

The data type is defined once. Adding a new interpreter means adding a new function with a new `Instances` requirement, and the compiler checks that every allowed type has the instances that interpreter needs.

## Why There Is One Cast Inside the Library

`Instances#withInstance` is implemented with a single `asInstanceOf`, inside the library. The membership evidence is what makes it sound: a proof that `B` is the head of the list means the head instance is a `TypeClass[B]`. Users of the library never write that cast. For the details, see [`Instances`](../reference.md#instances).

## Further Reading

The encoding the paper replaces, and why it cannot apply two interpreters to one expression, is explained in [Why Not the Hughes Schema?](./why-not-the-hughes-schema.md). The paper itself and other links are collected on the [Resources](../resources.md) page.
