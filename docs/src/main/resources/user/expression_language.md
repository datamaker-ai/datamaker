---
layout: default
title: Expression Language
parent: User
---

# Expression Language

The Spring Expression Language (“SpEL” for short) is a powerful expression language that supports querying and manipulating an object graph at runtime. The language syntax is similar to Unified EL but offers additional features, most notably method invocation and basic string templating functionality.

[Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions)

The expression language supports the following functionality:

- Literal expressions 
- Boolean and relational operators 
- Regular expressions 
- Class expressions 
- Accessing properties, arrays, lists, and maps 
- Method invocation 
- Relational operators 
- Assignment 
- Calling constructors 
- Bean references 
- Array construction 
- Inline lists 
- Inline maps 
- Ternary operator 
- Variables 
- User-defined functions 
- Collection projection 
- Collection selection 
- Templated expressions

## Available variables and fields

- **dataset** `(Dataset)`
  - dateCreated `(Date)`
  - dateModified `(Date)`
  - description `(String)`
  - externalId `(UUID)`
  - id `(Long)`
  - locale `(Locale)`
  - name `(String)`
  - tags `(Set<String>)`
- **dataJob** `(GenerateDataJob)`
  - config `(HashMap)`
  - dateCreated `(Date)`
  - dateModified `(Date)`
  - description `(String)`
  - externalId `(UUID)`
  - id `(Long)`
  - locale `(Locale)`
  - generator `(DataGenerator)`
  - dataType
    - name `(String)`
  - name `(String)`
  - schedule `(String)`
  - sinkNames `(Set<String>)`
  - tags `(Set<String>)`
- **jobExecution**
  - cancelTime `(Date)`
  - dataJob `(GenerateDataJob)`
  - endTime `(Date)`
  - errors `(List<String>)`
  - externalId `(UUID)`
  - id `(Long)`
  - numberOfRecords
  - results `(List<String>)`
  - startTime `(Date)`
  - state `(String)`

## Examples

`#dataset.name + "-" + T(java.lang.System).currentTimeMillis() + "." + #dataJob.generator.dataType.name().toLowerCase()`