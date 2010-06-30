#!/bin/bash

# ----------------------------------------------------------------------
# What should be installed
# ----------------------------------------------------------------------
# for mmdb
export INSTALL="edu.illinois.ncsa.mmdb.desktop.site edu.illinois.ncsa.mmdb.extractor.feature edu.illinois.ncsa.mmdb.web"

# for cyberintegrator
#export INSTALL="edu.illinois.ncsa.cyberintegrator.client.site edu.uiuc.ncsa.cyberintegrator.server.feature edu.uiuc.ncsa.cet.cron.feature edu.illinois.ncsa.cet.contextserver.ui.feature"

# for dse
#export INSTALL="edu.uiuc.ncsa.cyberintegrator.server.feature dse-webapp"

# ----------------------------------------------------------------------
# Where is eclipse installed
#  A version of eclipse need to exist at this location to bootstrap
# ----------------------------------------------------------------------
export ECLIPSE=/home/kooper/eclipse

# ----------------------------------------------------------------------
# Where is the target platform installed
#  If no target is found it will be downloaded
# ----------------------------------------------------------------------
export TARGET=`readlink -m $PWD/target`
#export TARGET=$HOME/projects/target

# ----------------------------------------------------------------------
# Where should buckminster be installed
#  If buckminster is not found it will be downloaded
# ----------------------------------------------------------------------
export BUCKMINSTER=`readlink -m $PWD/buckminster`
#export BUCKMINSTER=$HOME/projects/buckminster

# ----------------------------------------------------------------------
# Where should the workspace be created
#  This is where all files will appear
# ----------------------------------------------------------------------
export WORKSPACE=`readlink -m $PWD/workspace`

# ----------------------------------------------------------------------
# Info about what type of eclipse is running
# ----------------------------------------------------------------------
export P2OS=linux
export P2ARCH=`arch`
export P2WS=gtk
export P2NL=en_US

# ----------------------------------------------------------------------
# What version should be installed
#  use main for the trunk/head 
#  branches start with a /, tags just the name
#  for example /mmdb-0.5
# ----------------------------------------------------------------------
VERSIONALL=main
VERSIONTUPELO=main

# ----------------------------------------------------------------------
# Any certificates needed to be used?
# ----------------------------------------------------------------------
#export CERTS="-Djavax.net.ssl.trustStore=/home/kooper/jssecacerts"

# ----------------------------------------------------------------------
# Where to download buckminster from
# ----------------------------------------------------------------------
export URL=http://download.eclipse.org/tools/buckminster/headless-3.6
export SVN_URL=http://download.cloudsmith.com/buckminster/external-3.6
export LOGGING=INFO

# ----------------------------------------------------------------------
# Install buckminster
# ----------------------------------------------------------------------
if [ ! -e $BUCKMINSTER ]; then
  export EQUINOX="`ls -1rt $ECLIPSE/plugins/org.eclipse.equinox.launcher_*.jar | head -1`"
  java -jar $EQUINOX -application org.eclipse.equinox.p2.director -destination $BUCKMINSTER -profile buckminster -installIU org.eclipse.buckminster.cmdline.product -repository $URL -p2.os $P2OS -p2.ws $P2WS -p2.arch $P2ARCH -p2.nl $P2NL
  $BUCKMINSTER/buckminster install $URL org.eclipse.buckminster.core.headless.feature
  $BUCKMINSTER/buckminster install $URL  org.eclipse.buckminster.pde.headless.feature
  $BUCKMINSTER/buckminster install $SVN_URL org.eclipse.buckminster.subversive.headless.feature
fi

# ----------------------------------------------------------------------
# Some variables to get buckminster to work
# ----------------------------------------------------------------------
export EQUINOX="`ls -1rt $BUCKMINSTER/plugins/org.eclipse.equinox.launcher_*.jar | head -1`"
export BUCKMINSTERCMD="java -Xmx512m $CERTS -jar $EQUINOX -application org.eclipse.buckminster.cmdline.headless -data $WORKSPACE --loglevel $LOGGING -consolelog"

# ----------------------------------------------------------------------
# Eclipse target platform
# ----------------------------------------------------------------------
if [ ! -e $TARGET ]; then
  # create the rmap file
  cat > eclipse.rmap << EOF
<?xml version="1.0" encoding="UTF-8"?>
<rmap xmlns="http://www.eclipse.org/buckminster/RMap-1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:mp="http://www.eclipse.org/buckminster/MavenProvider-1.0"
    xmlns:pmp="http://www.eclipse.org/buckminster/PDEMapProvider-1.0"
    xmlns:bc="http://www.eclipse.org/buckminster/Common-1.0">

    <searchPath name="eclipse">
        <provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false">
            <uri format="http://download.eclipse.org/eclipse/updates/3.6?importType=binary"/>
        </provider>
        <provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false">
            <uri format="http://download.eclipse.org/releases/helios?importType=binary"/>
        </provider>
    </searchPath>

    <locator searchPathRef="eclipse" />
</rmap>
EOF

  # fetch all pieces
  for PLUGIN in org.eclipse.equinox.executable org.eclipse.platform org.eclipse.zest; do
    cat > $PLUGIN.mspec << EOF
<?xml version="1.0" encoding="UTF-8"?>
<mspec xmlns="http://www.eclipse.org/buckminster/MetaData-1.0" name="Target Platform MSPEC" materializer="p2" installLocation="$TARGET" url="$PLUGIN.cquery">
   <property key="target.arch" value="*"/>
   <property key="target.os" value="*"/>
   <property key="target.ws" value="*"/>
</mspec>
EOF
    cat > $PLUGIN.cquery << EOF
<?xml version="1.0" encoding="UTF-8"?>
<cq:componentQuery xmlns:cq="http://www.eclipse.org/buckminster/CQuery-1.0" resourceMap="eclipse.rmap">
    <cq:rootRequest name="$PLUGIN" componentType="eclipse.feature"/>
    <cq:advisorNode namePattern=".*" useTargetPlatform="false" useWorkspace="false"/>
    <cq:property key="target.arch" value="*"/>
    <cq:property key="target.os" value="*"/>
    <cq:property key="target.ws" value="*"/> 
</cq:componentQuery>
EOF
    $BUCKMINSTERCMD import $PLUGIN.mspec
    rm $PLUGIN.mspec $PLUGIN.cquery
  done
  rm eclipse.rmap
fi

# set target
cat > eclipse.target << EOF
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.5"?>

<target name="MMDB Target">
  <locations>
       <location path="$TARGET" type="Profile"/>
   </locations>
   <environment>
        <os>$P2OS</os>
        <ws>$P2WS</ws>
        <arch>$P2ARCH</arch>
        <nl>$P2NL</nl>
   </environment>
   <launcherArgs>
        <vmArgs>-Dosgi.requiredJavaVersion=1.5 -Xms40m -Xmx512m</vmArgs>
   </launcherArgs>
</target>
EOF
$BUCKMINSTERCMD importtargetdefinition -A $PWD/eclipse.target
rm eclipse.target

# ----------------------------------------------------------------------
# Populate workspace
# ----------------------------------------------------------------------

# create the rmap file
cat > ncsa.rmap << EOF
<?xml version="1.0" encoding="UTF-8"?>
<rmap xmlns="http://www.eclipse.org/buckminster/RMap-1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:mp="http://www.eclipse.org/buckminster/MavenProvider-1.0"
    xmlns:pmp="http://www.eclipse.org/buckminster/PDEMapProvider-1.0"
    xmlns:bc="http://www.eclipse.org/buckminster/Common-1.0">

    <searchPath name="ncsa">
        <provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/cet/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:propertyRef key="buckminster.component" />
            </uri>
        </provider>

        <provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:propertyRef key="buckminster.component" />
            </uri>
        </provider>

        <provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/cyberintegrator/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:propertyRef key="buckminster.component" />
            </uri>
        </provider>

        <provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/dse/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:propertyRef key="buckminster.component" />
            </uri>
        </provider>
    </searchPath>

    <searchPath name="tupelo">
        <provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/tupelo/trunk/tupelo-all/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:replace>
                    <bc:propertyRef key="buckminster.component" />
                    <bc:match pattern="^org\.tupeloproject\.((?:.\w+)*)$" replacement="tupelo-\$1" />
                </bc:replace>
            </uri>
        </provider>
    </searchPath>

    <searchPath name="ncsa-orbit">
        <provider readerType="svn" componentTypes="eclipse.feature,osgi.bundle,buckminster" source="true" mutable="true">
            <uri format="https://opensource.ncsa.illinois.edu/svn/ncsa-orbit/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch">
                <bc:propertyRef key="buckminster.component" />
            </uri>
        </provider>        
    </searchPath>

    <searchPath name="eclipse">
        <provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false">
            <uri format="http://download.eclipse.org/eclipse/updates/3.6?importType=binary"/>
        </provider>
        <provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false">
            <uri format="http://download.eclipse.org/releases/helios?importType=binary"/>
        </provider>
    </searchPath>

    <locator searchPathRef="ncsa" failOnError="false" />
    <locator searchPathRef="tupelo" pattern="^org\.tupeloproject(\..+)?" />
    <locator searchPathRef="ncsa-orbit" failOnError="false" />
    <locator searchPathRef="eclipse" />
</rmap>
EOF

# fetch all pieces
for PLUGIN in $INSTALL; do
  cat > $PLUGIN.cquery << EOF
<?xml version="1.0" encoding="UTF-8"?>
<cq:componentQuery xmlns:cq="http://www.eclipse.org/buckminster/CQuery-1.0" resourceMap="ncsa.rmap">
    <cq:rootRequest name="$PLUGIN"/>
    <cq:property key="target.arch" value="*"/>
    <cq:property key="target.os" value="*"/>
    <cq:property key="target.ws" value="*"/>
    <cq:advisorNode namePattern="edu.uiuc.ncsa.*" branchTagPath="$VERSIONALL"/>
    <cq:advisorNode namePattern="edu.illinois.ncsa.*" branchTagPath="$VERSIONALL"/>
    <cq:advisorNode namePattern="org.tupeloproject.*" branchTagPath="$VERSIONTUPELO"/>
</cq:componentQuery>
EOF
  $BUCKMINSTERCMD import $PLUGIN.cquery
  rm $PLUGIN.cquery
done

# remove map file
rm ncsa.rmap

