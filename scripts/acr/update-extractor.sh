#!/bin/bash

cd /home/medici

/etc/init.d/medici stop

if [ -e extractor ]; then
  mv extractor/server.properties extractor.properties
fi

rm -rf extractor Extractor.gtk.linux.x86_64.zip

wget -q -O Extractor.gtk.linux.x86_64.zip https://opensource.ncsa.illinois.edu/jenkins/job/Medici%20Extractor/lastSuccessfulBuild/artifact/buckminster.output/edu.illinois.ncsa.medici.extractor.site_1.2.100-eclipse.feature/Extractor.gtk.linux.x86_64.zip
unzip -q Extractor.gtk.linux.x86_64.zip
mv Extractor.gtk.linux.x86_64 extractor
mv extractor.properties extractor/server.properties

cd extractor
./ExtractionServer -application org.eclipse.equinox.p2.director \
	-r https://opensource.ncsa.illinois.edu/p2/medici/latest/extractor/ \
	-i edu.illinois.ncsa.mmdb.extractor.image.feature.feature.group,edu.illinois.ncsa.mmdb.extractor.csv.feature.feature.group,edu.illinois.ncsa.mmdb.extractor.excel.feature.feature.group,edu.illinois.ncsa.mmdb.extractor.pdf.feature.feature.group,edu.illinois.ncsa.medici.extractor.geoserver.feature.feature.group
cd ..

chown -R tomcat6.users extractor
rm Extractor.gtk.linux.x86_64.zip

/etc/init.d/medici start
