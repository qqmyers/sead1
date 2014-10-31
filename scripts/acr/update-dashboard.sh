#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war dashboard.war /var/lib/tomcat6/webapps/dashboard

wget -q -O dashboard.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI2/latest/artifact/JOB1/dashboard.war/dashboard.war

unzip -q -d war dashboard.war
cp dashboard.properties  war/WEB-INF/classes/dashboard.properties
cp dashboard.log4j  war/WEB-INF/classes/properties.log4j
rm -f war/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > war/WEB-INF/classes/commons-logging.properties

chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/dashboard

/etc/init.d/tomcat6 restart
