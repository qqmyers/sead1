#!/bin/bash

if [ $UID != 0 ]; then
  echo "Please run this script as root."
  exit
fi

# some variables
HOSTNAME=$( hostname -f )
ANONYMOUS="true"
APIKEY=$( uuidgen -r )
GOOGLEID=""
MEDICI_EMAIL="root@localhost"
MEDICI_PASSWORD="secret"
GEO_USER="admin"
GEO_PASSWORD="secret"
if [ -e config.txt ]; then
  . ./config.txt
fi

# install dependencies
echo "Installing/checking dependencies"
if [ ! -e "/etc/apt/sources.list.d/ubuntugis-ubuntugis-unstable-`lsb_release -c -s`.list" ]; then
  apt-get -y install python-software-properties
  add-apt-repository -y ppa:ubuntugis/ubuntugis-unstable
fi

apt-get -y update
apt-get -y install default-jre-headless ffmpeg imagemagick mysql-server poppler-utils tomcat6 ttf-dejavu-core ttf-dejavu-extra ttf-kochi-gothic ttf-kochi-mincho ttf-baekmuk ttf-arphic-gbsn00lp ttf-arphic-bsmi00lp ttf-arphic-gkai00mp ttf-arphic-bkai00mp ttf-sazanami-gothic ttf-kochi-gothic ttf-sazanami-mincho ttf-kochi-mincho ttf-wqy-microhei ttf-wqy-zenhei ttf-indic-fonts-core ttf-telugu-fonts ttf-oriya-fonts ttf-kannada-fonts ttf-bengali-fonts ubuntu-restricted-extras unzip gdal-bin python-gdal proj libgdal-dev

# make tomcat run on port 80
sed -i -e 's/8080/80/g' -e 's/8443/443/g' /etc/tomcat6/server.xml
sed -i -e 's/^#*AUTHBIND=.*$/AUTHBIND=yes/' /etc/default/tomcat6

# create folders
if [ ! -e /home/medici/data ]; then
  echo "Creating medici data folders"
  mkdir -p /home/medici/data
  chown tomcat6.users /home/medici/data
fi
if [ ! -e /home/medici/lucene ]; then
  echo "Creating medici lucene folders"
  mkdir -p /home/medici/lucene
  chown tomcat6.users /home/medici/lucene
fi

# install jar files
echo "Installing required jar files"
if [ ! -e /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar ]; then
  echo "Installing required mysql jar files"
  wget -q -O /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar "https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/mysql-connector-java-5.0.4.jar?at=sead-1.2&raw"
fi
if [ ! -e /usr/share/tomcat6/lib/xercesImpl-2.7.1.jar ]; then
  echo "Installing required xerces jar files"
  wget -q -O /usr/share/tomcat6/lib/xercesImpl-2.7.1.jar "https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/xercesImpl-2.7.1.jar?at=sead-1.2&raw"
fi

# create database
RET=$( /usr/bin/mysql --defaults-extra-file=/etc/mysql/debian.cnf -NBe "SELECT COUNT(SCHEMA_NAME) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='medici';" )
if [ $? -eq 0 ]; then
  if [ $RET -eq 0 ]; then
    echo "Creating Medici Database"
    /usr/bin/mysql --defaults-extra-file=/etc/mysql/debian.cnf < medici.sql
  fi
fi

# fix for zoomable images
if ! grep -Fq "MMDB-1087" /etc/default/tomcat6; then
  echo "Installing fix for zoomable images in tomcat"
  echo "" >> /etc/default/tomcat6
  echo "# Fix for zoomable images (MMDB-1087)" >> /etc/default/tomcat6
  echo "JAVA_OPTS=\"$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true\"" >> /etc/default/tomcat6
fi

# install geoserver
if [ ! -e /var/lib/tomcat6/webapps/geoserver ]; then
  echo "Installing GEOserver"
  wget -q -O geoserver-2.3.0-war.zip http://downloads.sourceforge.net/geoserver/geoserver-2.3.0-war.zip
  unzip -q geoserver-2.3.0-war.zip
  rm -rf geoserver-2.3.0-war.zip GPL.txt LICENSE.txt target
  unzip -q -d geoserver geoserver.war
  rm -rf geoserver/data
  unzip -q -d geoserver geoserver-data.zip
  chown -R tomcat6 geoserver/data
  rm -rf /var/lib/tomcat6/webapps/geoserver
  mv geoserver /var/lib/tomcat6/webapps/
fi

# instal web pages
echo "Installing SEAD webpages"
rm -rf /var/lib/tomcat6/webapps/ROOT
mkdir /var/lib/tomcat6/webapps/ROOT
cp -r static/* /var/lib/tomcat6/webapps/ROOT
echo '<Context path="/" docBase="/var/lib/tomcat6/webapps/ROOT"/>' > /etc/tomcat6/Catalina/localhost/ROOT.xml

# install geobrowse
echo "Installing geobrowse"
wget -q -O geobrowse.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI0/latest/artifact/JOB1/geobrowse.war/geobrowse.war
unzip -q -d geobrowse geobrowse.war
echo "domain=http://${HOSTNAME}/acr" > geobrowse/WEB-INF/classes/geobrowse.properties 
echo "enableAnonymous=${ANONYMOUS}" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "remoteAPIKey=${APIKEY}" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "google.client_id=${GOOGLEID}" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "geoserver=http://${HOSTNAME}/acr/geoproxy" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "proxiedgeoserver=http://${HOSTNAME}/geoserver" >> geobrowse/WEB-INF/classes/geobrowse.properties
rm -f geobrowse/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > geobrowse/WEB-INF/classes/commons-logging.properties
cp geobrowse.log4j geobrowse/WEB-INF/classes/properties.log4j
chown -R tomcat6 geobrowse
rm -rf /var/lib/tomcat6/webapps/geobrowse
mv geobrowse /var/lib/tomcat6/webapps

# install discovery
echo "Installing discovery"
wget -q -O discovery.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI0/latest/artifact/JOB1/discovery.war/discovery.war
unzip -q -d discovery discovery.war
echo "domain=http://${HOSTNAME}/acr" > discovery/WEB-INF/classes/discovery.properties 
echo "enableAnonymous=${ANONYMOUS}" >> discovery/WEB-INF/classes/discovery.properties
echo "remoteAPIKey=${APIKEY}" >> discovery/WEB-INF/classes/discovery.properties
echo "google.client_id=${GOOGLEID}" >> discovery/WEB-INF/classes/discovery.properties
rm -f discovery/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > discovery/WEB-INF/classes/commons-logging.properties
cp discovery.log4j discovery/WEB-INF/classes/properties.log4j
chown -R tomcat6 discovery
rm -rf /var/lib/tomcat6/webapps/discovery
mv discovery /var/lib/tomcat6/webapps

# install dashboard
echo "Installing dashboard"
wget -q -O dashboard.war https://opensource.ncsa.illinois.edu/bamboo/browse/MMDB-MEDICI0/latest/artifact/JOB1/dashboard.war/dashboard.war
unzip -q -d dashboard dashboard.war
echo "domain=http://${HOSTNAME}/acr" > dashboard/WEB-INF/classes/dashboard.properties 
echo "enableAnonymous=${ANONYMOUS}" >> dashboard/WEB-INF/classes/dashboard.properties
echo "remoteAPIKey=${APIKEY}" >> dashboard/WEB-INF/classes/dashboard.properties
echo "google.client_id=${GOOGLEID}" >> dashboard/WEB-INF/classes/dashboard.properties
rm -f dashboard/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > dashboard/WEB-INF/classes/commons-logging.properties
cp dashboard.log4j dashboard/WEB-INF/classes/properties.log4j
chown -R tomcat6 dashboard
rm -rf /var/lib/tomcat6/webapps/dashboard
mv dashboard /var/lib/tomcat6/webapps

# install medici extractor
echo "Installing Medici Extractor"
if [ -e /etc/init.d/medici-extractor ]; then
  rm /etc/init.d/medici-extractor
  update-rc.d medici-extractor remove
fi
cp medici /etc/init.d
chmod 755 /etc/init.d/medici
update-rc.d medici defaults
sed -e "s#^geoserver.username=.*\$#geoserver.username=${GEO_USER}#" \
    -e "s#^geoserver.password=.*\$#geoserver.password=${GEO_PASSWORD}#" \
    -e "s#^geoserver.server=.*\$#geoserver.server=http://${HOSTNAME}/geoserver#" \
    -e "s#^geoserver.owsserver=.*\$#geoserver.owsserver=http://${HOSTNAME}/geoserver/wms#" extractor.properties > /home/medici/extractor.properties
cp -f update-extractor.sh /home/medici
/home/medici/update-extractor.sh

# install medici web app
echo "Installing Medici WebApp"
cp acr.log4j /home/medici
sed -e "s/^#*remoteAPI=.*$/remoteAPI=${APIKEY}/" \
    -e "s/^#*mail.from=.*$/mail.from=${MEDICI_EMAIL}/" \
    -e "s/^#*user.0.email=.*$/user.0.email=${MEDICI_EMAIL}/" \
    -e "s/^#*user.0.password=.*$/user.0.password=${MEDICI_PASSWORD}/" \
    -e "s/^#*google.client_id=.*$/google.client_id=${GOOGLEID}/" acr.server > /home/medici/acr.server
echo "geoserver=http://${HOSTNAME}/geoserver" > /home/medici/acr.common
echo "geouser=${GEO_USER}" >> /home/medici/acr.common
echo "geopassword=${GEO_PASSWORD}" >> /home/medici/acr.common
cp -f update-web.sh /home/medici
/home/medici/update-web.sh

# All done
echo "The complete stack has been installed/updated. You can access SEAD using the following URL:"
echo "http://${HOSTNAME}/"

