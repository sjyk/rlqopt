#!/bin/bash
set -x

JOB_PATH="/Users/zongheng/workspace/join-order-benchmark"

mvn compile

mvn -q exec:java \
    -Dexec.mainClass=edu.berkeley.riselab.rlqopt.relalg.VisualizeJoinGraph \
    -Dexec.args="$(find ${JOB_PATH} -name '[0-9]*a.sql' | tr '\n' ' ')"
