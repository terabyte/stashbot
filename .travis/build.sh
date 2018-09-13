#!/bin/bash
set -o errexit -o nounset

git fetch --unshallow
./bin/invoke-sdk.sh clean test
