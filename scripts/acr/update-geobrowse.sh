#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war geobrowse.war /var/lib/tomcat6/webapps/geobrowse

wget -q -O geobrowse.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI2/latest/artifact/JOB1/geobrowse.war/geobrowse.war

unzip -q -d war geobrowse.war
cp geobrowse.properties  war/WEB-INF/classes/geobrowse.properties
cp geobrowse.log4j  war/WEB-INF/classes/properties.log4j
rm -f war/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > war/WEB-INF/classes/commons-logging.properties

chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/geobrowse

/etc/init.d/tomcat6 restart
