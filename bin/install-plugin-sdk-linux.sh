#!/bin/bash

# Version 6.3.10
DOWNLOAD_URL="https://marketplace.atlassian.com/download/apps/1210993/version/42430"
# To find new URLs, see: https://marketplace.atlassian.com/plugins/atlassian-plugin-sdk-tgz/versions

INSTALL_BIN=`pwd`/.sdk.tar.gz
INSTALL_DIR=`pwd`/.sdk
TMP_DIR=`pwd`/.tmp

if [[ `uname` -ne "Linux" ]]; then
    echo "ERROR: this script currently only supports linux" && exit 1
fi
if [[ ! -x $INSTALL_DIR/bin/atlas-mvn ]]; then
    # clean up everything, reinstall plugin sdk
    rm -rf $INSTALL_DIR
    if [[ ! -f $INSTALL_BIN ]]; then
        # download plugin sdk
        echo "Downloading SDK"
        wget -O$INSTALL_BIN $DOWNLOAD_URL || exit 1;
    fi

    mkdir $TMP_DIR && cd $TMP_DIR && tar -xzf $INSTALL_BIN
    SDK_DIR=`ls -d atlassian-plugin-sdk-*`
    mv $SDK_DIR $INSTALL_DIR
    cd .. && rm -rf $TMP_DIR
fi

exit 0
