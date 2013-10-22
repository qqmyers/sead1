#!/bin/bash

#SEAD Dashboard installer - remote mode not yet tested

usage()
{
cat << EOF


       ******* SEAD ACR Dashboard Installer *******

This script does a clean remote install of the SEAD ACR Dashboard app.
The Dashboard connects to an ACR/Medici app 
and displays its data. 

usage: $0 options

OPTIONS:
   -h   Show this message
   -s   Server address to install to (no server = local install)
   -m   Medici base URL for Dashboard to connect to (Required)
   -a 	enableAnonymous (try to login as anonymous before showing login prompt)
   -r   RemoetAPIKey (must match setting in ACR/Medici
   -g	Google map key (not yet used / hardcoded in dashboard.jsp)
   -c	Google Client ID (Required)
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
         g)
             mapKey=$OPTARG
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
	echo Google Map Key: $mapKey
	echo enableAnonymous $anon
	echo Google client id $clientid
fi

if [[ -z $medici ]] || [[ -z clientid ]]  
then
	echo Required argument\(s\) missing
  	if [ "$verbose" ]; then
		usage
	fi
    exit 1
fi



if [ "$verbose" ]; then
	echo
	echo 'Installing SEAD ACR Dashboard!'
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

	
# install ACR Dashboard
#delete current install and any old ones (projectsummary, summary)
if [ "$verbose" ]; then
	echo
	echo 'Deleting old versions...'
fi	

if [ "$server" ]; then
	ssh $server 'rm -rf /var/lib/tomcat6/webapps/{dashboard,projectsummary,summary}'
else
		rm -rf /var/lib/tomcat6/webapps/dashboard
		rm -rf /var/lib/tomcat6/webapps/projectsummary
		rm -rf /var/lib/tomcat6/webapps/summary
fi

echo 
if [ -e "dashboard.war" ]; then
	if [ "$verbose" ]; then
		echo 'Found local dashboard.war...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving dashboard.war from Stash ...'
	fi	
	wget -q -O dashboard.war 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/scripts/acr/dashboard.war?at=sead-1.2&raw'
fi
	
if [ "$verbose" ]; then
	echo
	echo 'Unzipping dashboard.war...'
fi	

unzip -q -d dashboard dashboard.war
if [ "$verbose" ]; then
	echo
	echo 'Creating dashboard.properties...'
fi	

echo "domain=$medici" > dashboard/WEB-INF/classes/dashboard.properties
if [ "$anon" ]; then
	echo "enableAnonymous=true" >> dashboard/WEB-INF/classes/dashboard.properties
else
	echo "enableAnonymous=false" >> dashboard/WEB-INF/classes/dashboard.properties
fi
echo "remoteAPIKey=$apiKey" >> dashboard/WEB-INF/classes/dashboard.properties
echo "#mapKey=$mapKey" >> dashboard/WEB-INF/classes/dashboard.properties
echo "google.client_id=$clientid" >> dashboard/WEB-INF/classes/dashboard.properties

if [ "$verbose" ]; then
	echo
	echo 'Configuring logging...'
fi	

#Dashboard is configured to use log4j.xml by default - change that for consistency
rm -f dashboard/WEB-INF/classes/log4j.xml
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > dashboard/WEB-INF/classes/commons-logging.properties

if [ -e "dashboard.log4j" ]; then
	if [ "$verbose" ]; then
		echo 'Found local dashboard.log4j...'
	fi	
else
	if [ "$verbose" ]; then
		echo 'Retrieving dashboard.log4j from Stash ...'
	fi	
	wget -q -O dashboard.log4j 'https://opensource.ncsa.illinois.edu/stash/projects/MED/repos/medici-gwt-web/browse/scripts/acr/dashboard.log4j?at=sead-1.2&raw'
fi

cp dashboard.log4j  dashboard/WEB-INF/classes/log4j.properties


if [ "$verbose" ]; then
	echo
	echo 'Copying to server...'
fi	

if [ "$server" ]; then
	scp -q -r dashboard $server:/var/lib/tomcat6/webapps/
else
	mv dashboard /var/lib/tomcat6/webapps
fi

if [ "$verbose" ]; then
	echo
	echo 'Setting permissions to tomcat6 group/user...'
fi	

if [ "$server" ]; then
	ssh $server 'chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/dashboard'
else	
	chown -R tomcat6.tomcat6 /var/lib/tomcat6/webapps/dashboard
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
	rm -rf dashboard
fi

if [ "$verbose" ]; then
	echo
	if [ "$server" ]; then
		echo Done! ACR Dashboard installed to $server.
	else	
		echo Done! ACR Dashboard installed locally.
	fi	
fi	



