#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war medici.war /var/lib/tomcat6/webapps/acr

#One-time cleanup from old versions
rm -rf dashboard.log4j dashboard.properties dashboard.war update-dashboard.sh /var/lib/tomcat6/webapps/dashboard
rm -rf discovery.log4j discovery.properties discovery.war update-discovery.sh /var/lib/tomcat6/webapps/discovery
rm -rf geobrowse.log4j geobrowse.properties geobrowse.war update-geobrowse.sh /var/lib/tomcat6/webapps/geobrowse

wget -q -O medici.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI/latestSuccessful/artifact/JOB1/medici.war/medici.war

unzip -q -d war medici.war
cp acr.log4j  war/WEB-INF/classes/log4j.properties
cp acr.server war/WEB-INF/classes/server.properties


chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/acr

/etc/init.d/tomcat6 restart
