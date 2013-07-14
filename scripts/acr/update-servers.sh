#!/bin/bash

if [ $UID != 0 ]; then
  echo "Please run this script as root."
  exit
fi

for h in colombiaflood hydroshare ifri irbo lowermississippi sead sead-demo wsc-reach; do
#  scp update-web.sh $h:/home/medici/update-web.sh
#  scp update-extractor.sh $h:/home/medici/update-extractor.sh
#  ssh $h '/home/medici/update-web.sh'
#  ssh $h '/home/medici/update-extractor.sh'
  ssh $h '/etc/init.d/medici status'
done
