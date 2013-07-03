#!/bin/bash

sudo apt-get install python-software-properties
sudo add-apt-repository ppa:ubuntugis/ubuntugis-unstable
apt-get update

apt-get -y install default-jre-headless ffmpeg imagemagick mysql-server poppler-utils tomcat6 ttf-dejavu-core ttf-dejavu-extra ttf-kochi-gothic ttf-kochi-mincho ttf-baekmuk ttf-arphic-gbsn00lp ttf-arphic-bsmi00lp ttf-arphic-gkai00mp ttf-arphic-bkai00mp ttf-sazanami-gothic ttf-kochi-gothic ttf-sazanami-mincho ttf-kochi-mincho ttf-wqy-microhei ttf-wqy-zenhei ttf-indic-fonts-core ttf-telugu-fonts ttf-oriya-fonts ttf-kannada-fonts ttf-bengali-fonts ubuntu-restricted-extras unzip gdal-bin python-gdal proj libgdal-dev

mkdir -p /home/medici/data /home/medici/lucene
chown tomcat6.users /home/medici/data /home/medici/lucene

wget -O /usr/share/tomcat6/lib/mysql-connector-java-5.0.4.jar https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/edu.illinois.ncsa.mmdb.web/war/WEB-INF/lib/mysql-connector-java-5.0.4.jar

cp medici-extractor /etc/init.d
chmod 755 /etc/init.d/medici-extractor
update-rc.d medici-extractor defaults

mysql --defaults-extra-file=/etc/mysql/debian.cnf < medici.sql

cat >> /etc/default/tomcat6 << EOF

# Fix for zoomable images (MMDB-1087)
JAVA_OPTS="${JAVA_OPTS} -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
EOF

wget http://downloads.sourceforge.net/geoserver/geoserver-2.3.0-war.zip
unzip geoserver-2.3.0-war.zip
rm -rf geoserver-2.3.0-war.zip GPL.txt LICENSE.txt target
cp geoserver.war /var/lib/tomcat6/webapps/

wget https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/geo-webapp/war/geo-webapp.war
unzip -d geo-webapp geo-webapp.war
cp geo-webapp.xml geo-webapp/WEB-INF/web.xml
mv geo-webapp /var/lib/tomcat6/webapps

cp update-extractor.sh update-web.sh acr.log4j acr.server acr.public_properties /home/medici

cd /home/medici
./update-extractor.sh
./update-web.sh

echo "Follow steps 4-8 on https://opensource.ncsa.illinois.edu/confluence/display/MMDB/Geoserver+Extractor"
echo "Set the password for admin to tu5id6geaj7w"
