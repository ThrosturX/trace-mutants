#!/bin/bash

source setclasspath.sh

NAME=jbus

rm -rfv $NAME
java mujava.cli.testnew $NAME JBus
mkdir $NAME/classes/lib/
cp lib/*.jar $NAME/classes/lib
java mujava.cli.genmutes -ALL $NAME
