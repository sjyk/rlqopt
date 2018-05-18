#!/bin/bash
set -x
# References:
# https://github.com/tensorflow/tensorflow/blob/master/tensorflow/java/README.md
# https://www.tensorflow.org/versions/master/install/install_java
#
# For Intellij 
# (1) run this script, then 
# (2) add the resulting "jni" folder to Project Settings -> Libraries -> "+" -> "jni".

TF_TYPE="cpu" # Default processor is CPU. If you want GPU, set to "gpu"
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
mkdir -p ./jni
curl -L \
  "https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-${TF_TYPE}-${OS}-x86_64-1.8.0.tar.gz" |
  tar -xz -C ./jni
