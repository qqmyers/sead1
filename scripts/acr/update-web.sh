#!/bin/bash

cd /home/medici

/etc/init.d/tomcat6 stop

rm -rf war medici.war /var/lib/tomcat6/webapps/acr


wget -q -O medici.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI0-90/artifact/JOB1/medici.war/medici.war
#Or get the war from stash:
#wget -q -O medici.war  'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/scripts/acr/medici.war?at=sead-1.2&raw'

unzip -q -d war medici.war
cp acr.log4j  war/WEB-INF/classes/log4j.properties
cp acr.server war/WEB-INF/classes/server.properties
cp acr.commons war/WEB-INF/classes/acr_commons.properties

chown -R tomcat6.tomcat6 war
mv war /var/lib/tomcat6/webapps/acr

/etc/init.d/tomcat6 restart
