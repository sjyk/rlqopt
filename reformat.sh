#!/bin/bash
sources=$(find . -name '*.java')
java -jar jars/google-java-format-1.5-all-deps.jar -replace ${sources}
