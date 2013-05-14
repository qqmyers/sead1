#!/bin/bash

if [ ! -e summary.war ]; then
  echo "Download war file first and place here as summary.war"
  exit
fi

/etc/init.d/tomcat6 stop

rm -rf war
rm -rf /var/lib/tomcat6/webapps/summary

echo "summary installation"
unzip -q -d war summary.war
chown -R tomcat6.tomcat6 war
mv war  /var/lib/tomcat6/webapps/summary
echo "end summary installation"

/etc/init.d/tomcat6 restart
