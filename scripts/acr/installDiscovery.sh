#!/bin/bash

#SEAD Discovery installer - remote mode not yet tested

usage()
{
cat << EOF


       ******* SEAD ACR Discovery Installer *******

This script does a clean remote install of the SEAD ACR Discovery app.
The Discovery connects to an ACR/Medici app 
and displays its data. 

usage: $0 options

OPTIONS:
   -h   Show this message
   -s   Server address to install to (no server = local install)
   -m   Medici base URL for Discovery to connect to (Required)
   -a 	enableAnonymous (try to login as anonymous before showing login prompt)
   -r   RemoetAPIKey (must match setting in ACR/Medici
   -c   Google client ID (Required)
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
clientid=

while getopts  hs:m:var:g:c: OPTION
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
         c)
             clientid=$OPTARG
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
	echo enableAnonymous $anon
	echo Google Client ID: $clientid
fi

if [[ -z $medici ]] || [[ -z clientid]]  
then
	echo Required argument\(s\) missing
  	if [ "$verbose" ]; then
		usage
	fi
    exit 1
fi



if [ "$verbose" ]; then
	echo
	echo 'Installing SEAD ACR Discovery!'
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

	
# install ACR Discovery
#delete current install and any old ones (nced)
if [ "$verbose" ]; then
	echo
	echo 'Deleting old versions...'
fi	

if [ "$server" ]; then
	ssh $server 'rm -rf /var/lib/tomcat6/webapps/{discovery,nced}'
else
		rm -rf /var/lib/tomcat6/webapps/discovery
		rm -rf /var/lib/tomcat6/webapps/nced
fi

echo 
if [ -e "discovery.war" ]; then
	if [ "$verbose" ]; then
		echo 'Found local discovery.war...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving discovery.war from Stash ...'
	fi	
	wget -q -O discovery.war 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/scripts/acr/discovery.war?at=sead-1.2&raw'
fi
	
if [ "$verbose" ]; then
	echo
	echo 'Unzipping discovery.war...'
fi	

unzip -q -d discovery discovery.war
if [ "$verbose" ]; then
	echo
	echo 'Creating discovery.properties...'
fi	

echo "domain=$medici" > discovery/WEB-INF/classes/discovery.properties
if [ "$anon" ]; then
	echo "enableAnonymous=true" >> discovery/WEB-INF/classes/discovery.properties
else
	echo "enableAnonymous=false" >> discovery/WEB-INF/classes/discovery.properties
fi
echo "remoteAPIKey=$apiKey" >> discovery/WEB-INF/classes/discovery.properties
echo "#mapKey=$mapKey" >> discovery/WEB-INF/classes/discovery.properties
echo "google.client_id=$clientid" >> discovery/WEB-INF/classes/discovery.properties

if [ "$verbose" ]; then
	echo
	echo 'Configuring logging...'
fi	

#Discovery is configured to use log4j.xml by default - change that for consistency
rm -f discovery/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > discovery/WEB-INF/classes/commons-logging.properties

if [ -e "discovery.log4j" ]; then
	if [ "$verbose" ]; then
		echo 'Found local discovery.log4j...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving discovery.log4j from Stash ...'
	fi	
	wget -q -O discovery.log4j 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt=web/browse/scripts/acr/discovery.log4j?at=sead-1.2&raw'
fi

cp discovery.log4j  discovery/WEB-INF/classes/log4j.properties


if [ "$verbose" ]; then
	echo
	echo 'Copying to server...'
fi	

if [ "$server" ]; then
	scp -q -r discovery $server:/var/lib/tomcat6/webapps/
else
	mv discovery /var/lib/tomcat6/webapps
fi

if [ "$verbose" ]; then
	echo
	echo 'Setting permissions to tomcat6 group/user...'
fi	

if [ "$server" ]; then
	ssh $server 'chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/discovery'
else	
	chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/discovery
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
	rm -rf discovery
fi

if [ "$verbose" ]; then
	echo
	if [ "$server" ]; then
		echo Done! ACR Discovery installed to $server.
	else	
		echo Done! ACR Discovery installed locally.
	fi	
fi	



