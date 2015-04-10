#!/bin/bash

echo "Note: If 'nothing happens', that means the original (non-mutant) is failing. (It can also mean python2 is not in your path)."

set -e 
python2 src/mutant_test.py --original

echo "Original seems to have passed with flying colors! Please be patient while each mutant is tested..."

python2 src/mutant_test.py JBus.java sbt

echo "Done!"
