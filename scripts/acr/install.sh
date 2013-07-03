#!/bin/bash

if [ $UID != 0 ]; then
  echo "Please run this script as root."
  exit
fi

if [ ! -e "/etc/apt/sources.list.d/ubuntugis-ubuntugis-unstable-`lsb_release -c -s`.list" ]; then
  apt-get -y install python-software-properties
  add-apt-repository ppa:ubuntugis/ubuntugis-unstable
fi

apt-get -y update
apt-get -y install default-jre-headless ffmpeg imagemagick mysql-server poppler-utils tomcat6 ttf-dejavu-core ttf-dejavu-extra ttf-kochi-gothic ttf-kochi-mincho ttf-baekmuk ttf-arphic-gbsn00lp ttf-arphic-bsmi00lp ttf-arphic-gkai00mp ttf-arphic-bkai00mp ttf-sazanami-gothic ttf-kochi-gothic ttf-sazanami-mincho ttf-kochi-mincho ttf-wqy-microhei ttf-wqy-zenhei ttf-indic-fonts-core ttf-telugu-fonts ttf-oriya-fonts ttf-kannada-fonts ttf-bengali-fonts ubuntu-restricted-extras unzip gdal-bin python-gdal proj libgdal-dev

if [ ! -e /home/medici/data ]; then
  mkdir -p /home/medici/data
  chown tomcat6.users /home/medici/data
fi
if [ ! -e /home/medici/data ]; then
  mkdir -p /home/medici/lucene
  chown tomcat6.users /home/medici/lucene
fi

if [ ! -e /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar ]; then
  wget -O /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/mysql-connector-java-5.0.4.jar
fi

RET=$( /usr/bin/mysql --defaults-extra-file=/etc/mysql/debian.cnf -NBe "SELECT COUNT(SCHEMA_NAME) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='medici';" )
if [ $? -eq 0 ]; then
  if [ $RET -eq 0 ]; then
    /usr/bin/mysql --defaults-extra-file=/etc/mysql/debian.cnf < medici.sql
  fi
fi

if ! grep -Fq "MMDB-1087" /etc/default/tomcat6; then
  echo "" >> /etc/default/tomcat6
  echo "# Fix for zoomable images (MMDB-1087)" >> /etc/default/tomcat6
  echo "JAVA_OPTS=\"$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true\"" >> /etc/default/tomcat6
fi

if [ ! -e /var/lib/tomcat6/webapps/geoserver ]; then
  wget http://downloads.sourceforge.net/geoserver/geoserver-2.3.0-war.zip
  unzip -q geoserver-2.3.0-war.zip
  rm -rf geoserver-2.3.0-war.zip GPL.txt LICENSE.txt target
  unzip -q -d geoserver geoserver.war
  rm -rf geoserver/data
  unzip -q -d geoserver geoserver-data.zip
  chown -R tomcat6 geoserver/data
  mv geoserver /var/lib/tomcat6/webapps/
fi

if [ ! -e /var/lib/tomcat6/webapps/geo-webapp ]; then
  wget https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/geo-webapp/war/geo-webapp.war
  unzip -q -d geo-webapp geo-webapp.war
  cp geo-webapp.xml geo-webapp/WEB-INF/web.xml
  mv geo-webapp /var/lib/tomcat6/webapps
fi

cp medici-extractor /etc/init.d
chmod 755 /etc/init.d/medici-extractor
update-rc.d medici-extractor defaults

cp -f update-extractor.sh update-web.sh /home/medici
if [ ! -e /home/medici/acr.log4j ]; then
  cp acr.log4j /home/medici
fi
if [ ! -e /home/medici/acr.server ]; then
  cp acr.server /home/medici
fi
if [ ! -e /home/medici/acr.public_properties ]; then
  cp acr.public_properties /home/medici
fi

rm -rf /var/lib/tomcat6/webapps/ROOT
mkdir /var/lib/tomcat6/webapps/ROOT
cp -r static/* /var/lib/tomcat6/webapps/ROOT
echo '<Context path="/" docBase="/var/lib/tomcat6/webapps/ROOT"/>' > /etc/tomcat6/Catalina/localhost/ROOT.xml

rm -rf /var/lib/tomcat6/webapps/summary
unzip -q -d /var/lib/tomcat6/webapps/summary summary.war
echo "domain=http://`hostname -f`/acr/resteasy/sparql" > /var/lib/tomcat6/webapps/summary/WEB-INF/classes/nced.properties 
echo "projectPath=http://`hostname -f`/acr" >> /var/lib/tomcat6/webapps/summary/WEB-INF/classes/nced.properties 

rm -rf /var/lib/tomcat6/webapps/nced
unzip -q -d /var/lib/tomcat6/webapps/nced nced.war
echo "domain=http://`hostname -f`/acr/resteasy/sparql" > /var/lib/tomcat6/webapps/nced/WEB-INF/classes/nced.properties 


cd /home/medici
./update-extractor.sh
./update-web.sh

echo "The complete stack has been installed/updated. You can access SEAD using the following URL:"
echo "http://`hostname -f`/"
