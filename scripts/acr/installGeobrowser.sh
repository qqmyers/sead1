#!/bin/bash

#SEAD Geobrowser installer - remote mode not yet tested

usage()
{
cat << EOF


       ******* SEAD ACR Geobrowser Installer *******

This script does a clean remote install of the SEAD ACR Geobrowser app.
The Geobrowser connects to an ACR/Medici app 
and displays its data. 

usage: $0 options

OPTIONS:
   -h   Show this message
   -s   Server address to install to (no server = local install)
   -m   Medici base URL for Geobrowser to connect to (Required)
   -a 	enableAnonymous (try to login as anonymous before showing login prompt)
   -r   RemoetAPIKey (must match setting in ACR/Medici
   -g	Google map key (not yet used / hardcoded in geobrowse.jsp)
   		The Geobrowser can use hardcoded credentials for direct accesses to a remote geoserver or a proxy 
   		The following four parameters set that up:
   -u   Username on the geoserver (only needed for direct access)
   -p	Password on the geoserver (only needed for direct access)
   -x   URL for the proxied geoserver (the original geoserver URL in the proxied case)
   -q	URL for the geoserver endpoint to use  (the geoserver in the direct case, the proxy URL in the proxies case)
   -v   Verbose
EOF
}


if [ $UID != 0 ]; then
  echo "Please run this script as root."
  echo
  usage
  exit 1
fi

server=
medici=
verbose=
anon=
apiKey=
mapKey=
gUser=
gPassword=
gServer=
gProxy=

while getopts  hs:m:var:g:u:p:q:x: OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         s)
             server=$OPTARG
             ;;
         m)
             medici=$OPTARG
             ;;
         r)
             apiKey=$OPTARG
             ;;
         g)
             mapKey=$OPTARG
             ;;
         u)
             gUser=$OPTARG
             ;;
         p)
             gPassword=$OPTARG
             ;;
         q)
             gServer=$OPTARG
             ;;
         x)
             gProxy=$OPTARG
             ;;             
         v)
             verbose=1
             ;;
         a)
             anon=1
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

if [ "$verbose" ]; then
	echo Server: $server
	echo Medici: $medici
	echo remoteAPIKey: $apiKey
	echo Google Map Key: $mapKey
	echo enableAnonymous $anon
	echo geoserver user $gUser
	echo geoserver password  $gPassword
	echo geoserver URL $gServer
	echo geoproxy URL  $gProxy
fi

if [[ -z $medici ]] || [[ -z $gServer ]] 
then
	if [[[-z $gUser] || [-z $gPassword]] && [-z gProxy]]
	then 
		echo Required argument\(s\) missing
  		if [ "$verbose" ]; then
			usage
		fi
    	exit 1
    fi	
fi



if [ "$verbose" ]; then
	echo
	echo 'Installing SEAD ACR Geobrowser!'
fi

if [ "$verbose" ]; then
	echo
	echo 'Stopping Tomcat...'
fi	

if [ "$server" ]; then
	ssh $server '/etc/init.d/tomcat6 stop'
else	
	/etc/init.d/tomcat6 stop
fi	

	
# install ACR Geobrowser
#delete current install and any old ones (geo-webapp)
if [ "$verbose" ]; then
	echo
	echo 'Deleting old versions...'
fi	

if [ "$server" ]; then
	ssh $server 'rm -rf /var/lib/tomcat6/webapps/{geobrowse,geo-webapp}'
else
		rm -rf /var/lib/tomcat6/webapps/geobrowse
		rm -rf /var/lib/tomcat6/webapps/geo-webapp
fi

echo 
if [ -e "geobrowse.war" ]; then
	if [ "$verbose" ]; then
		echo 'Found local geobrowse.war...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving geobrowse.war from Stash ...'
	fi	
	wget -q -O geobrowse.war 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt=web/browse/scripts/acr/geobrowse.war?at=sead-1.2&raw'
fi
	
if [ "$verbose" ]; then
	echo
	echo 'Unzipping geobrowse.war...'
fi	

unzip -q -d geobrowse geobrowse.war
if [ "$verbose" ]; then
	echo
	echo 'Creating geobrowse.properties...'
fi	

echo "domain=$medici" > geobrowse/WEB-INF/classes/geobrowse.properties
if [ "$anon" ]; then
	echo "enableAnonymous=true" >> geobrowse/WEB-INF/classes/geobrowse.properties
else
	echo "enableAnonymous=false" >> geobrowse/WEB-INF/classes/geobrowse.properties
fi
echo "remoteAPIKey=$apiKey" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "#mapKey=$mapKey" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "geoserver=$gServer" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "geouser=$gUser" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "geopassword=$gPassword" >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "proxiedgeoserver=$gProxy"  >> geobrowse/WEB-INF/classes/geobrowse.properties
echo "google.client_id=$clientid" >> geobrowse/WEB-INF/classes/geobrowse.properties

if [ "$verbose" ]; then
	echo
	echo 'Configuring logging...'
fi	

#Geobrowser is configured to use log4j.xml by default - change that for consistency
rm -f geobrowse/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > geobrowse/WEB-INF/classes/commons-logging.properties

if [ -e "geobrowse.log4j" ]; then
	if [ "$verbose" ]; then
		echo 'Found local geobrowse.log4j...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving geobrowse.log4j from Stash ...'
	fi	
	wget -q -O geobrowse.log4j 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/scripts/acr/geobrowse.log4j?at=sead-1.2&raw'
fi

cp geobrowse.log4j  geobrowse/WEB-INF/classes/log4j.properties


if [ "$verbose" ]; then
	echo
	echo 'Copying to server...'
fi	

if [ "$server" ]; then
	scp -q -r geobrowse $server:/var/lib/tomcat6/webapps/
else
	mv geobrowse /var/lib/tomcat6/webapps
fi

if [ "$verbose" ]; then
	echo
	echo 'Setting permissions to tomcat6 group/user...'
fi	

if [ "$server" ]; then
	ssh $server 'chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/geobrowse'
else	
	chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/geobrowse
fi	

if [ "$verbose" ]; then
	echo
	echo 'Restarting Tomcat...'
fi	

if [ "$server" ]; then
	ssh $server '/etc/init.d/tomcat6 restart'
else	
	/etc/init.d/tomcat6 restart
fi	

if [ "$server" ]; then
	if [ "$verbose" ]; then
		echo
		echo 'Cleaning up...'
	fi	
	rm -rf geobrowse
fi

if [ "$verbose" ]; then
	echo
	if [ "$server" ]; then
		echo Done! ACR Geobrowser installed to $server.
	else	
		echo Done! ACR Geobrowser installed locally.
	fi	
fi	



