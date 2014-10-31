#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war discovery.war /var/lib/tomcat6/webapps/discovery

wget -q -O discovery.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI2/latest/artifact/JOB1/discovery.war/discovery.war

unzip -q -d war discovery.war
cp discovery.properties  war/WEB-INF/classes/discovery.properties
cp discovery.log4j  war/WEB-INF/classes/properties.log4j
rm -f war/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > war/WEB-INF/classes/commons-logging.properties

if [ -e bkgrnd_repeat_x.png ]; then
  cp bkgrnd_repeat_x.png war/login_img
fi
if [ -e header-image.png ]; then
  cp header-image.png war/login_img
fi

chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/discovery

/etc/init.d/tomcat6 restart
