#!/bin/bash
LOCAL=jsdocs
REMOTE=gs://docs.loadtestgo.com
gradle docs
cd $LOCAL
gsutil -m cp -a public-read -r . $REMOTE
