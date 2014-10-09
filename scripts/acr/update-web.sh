#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war medici.war /var/lib/tomcat6/webapps/acr

wget -q -O medici.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI2/latest/artifact/JOB1/medici.war/medici.war

unzip -q -d war medici.war
cp acr.log4j  war/WEB-INF/classes/log4j.properties
cp acr.server war/WEB-INF/classes/server.properties
cp acr.common war/WEB-INF/classes/acr_commons.properties

chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/acr

/etc/init.d/tomcat6 restart
