#!/bin/bash
set -ex
bench() {
    echo has_select ${has_selection} query_graph ${query_graph} sel_scaling ${sel_scaling}

    mvn \
        -Dtest=DoExperiments \
        -DhasSelection=${has_selection} \
        -DqueryGraph=${query_graph} \
        -DselScaling=${sel_scaling} \
        test 2>&1 | tee feats-selection_${has_selection}-queryGraph_${query_graph}-selScaling_${sel_scaling}.log
}

for has_selection in false true; do
    query_graph=true sel_scaling=true bench
    query_graph=true sel_scaling=false bench
    query_graph=false sel_scaling=false bench
done
