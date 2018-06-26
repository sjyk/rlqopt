#!/bin/bash
set -x

JOB_PATH="/Users/zongheng/workspace/join-order-benchmark"

for f in $(find ${JOB_PATH} -name '[0-9]*a.dot'); do
    echo $f
    dot -Kdot -Tpng ${f} >${f%.dot}.png
done
