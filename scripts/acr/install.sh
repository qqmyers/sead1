#!/bin/bash

if [ $UID != 0 ]; then
  echo "Please run this script as root."
  exit
fi

# some variables
FQDN=$( hostname -f )
ANONYMOUS="true"
APIKEY=$( uuidgen -r )
GOOGLEAPIKEY=""
GOOGLEID=""
GOOGLE_DEVID=""
ORCIDID=""
ORCIDSECRET=""
MEDICI_EMAIL="root@localhost"
MEDICI_PASSWORD="secret"
VA_PASSWORD="secret"
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
apt-get -y update > /dev/null

# install rest of packages
apt-get -y install openjdk-7-jre-headless openjdk-7-jre-lib ffmpeg imagemagick mysql-server poppler-utils tomcat6 ttf-dejavu-core ttf-dejavu-extra ttf-kochi-gothic ttf-kochi-mincho ttf-baekmuk ttf-arphic-gbsn00lp ttf-arphic-bsmi00lp ttf-arphic-gkai00mp ttf-arphic-bkai00mp ttf-sazanami-gothic ttf-kochi-gothic ttf-sazanami-mincho ttf-kochi-mincho ttf-wqy-microhei ttf-wqy-zenhei ttf-indic-fonts-core ttf-telugu-fonts ttf-oriya-fonts ttf-kannada-fonts ttf-bengali-fonts ubuntu-restricted-extras unzip gdal-bin python-gdal proj libgdal-dev nginx p7zip-full

# remove java-6
apt-get -y purge --auto-remove 6-jre*

# setup nginx
if [ ! -e /etc/nginx/sites-enabled/sead ]; then
  rm /etc/nginx/sites-enabled/default
  cp nginx.conf /etc/nginx/sites-enabled/sead
  service nginx restart
fi

# Configure Tomcat to use forwarded headers from nginx
sed -i '/.*<Engine name=\"Catalina\" defaultHost=\"localhost\">/a \
\t<Valve className=\"org.apache.catalina.valves.RemoteIpValve\"  \
\t\tremoteIpHeader=\"X-Forwarded-For\"   \
\t\tprotocolHeader=\"X-Forwarded-Proto\"  \
\t\tprotocolHeaderHttpsValue=\"https\"/>' /var/lib/tomcat6/conf/server.xml
       


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

# fix for zoomable images - now required for all restful services that encode ids for collections (which have '/' in them)
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
echo "Installing SEAD redirect"
rm -rf /var/lib/tomcat6/webapps/ROOT
mkdir /var/lib/tomcat6/webapps/ROOT
echo '<% response.sendRedirect("/acr"); %>' > /var/lib/tomcat6/webapps/ROOT/index.jsp

# install all update scripts
cp update-*.sh /home/medici
cp *.log4j /home/medici

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
    -e "s#^geoserver.server=.*\$#geoserver.server=http://${FQDN}/geoserver#" \
    -e "s#^geoserver.owsserver=.*\$#geoserver.owsserver=http://${FQDN}/geoserver/wms#" extractor.properties > /home/medici/extractor.properties
/home/medici/update-extractor.sh

# install web app
echo "Installing WebApp"
cp acr.server.common /home/medici/acr.server
if [ -e acr.server.bigdata.${HOSTNAME} ]; then
  cat acr.server.bigdata.${HOSTNAME} >> /home/medici/acr.server
else
  cat acr.server.bigdata >> /home/medici/acr.server
fi
if [ -e acr.server.userfields.${HOSTNAME} ]; then
  cat acr.server.userfields.${HOSTNAME} >> /home/medici/acr.server
else
  cat acr.server.userfields >> /home/medici/acr.server
fi
if [ -e acr.server.relationships.${HOSTNAME} ]; then
  cat acr.server.relationships.${HOSTNAME} >> /home/medici/acr.server
else
  cat acr.server.relationships >> /home/medici/acr.server
fi

sed -i -e "s/^#*remoteAPIKey=.*$/remoteAPIKey=${APIKEY}/" \
       -e "s/^#*mail.from=.*$/mail.from=${MEDICI_EMAIL}/" \
       -e "s/^#*user.0.email=.*$/user.0.email=${MEDICI_EMAIL}/" \
       -e "s/^#*user.0.password=.*$/user.0.password=${MEDICI_PASSWORD}/" \
       -e "s/^#*user.1.password=.*$/user.1.password=${VA_PASSWORD}/" \
       -e "s/^#*google.api_key=.*$/google.api_key=${GOOGLEAPIKEY}/" \
       -e "s/^#*google.client_id=.*$/google.client_id=${GOOGLEID}/" \
       -e "s/^#*google.device_client_id=.*$/google.device_client_id=${GOOGLE_DEVID}/" \
       -e "s/^#*orcid.client_id=.*$/orcid.client_id=${ORCIDID}/" \
       -e "s/^#*orcid.client_secret=.*$/orcid.client_secret=${ORCIDSECRET}/" \
       -e "s/^#*proxiedgeoserver=.*$/proxiedgeoserver=https:\/\/${FQDN}\/geoserver/" \
       -e "s/^#*geoserver=.*$/geoserver=https:\/\/${FQDN}\/acr\/geoproxy/" \
       -e "s/^#*geouser=.*$/geouser=${GEO_USER}/" \
       -e "s/^#*geopassword=.*$/geopassword=${GEO_PASSWORD}/" \
       -e "s/^#*domain=.*$/domain=https:\/\/${FQDN}\/acr/" /home/medici/acr.server
/home/medici/update-web.sh

# cleanup
rm -rf /home/medici/dashboard.* /home/medici/installDashboard.sh /home/medici/update-dashboard.sh /var/lib/tomcat6/webapps/dashboard
rm -rf /home/medici/discovery.* /home/medici/installDiscovery.sh /home/medici/update-discovery.sh /var/lib/tomcat6/webapps/discovery
rm -rf /home/medici/geobrowse.* /home/medici/installGeobrowser.sh /home/medici/update-geobrowse.sh /var/lib/tomcat6/webapps/geobrowse

# All done
echo "The complete stack has been installed/updated. You can access SEAD using the following URL:"
echo "http://${FQDN}/"

