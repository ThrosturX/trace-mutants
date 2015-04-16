#!/bin/bash

source setclasspath.source

NAME=jbus
SRCS=(
	JBus
)

rm -rfv $NAME
java mujava.cli.testnew $NAME JBus
mkdir -p $NAME/classes/lib/
cp lib/*.jar $NAME/classes/lib
java mujava.cli.genmutes -ALL $NAME

mkdir mutants


for src in "${SRCS[@]}"; do
	cp $NAME/result/$src/traditional_mutants/*/*/$src.java mutants --parents
	cp $NAME/result/$src/traditional_mutants/mutation_log mutants
done

tar -cf mutants.tar mutants 
rm -rf mutants
