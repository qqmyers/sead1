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
GOOGLE_DEVID=""
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

# make sure we have latest listing of packages
apt-get -y update

# install rest of packages
apt-get -y install openjdk-7-jre-headless openjdk-7-jre-lib ffmpeg imagemagick mysql-server poppler-utils tomcat6 ttf-dejavu-core ttf-dejavu-extra ttf-kochi-gothic ttf-kochi-mincho ttf-baekmuk ttf-arphic-gbsn00lp ttf-arphic-bsmi00lp ttf-arphic-gkai00mp ttf-arphic-bkai00mp ttf-sazanami-gothic ttf-kochi-gothic ttf-sazanami-mincho ttf-kochi-mincho ttf-wqy-microhei ttf-wqy-zenhei ttf-indic-fonts-core ttf-telugu-fonts ttf-oriya-fonts ttf-kannada-fonts ttf-bengali-fonts ubuntu-restricted-extras unzip gdal-bin python-gdal proj libgdal-dev nginx p7zip-full

# remove java-6
apt-get -y purge --auto-remove 6-jre*

# setup nginx
rm /etc/nginx/sites-enabled/default
cp nginx.conf /etc/nginx/sites-enabled/sead
if [ -e sead.key -a -e sead.crt ]; then
  cp sead.key /etc/ssl/sead.key
  if [ -e intermediate.crt ]; then
    cat sead.crt intermediate.crt > /etc/ssl/sead.crt
  else
    cp sead.crt /etc/ssl/sead.crt
  fi
  cat nginx.ssl >> /etc/nginx/sites-enabled/sead
fi
service nginx restart

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
  wget -q -O /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar "https://opensource.ncsa.illinois.edu/stash/projects/MMDB/repos/medici-gwt-web/browse/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/mysql-connector-java-5.0.4.jar?at=sead-1.2&raw"
fi
if [ ! -e /usr/share/tomcat6/lib/xercesImpl-2.7.1.jar ]; then
  echo "Installing required xerces jar files"
  wget -q -O /usr/share/tomcat6/lib/xercesImpl-2.7.1.jar "https://opensource.ncsa.illinois.edu/stash/projects/MMDB/repos/medici-gwt-web/browse/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/xercesImpl-2.7.1.jar?at=sead-1.2&raw"
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
  echo "JAVA_OPTS=\"\$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true\"" >> /etc/default/tomcat6
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

# install jai to speed up geoserver
if [ ! -e /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/libclib_jiio.so ]; then
  if [ ! -e jai_imageio-1_1-lib-linux-amd64.tar.gz ]; then
    wget -q -O jai_imageio-1_1-lib-linux-amd64.tar.gz http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-linux-amd64.tar.gz
  fi
  tar zxvf jai_imageio-1_1-lib-linux-amd64.tar.gz
  cp jai_imageio-1_1/lib/*.jar /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/ext
  cp jai_imageio-1_1/lib/libclib_jiio.so /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/
  chmod 644 /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/ext/jai_imageio.jar
  rm -rf jai_imageio-1_1-lib-linux-amd64.tar.gz jai_imageio-1_1

  if [ ! -e jai-1_1_3-lib-linux-amd64.tar.gz ]; then
    wget -q -O jai-1_1_3-lib-linux-amd64.tar.gz http://download.java.net/media/jai/builds/release/1_1_3/jai-1_1_3-lib-linux-amd64.tar.gz
  fi
  tar zxvf jai-1_1_3-lib-linux-amd64.tar.gz
  cp jai-1_1_3/lib/*.jar /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/ext
  cp jai-1_1_3/lib/libmlib_jai.so /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/
  rm -rf jai-1_1_3-lib-linux-amd64.tar.gz jai-1_1_3

  rm -f /var/lib/tomcat6/webapps/geoserver/WEB-INF/lib/jai_*
fi

# instal web pages
echo "Installing SEAD webpages"
rm -rf /var/lib/tomcat6/webapps/ROOT
mkdir /var/lib/tomcat6/webapps/ROOT
cp -r static/* /var/lib/tomcat6/webapps/ROOT
echo '<Context path="/" docBase="/var/lib/tomcat6/webapps/ROOT"/>' > /etc/tomcat6/Catalina/localhost/ROOT.xml

# install all update scripts
cp update-*.sh /home/medici
cp *.log4j /home/medici

# install geobrowse
echo "Installing geobrowse"
echo "domain=http://${HOSTNAME}/acr" > /home/medici/geobrowse.properties 
echo "enableAnonymous=${ANONYMOUS}" >> /home/medici/geobrowse.properties
echo "remoteAPIKey=${APIKEY}" >> /home/medici/geobrowse.properties
echo "google.client_id=${GOOGLEID}" >> /home/medici/geobrowse.properties
echo "geoserver=http://${HOSTNAME}/acr/geoproxy" >> /home/medici/geobrowse.properties
echo "proxiedgeoserver=http://${HOSTNAME}/geoserver" >> /home/medici/geobrowse.properties
/home/medici/update-geobrowse.sh

# install discovery
echo "Installing discovery"
echo "domain=http://${HOSTNAME}/acr" > /home/medici/discovery.properties 
echo "enableAnonymous=${ANONYMOUS}" >> /home/medici/discovery.properties
echo "remoteAPIKey=${APIKEY}" >> /home/medici/discovery.properties
echo "google.client_id=${GOOGLEID}" >> /home/medici/discovery.properties
/home/medici/update-discovery.sh

# install dashboard
echo "Installing dashboard"
echo "domain=http://${HOSTNAME}/acr" > /home/medici/dashboard.properties 
echo "enableAnonymous=${ANONYMOUS}" >> /home/medici/dashboard.properties
echo "remoteAPIKey=${APIKEY}" >> /home/medici/dashboard.properties
echo "google.client_id=${GOOGLEID}" >> /home/medici/dashboard.properties
/home/medici/update-dashboard.sh

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
/home/medici/update-extractor.sh

# install medici web app
echo "Installing Medici WebApp"
sed -e "s/^#*remoteAPI=.*$/remoteAPI=${APIKEY}/" \
    -e "s/^#*mail.from=.*$/mail.from=${MEDICI_EMAIL}/" \
    -e "s/^#*user.0.email=.*$/user.0.email=${MEDICI_EMAIL}/" \
    -e "s/^#*user.0.password=.*$/user.0.password=${MEDICI_PASSWORD}/" \
    -e "s/^#*google.client_id=.*$/google.client_id=${GOOGLEID}/" \
    -e "s/^#*google.device_client_id=.*$/google.device_client_id=${GOOGLE_DEVID}/" acr.server > /home/medici/acr.server
echo "geoserver=http://${HOSTNAME}/geoserver" > /home/medici/acr.common
echo "geouser=${GEO_USER}" >> /home/medici/acr.common
echo "geopassword=${GEO_PASSWORD}" >> /home/medici/acr.common
/home/medici/update-web.sh

# All done
echo "The complete stack has been installed/updated. You can access SEAD using the following URL:"
echo "http://${HOSTNAME}/"

