#!/bin/bash

DRYRUN="echo"

MAINTAINER=kooper@ncsa.illinois.edu
DATABASE=mmdb
DB_USER=mmdb
DB_PASS=mmdb
MMDB=/home/mmdb

GOOGLEMAPKEY=

# O.4 release (added fixes to executor)
EXTRACTOR_URL=http://isda.ncsa.uiuc.edu:8090/job/MMDB%20Extractor/91/artifact/build/I.Extractor/Extractor-linux.gtk.x86_64.tar.gz
MMDB_URL=http://isda.ncsa.uiuc.edu:8090/job/MMDB%20Web/421/artifact/mmdb/build/mmdb.war

# latest
#EXTRACTOR_URL=http://isda.ncsa.uiuc.edu:8090/job/MMDB%20Extractor/lastSuccessfulBuild/artifact/build/I.Extractor/Extractor-linux.gtk.x86_64.tar.gz
#MMDB_URL=http://isda.ncsa.uiuc.edu:8090/job/MMDB%20Web/lastSuccessfulBuild/artifact/mmdb/build/mmdb.war

TWEAKS="yes"

# ----------------------------------------------------------------------
if [ -e /etc/redhat-release ]; then
  DIST=redhat
else
  DIST=ubuntu
fi
echo "CONFIGURING FOR $DIST"

if [ "$DIST" == "ubuntu" ]; then
  TOMCAT_USER=tomcat6
  TOMCAT_DIR=/var/lib/tomcat6
  TOMCAT_SCRIPT=/etc/init.d/tomcat6
fi
if [ "$DIST" == "redhat" ]; then
  TOMCAT_USER=tomcat
  TOMCAT_DIR=/usr/share/tomcat5/
  TOMCAT_SCRIPT=/etc/init.d/tomcat5
fi

# ----------------------------------------------------------------------
echo "NCSA TWEAKS"

if [ "$TWEAKS" == "yes" -a "$DIST" == "ubuntu" ]; then
  $DRYRUN sed -i.bak -e 's#http://us.archive.ubuntu.com/ubuntu/#http://cosmos.cites.uiuc.edu/pub/ubuntu#g' /etc/apt/sources.list
  $DRYRUN apt-get -qq update
  $DRYRUN apt-get -qq -y install ntp

  $DRYRUN sed -i.bak -e 's#server ntp.ubuntu.com#server ntp.ncsa.uiuc.edu#g' /etc/ntp.conf
  $DRYRUN /etc/init.d/ntp restart
fi

# ----------------------------------------------------------------------
echo "UPDATING SYSTEM/INSTALLING SOFTWARE"

if [ "$DIST" == "ubuntu" ]; then
  $DRYRUN apt-get -qq -y dist-upgrade
  $DRYRUN apt-get -qq -y install mysql-server-5.1 unzip apache2 tomcat6
fi

# ----------------------------------------------------------------------
echo "CREATING WORKING FOLDER"

$DRYRUN mkdir -p $MMDB/data
$DRYRUN chown -R $TOMCAT_USER $MMDB/data
$DRYRUN cd $MMDB

# ----------------------------------------------------------------------
echo "CREATING DATABASE"

$DRYRUN cat > mmdb.sql << EOF
DROP DATABASE IF EXISTS $DATABASE;
CREATE DATABASE $DATABASE;
USE $DATABASE;

CREATE TABLE \`blb\` (
  \`bid\` bigint(20) NOT NULL,
  \`bda\` longblob,
  PRIMARY KEY  (\`bid\`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE \`sym\` (
  \`uid\` bigint(20) NOT NULL auto_increment,
  \`hsh\` varchar(32) default NULL,
  \`sym\` text character set latin1 collate latin1_bin,
  PRIMARY KEY  (\`uid\`),
  UNIQUE KEY \`hsh\` (\`hsh\`)
) ENGINE=InnoDB AUTO_INCREMENT=1221 DEFAULT CHARSET=latin1;

CREATE TABLE \`tup\` (
  \`sub\` bigint(20) NOT NULL,
  \`pre\` bigint(20) NOT NULL,
  \`obj\` bigint(20) NOT NULL,
  \`typ\` bigint(20) default NULL,
  UNIQUE KEY \`sub\` (\`sub\`,\`pre\`,\`obj\`),
  KEY \`sub_2\` (\`sub\`),
  KEY \`pre\` (\`pre\`),
  KEY \`obj\` (\`obj\`),
  KEY \`sub_3\` (\`sub\`,\`pre\`),
  KEY \`sub_4\` (\`sub\`,\`obj\`),
  KEY \`pre_2\` (\`pre\`,\`obj\`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

GRANT ALL ON $DATABASE.* TO '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
EOF
$DRYRUN mysql -u root -p -h localhost < mmdb.sql

# ----------------------------------------------------------------------
echo "EXTRACTION SERVICE"

# download latests build
$DRYRUN wget -q -O $$.tar.gz $EXTRACTOR_URL
$DRYRUN rm -rf Extractor
$DRYRUN tar zxf $$.tar.gz
$DRYRUN chmod -R g+w Extractor
$DRYRUN chown -R $TOMCAT_USER Extractor
$DRYRUN rm -rf $$.tar.gz

if [ "$TWEAKS" == "yes" ]; then
  # Give more memory
  $DRYRUN sed -i -e 's#-Xmx1G#-Xmx10G#g' Extractor/ExtractionServer.ini

  # Give more extractors
  $DRYRUN sed -i -e 's/workers.\([a-z0-9]*\)=1/workers.\1=2/' Extractor/server.properties 
fi

# fix extractor paths/user
$DRYRUN cp Extractor/service/extractor.$DIST extractor
$DRYRUN sed -i -e "s#DIR=/home/kooper/Extractor#DIR=$MMDB/Extractor#" -e "s#USER=kooper#USER=$TOMCAT_USER#" extractor

# autostart extractor
$DRYRUN cp extractor /etc/init.d
$DRYRUN chmod 755 /etc/init.d/extractor
if [ "$DIST" == "ubuntu" ]; then
  $DRYRUN update-rc.d extractor defaults
fi
if [ "$DIST" == "redhat" ]; then
  $DRYRUN /sbin/chkconfig extractor on
fi
$DRYRUN /etc/init.d/extractor start

# ----------------------------------------------------------------------
#echo "IPTABLES"
#echo "If needed add a hole in the firewall for extractor service"
#echo "# Extractor Service Medici"
#echo "-A RH-Firewall-1-INPUT -m state --state NEW -m tcp -p tcp --dport 9856 -j ACCEPT"

# ----------------------------------------------------------------------
echo "WEB CLIENT"

# fix tomcat
if [ "$TWEAKS" == "yes" -a "$DIST" == "ubuntu" ]; then
  $DRYRUN sed -i -e 's#TOMCAT6_SECURITY=yes#TOMCAT6_SECURITY=no#g' \
	-e "s#-outfile SYSLOG -errfile SYSLOG#-outfile $TOMCAT_DIR/logs/stdout -errfile $TOMCAT_DIR/logs/stderr#g" \
	-e 's#-Xmx128M#-Xmx256M#' $TOMCAT_SCRIPT
fi

# add proxy for mmdb
$DRYRUN cat > /etc/apache2/conf.d/mmdb.conf << EOF
<Proxy *>
Order deny,allow
Allow from all
</Proxy>

ProxyRequests           Off
ProxyPreserveHost       On
ProxyPass               /mmdb   http://localhost:8080/mmdb
ProxyPassReverse        /mmdb   http://localhost:8080/mmdb
EOF

if [ "$DIST" == "ubuntu" ]; then
  $DRYRUN a2enmod proxy_http
fi
if [ "$DIST" == "redhat" ]; then
  $DRYRUN echo "ENABLE PROXY MOD"
fi
$DRYRUN /etc/init.d/apache2 restart

# create update file
$DRYRUN cat > updatewar.sh << EOF
#!/bin/sh

cd /home/mmdb
rm -rf war mmdb.war
wget http://isda.ncsa.uiuc.edu:8090/job/MMDB%20Web/lastSuccessfulBuild/artifact/mmdb/build/mmdb.war
unzip -q -d war mmdb.war
cp context.xml war/WEB-INF/classes
cp server.properties war/WEB-INF/classes
chown -R $TOMCAT_USER war

$TOMCAT_SCRIPT stop
sleep 1
rm -rf $TOMCAT_DIR/webapps/mmdb
mv war $TOMCAT_DIR/webapps/mmdb
$TOMCAT_SCRIPT start

rm mmdb.war
EOF
$DRYRUN chmod 755 updatewar.sh

# create the context.xml file
$DRYRUN cat > context.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:a="http://cet.ncsa.uiuc.edu/2007/context/" xmlns:b="tag:tupeloproject.org,2006:/2.0/beans/2.0/" xmlns:cet="http://cet.ncsa.uiuc.edu/2007/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
  <cet:Context rdf:about="tag:cet.ncsa.uiuc.edu,2008:/bean/Context/4429050e-f7b0-4fa7-b1ee-c007ddee8641">
    <a:hasChildren>
      <rdf:Seq rdf:about="tag:tupeloproject.org,2006:e3d29a476bd2848c90aedce8f3ede4f9a8e5ff9b">
        <rdf:_1>
          <cet:Context rdf:about="tag:cet.ncsa.uiuc.edu,2008:/bean/Context/f1c18980-d469-4d71-9a6e-31a266f876c5">
            <a:hasChildren>
              <rdf:Seq rdf:about="tag:tupeloproject.org,2006:221770df355e5235a5c1272e308824ee09690f25"/>
            </a:hasChildren>
            <a:hasProperties>
              <b:storageTypeMapEntry rdf:about="tag:tupeloproject.org,2006:32d4516c8c0f0027d70b29e8fcb8d8c0b1b52249">
                <b:storageTypeMapKey>MySQL.user</b:storageTypeMapKey>
                <b:storageTypeMapValue>$DB_USER</b:storageTypeMapValue>
              </b:storageTypeMapEntry>
            </a:hasProperties>
            <a:hasProperties>
              <b:storageTypeMapEntry rdf:about="tag:tupeloproject.org,2006:3ccc4b4cf570df6d08c5fbb2abd83b4b0b2a908f">
                <b:storageTypeMapKey>MySQL.host</b:storageTypeMapKey>
                <b:storageTypeMapValue>localhost</b:storageTypeMapValue>
              </b:storageTypeMapEntry>
            </a:hasProperties>
            <a:hasProperties>
              <b:storageTypeMapEntry rdf:about="tag:tupeloproject.org,2006:908286823be7e7091438eab98862af39c910a9d8">
                <b:storageTypeMapKey>MySQL.password</b:storageTypeMapKey>
                <b:storageTypeMapValue>$DB_PASS</b:storageTypeMapValue>
              </b:storageTypeMapEntry>
            </a:hasProperties>
            <a:hasProperties>
              <b:storageTypeMapEntry rdf:about="tag:tupeloproject.org,2006:baeb324048fff81406286306cac647d62c885d18">
                <b:storageTypeMapKey>MySQL.database</b:storageTypeMapKey>
                <b:storageTypeMapValue>$DATABASE</b:storageTypeMapValue>
              </b:storageTypeMapEntry>
            </a:hasProperties>
            <a:isType>MySQL</a:isType>
            <dc:creator>
              <foaf:Person rdf:about="http://cet.ncsa.uiuc.edu/2007/person/anonymous">
                <dc:identifier>http://cet.ncsa.uiuc.edu/2007/person/anonymous</dc:identifier>
                <rdf:type rdf:resource="tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry"/>
                <rdfs:label>Anonymous</rdfs:label>
                <foaf:mbox/>
                <foaf:name>Anonymous</foaf:name>
                <b:propertyImplementationMappingSubject rdf:resource="tag:cet.ncsa.uiuc.edu,2009:/mapping/http://xmlns.com/foaf/0.1/Person"/>
                <b:propertyValueImplementationClassName>edu.uiuc.ncsa.cet.bean.PersonBean</b:propertyValueImplementationClassName>
              </foaf:Person>
            </dc:creator>
            <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">2010-02-05T03:35:15.968Z</dc:date>
            <dc:description/>
            <dc:identifier>tag:cet.ncsa.uiuc.edu,2008:/bean/Context/f1c18980-d469-4d71-9a6e-31a266f876c5</dc:identifier>
            <dc:title>MySQL</dc:title>
            <rdf:type rdf:resource="tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry"/>
            <rdfs:label>MySQL</rdfs:label>
            <b:propertyImplementationMappingSubject rdf:resource="tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Context"/>
            <b:propertyValueImplementationClassName>edu.uiuc.ncsa.cet.bean.context.ContextBean</b:propertyValueImplementationClassName>
          </cet:Context>
        </rdf:_1>
        <rdf:_2>
          <cet:Context rdf:about="tag:cet.ncsa.uiuc.edu,2008:/bean/Context/c67a3cdb-c08c-4289-aef2-6827d0d78c90">
            <a:hasChildren>
              <rdf:Seq rdf:about="tag:tupeloproject.org,2006:c8991733d11ad088c721e633f774e38edec0363e"/>
            </a:hasChildren>
            <a:hasProperties>
              <b:storageTypeMapEntry rdf:about="tag:tupeloproject.org,2006:1f54b2550c47fe349b23567961f120c650556174">
                <b:storageTypeMapKey>folder</b:storageTypeMapKey>
                <b:storageTypeMapValue>$MMDB/data</b:storageTypeMapValue>
              </b:storageTypeMapEntry>
            </a:hasProperties>
            <a:isType>HashFile</a:isType>
            <dc:creator rdf:resource="http://cet.ncsa.uiuc.edu/2007/person/anonymous"/>
            <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">2010-02-05T03:35:15.968Z</dc:date>
            <dc:description/>
            <dc:identifier>tag:cet.ncsa.uiuc.edu,2008:/bean/Context/c67a3cdb-c08c-4289-aef2-6827d0d78c90</dc:identifier>
            <dc:title>HashFile</dc:title>
            <rdf:type rdf:resource="tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry"/>
            <rdfs:label>HashFile</rdfs:label>
            <b:propertyImplementationMappingSubject rdf:resource="tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Context"/>
            <b:propertyValueImplementationClassName>edu.uiuc.ncsa.cet.bean.context.ContextBean</b:propertyValueImplementationClassName>
          </cet:Context>
        </rdf:_2>
      </rdf:Seq>
    </a:hasChildren>
    <a:isType>ContentStore</a:isType>
    <dc:creator rdf:resource="http://cet.ncsa.uiuc.edu/2007/person/anonymous"/>
    <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">2010-02-05T03:35:15.968Z</dc:date>
    <dc:description/>
    <dc:identifier>tag:cet.ncsa.uiuc.edu,2008:/bean/Context/4429050e-f7b0-4fa7-b1ee-c007ddee8641</dc:identifier>
    <dc:title>MySQL and Hashfile</dc:title>
    <rdf:type rdf:resource="tag:tupeloproject.org,2006:/2.0/DefaultContext"/>
    <rdf:type rdf:resource="tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry"/>
    <rdfs:label>MySQL and Hashfile</rdfs:label>
    <b:propertyImplementationMappingSubject rdf:resource="tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Context"/>
    <b:propertyValueImplementationClassName>edu.uiuc.ncsa.cet.bean.context.ContextBean</b:propertyValueImplementationClassName>
  </cet:Context>
</rdf:RDF>
EOF

# download and install latest version
$DRYRUN wget $MMDB_URL
$DRYRUN unzip -q -d war mmdb.war
$DRYRUN cp context.xml war/WEB-INF/classes
$DRYRUN cp war/WEB-INF/classes/server.properties .
$DRYRUN sed -i.bak -e "s#mail.from=lmarini@ncsa.illinois.edu#mail.from=$MAINTAINER#g" -e "s/#user.0.email=/user.0.email=$MAINTAINER/g" server.properties
$DRYRUN mv war $TOMCAT_DIR/webapps/mmdb
$DRYRUN rm mmdb.war
$DRYRUN $TOMCAT_SCRIPT restart

# ----------------------------------------------------------------------
echo "READY"
echo "A reboot might be required after the dist-upgrade"
