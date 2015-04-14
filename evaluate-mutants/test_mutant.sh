#!/bin/bash

if (($# < 1)); then echo "Error: Must supply base path!"; exit 1; fi

outdir=$1/classes

# improve the classpath
LIB="$(pwd)/lib"
jcp="$LIB/akka-actor.jar:$LIB/scala-library.jar" 
scp="$LIB/scala-library.jar:$LIB/akka-testkit.jar:$LIB/akka-actor.jar:$LIB/scalatest.jar:$1/src/main/java:$outdir"

mkdir -p $outdir

# scala paths
sdir="scala/bin"
jdir="java"

# compile the mutant
$jdir/javac -cp "$jcp" "$1/src/main/java/JBus.java" -d $outdir || exit 2

shopt -s failglob
for filename in $1/src/test/scala/*.scala; do
	# compile the test sources
	$sdir/scalac -classpath "$scp" "$filename" -d $outdir || exit 3
	# run the tests
	specname=${filename%.*}
	specname0=${specname##*/}
	cspec=${specname0^}
	$sdir/scala -cp "$scp" org.scalatest.run "$cspec" || exit 1
done

# success!
exit 0
