#!/bin/bash

OUTPUT=war/versions.txt

function echoRevision { 
  VER=`svn info https://svn.ncsa.uiuc.edu/svn/$1/$2 | awk '/Last Changed Rev:/ { print $4 }'`
  echo "$1=$VER" 
}
  
echo "hudson=$BUILD_NUMBER" > $OUTPUT
echo "date=`date`" >> $OUTPUT
echoRevision "mmdb" "trunk" >> $OUTPUT
echoRevision "cet" "trunk" >> $OUTPUT
echoRevision "tupelo" "branches/2.5" >> $OUTPUT
