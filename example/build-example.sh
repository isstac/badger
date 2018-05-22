#!/bin/bash
# chmod +x build-example.sh
# ./build-example.sh

trap "exit" INT

#kelinciPath="/Users/yannic/repositories/cmu/kelinciwca/instrumentor/build/libs/kelinci.jar"

echo "Create KelinciWCA folder and copy files"
mkdir -p ./kelinciwca_analysis/src
cp ../badger/src/examples/InsertionSortFuzz.java ./kelinciwca_analysis/src
mkdir -p ./kelinciwca_analysis/bin
cp ../badger/build/examples/InsertionSortFuzz.class ./kelinciwca_analysis/bin

echo "Create SymExe folder and copy files"
mkdir -p ./symexe_analysis/src
cp ../badger/src/examples/InsertionSortSym.java ./symexe_analysis/src
mkdir -p ./symexe_analysis/bin
cp ../badger/build/examples/InsertionSortSym.class ./symexe_analysis/bin

#echo "Instrument Kelinci code"
#cd ./kelinciwca_analysis
#java -jar $kelinciPath -i ./bin/ -o ./bin-instr -skipmain

echo "Done."
