# `trace-mutants`

This repository contains three packages, `trace-extract`, `gen-mutants` and `evaluate-mutants`.
The `trace-extract` package concerns extracting traces from a state space and generating test cases.
`gen-mutants` should include everything needed to generate `JBus.java` mutants from muJava.
`evaluate-mutants` is a test-bed to evaluate whether or not mutants are being killed, given the supplied traces.

## `trace-extract`

The `trace-extract` package provides two features:

* Extracting traces from `.aut` graphs
* Generating test cases for `JBus.java` (and its mutants) from traces

Extracting traces is done through the `Main.java` class and generating test cases is done with the `TestGenerator.java` class.

`Main.java` expects the first parameter to be a path to an `.aut` file to create a graph from.
The second parameter is optional, defaults to 50 and represents the number of traces to extract from the graph.
The resulting traces are printed out in a `.traces` file of the same name as the input.

`TestGenerator.java` accepts a `.traces` file as a first parameter.
The second and third parameters are optional and represent spec name and batch size.
If no spec or batch size parameters are supplied, they default to `"spec"` and 50.
The batch size limits the amount of test cases per spec file, 
so if there are 1000 traces and batch size is 100, then 10 spec files are created.

## `gen-mutants`

The `gen-mutants` package contains the necessary files to generate mutants for `JBus.java`. 
Simply run `run.sh`. It should generate at least 300 mutants and zip their sources into `mutants.tar`.
`setclasspath.source` may need to be modified to include `tools.jar` (unless `tools.jar` is already in the `CLASSPATH`).

## `evaluate-mutants`

The `evaluate-mutants` package is a simple framework for evaluating mutants.
It contains a `python 2` script that uses `sbt` and the filesystem to test each mutant and manage things.
Dependencies: `sbt` (simple build tool), `java`, `python 2.7`.
See `run.sh` and `src/mutant_test.py [--help]` for more information.
