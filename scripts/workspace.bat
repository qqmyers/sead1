@echo off

REM enable delayed expansian of variables
SETLOCAL ENABLEDELAYEDEXPANSION

REM ----------------------------------------------------------------------
REM What should be installed
REM ----------------------------------------------------------------------
REM for mmdb
set INSTALL=edu.illinois.ncsa.mmdb.desktop.site edu.illinois.ncsa.mmdb.extractor.feature edu.illinois.ncsa.mmdb.web

REM for cyberintegrator
REM set INSTALL=edu.illinois.ncsa.cyberintegrator.client.site edu.uiuc.ncsa.cyberintegrator.server.feature edu.uiuc.ncsa.cet.cron.feature edu.illinois.ncsa.cet.contextserver.ui.feature

REM for dse
REM set INSTALL=edu.uiuc.ncsa.cyberintegrator.server.feature dse-webapp

REM ----------------------------------------------------------------------
REM Where is eclipse installed
REM  A version of eclipse need to exist at this location to bootstrap
REM ----------------------------------------------------------------------
set ECLIPSE=C:\Program Files\eclipse

REM ----------------------------------------------------------------------
REM Where is the target platform installed
REM  If no target is found it will be downloaded
REM ----------------------------------------------------------------------
set TARGET=C:\target

REM ----------------------------------------------------------------------
REM Where should buckminster be installed
REM  If buckminster is not found it will be downloaded
REM ----------------------------------------------------------------------
set BUCKMINSTER=C:\buckminster

REM ----------------------------------------------------------------------
REM Where should the workspace be created
REM  This is where all files will appear
REM ----------------------------------------------------------------------
set WORKSPACE=C:\workspace

REM ----------------------------------------------------------------------
REM Info about what type of eclipse is running
REM ----------------------------------------------------------------------
set P2OS=win32
set P2ARCH=x86
set P2WS=win32
set P2NL=en_US

REM ----------------------------------------------------------------------
REM What version should be installed
REM  use main for the trunk/head 
REM  branches start with a /, tags just the name
REM  for example /mmdb-0.5
REM ----------------------------------------------------------------------
set VERSIONALL=main
set VERSIONTUPELO=main

REM ----------------------------------------------------------------------
REM Any certificates needed to be used?
REM ----------------------------------------------------------------------
REM set CERTS=-Djavax.net.ssl.trustStore=C:\jssecacerts

REM ----------------------------------------------------------------------
REM Where to download buckminster from
REM ----------------------------------------------------------------------
set URL=http://download.eclipse.org/tools/buckminster/headless-3.6/
set SVN_URL=http://download.cloudsmith.com/buckminster/external-3.6
set LOGGING=INFO

REM ----------------------------------------------------------------------
REM Install buckminster
REM ----------------------------------------------------------------------
if not exist %BUCKMINSTER% (
  for /f "delims=" %%E in ('dir /b "%ECLIPSE%\plugins\org.eclipse.equinox.launcher_*"') do (
    set EQUINOX="%ECLIPSE%\plugins\%%E"
  )
  java -jar !EQUINOX! -application org.eclipse.equinox.p2.director -destination %BUCKMINSTER% -profile buckminster -installIU org.eclipse.buckminster.cmdline.product -repository %URL% -p2.os %P2OS% -p2.ws %P2WS% -p2.arch %P2ARCH% -p2.nl %P2NL%
  %BUCKMINSTER%/buckminster install %URL% org.eclipse.buckminster.core.headless.feature
  %BUCKMINSTER%/buckminster install %URL%  org.eclipse.buckminster.pde.headless.feature
  %BUCKMINSTER%/buckminster install %SVN_URL% org.eclipse.buckminster.subversive.headless.feature
)

REM ----------------------------------------------------------------------
REM Some variables to get buckminster to work
REM ----------------------------------------------------------------------
for /f "delims=" %%E in ('dir /b "%BUCKMINSTER%\plugins\org.eclipse.equinox.launcher_*"') do (
  set EQUINOX="%BUCKMINSTER%\plugins\%%E"
)
set BUCKMINSTERCMD=java -Xmx512m %CERT% -jar %EQUINOX% -application org.eclipse.buckminster.cmdline.headless -data %WORKSPACE% --loglevel %LOGGING% -consolelog

REM ----------------------------------------------------------------------
REM Eclipse target platform
REM ----------------------------------------------------------------------
if not exist %TARGET% (
  REM create the rmap file
  echo ^<?xml version="1.0" encoding="UTF-8"?^> > eclipse.rmap
  echo ^<rmap xmlns="http://www.eclipse.org/buckminster/RMap-1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >> eclipse.rmap
  echo     xmlns:mp="http://www.eclipse.org/buckminster/MavenProvider-1.0" >> eclipse.rmap
  echo     xmlns:pmp="http://www.eclipse.org/buckminster/PDEMapProvider-1.0" >> eclipse.rmap
  echo     xmlns:bc="http://www.eclipse.org/buckminster/Common-1.0"^> >> eclipse.rmap
  echo. >> eclipse.rmap
  echo     ^<searchPath name="eclipse"^> >> eclipse.rmap
  echo         ^<provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false"^> >> eclipse.rmap
  echo             ^<uri format="http://download.eclipse.org/eclipse/updates/3.6?importType=binary"/^> >> eclipse.rmap
  echo         ^</provider^> >> eclipse.rmap
  echo         ^<provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false"^> >> eclipse.rmap
  echo             ^<uri format="http://download.eclipse.org/releases/helios?importType=binary"/^> >> eclipse.rmap
  echo         ^</provider^> >> eclipse.rmap
  echo     ^</searchPath^> >> eclipse.rmap
  echo. >> eclipse.rmap
  echo     ^<locator searchPathRef="eclipse" /^> >> eclipse.rmap
  echo ^</rmap^> >> eclipse.rmap

  REM fetch all pieces
  for %%P in (org.eclipse.equinox.executable org.eclipse.platform org.eclipse.zest) do (
    echo ^<?xml version="1.0" encoding="UTF-8"?^> > %%P.mspec
    echo  ^<mspec xmlns="http://www.eclipse.org/buckminster/MetaData-1.0" name="Target Platform MSPEC" materializer="p2" installLocation="%TARGET%" url="%%P.cquery"^> >> %%P.mspec
    echo    ^<property key="target.arch" value="*"/^> >> %%P.mspec
    echo    ^<property key="target.os" value="*"/^> >> %%P.mspec
    echo    ^<property key="target.ws" value="*"/^> >> %%P.mspec
    echo ^</mspec^> >> %%P.mspec

    echo ^<?xml version="1.0" encoding="UTF-8"?^> > %%P.cquery
    echo ^<cq:componentQuery xmlns:cq="http://www.eclipse.org/buckminster/CQuery-1.0" resourceMap="eclipse.rmap"^> >> %%P.cquery
    echo     ^<cq:rootRequest name="%%P" componentType="eclipse.feature"/^> >> %%P.cquery
    echo     ^<cq:advisorNode namePattern=".*" useTargetPlatform="false" useWorkspace="false"/^> >> %%P.cquery
    echo     ^<cq:property key="target.arch" value="*"/^> >> %%P.cquery
    echo     ^<cq:property key="target.os" value="*"/^> >> %%P.cquery
    echo     ^<cq:property key="target.ws" value="*"/^> >> %%P.cquery
    echo ^</cq:componentQuery^> >> %%P.cquery

    %BUCKMINSTERCMD% import %%P.mspec
    del %%P.mspec %%P.cquery
  )
  del eclipse.rmap
)

REM set target
echo ^<?xml version="1.0" encoding="UTF-8" standalone="no"?^> > eclipse.target
echo ^<?pde version="3.5"?^> >> eclipse.target
echo.  >> eclipse.target
echo ^<target name="MMDB Target"^> >> eclipse.target
echo   ^<locations^> >> eclipse.target
echo        ^<location path="%TARGET%" type="Profile"/^> >> eclipse.target
echo    ^</locations^> >> eclipse.target
echo    ^<environment^> >> eclipse.target
echo         ^<os^>%P2OS%^</os^> >> eclipse.target
echo         ^<ws^>%P2WS%^</ws^> >> eclipse.target
echo         ^<arch^>%P2ARCH%^</arch^> >> eclipse.target
echo         ^<nl^>%P2NL%^</nl^> >> eclipse.target
echo    ^</environment^> >> eclipse.target
echo    ^<launcherArgs^> >> eclipse.target
echo         ^<vmArgs^>-Dosgi.requiredJavaVersion=1.5 -Xms40m -Xmx512m^</vmArgs^> >> eclipse.target
echo    ^</launcherArgs^> >> eclipse.target
echo ^</target^> >> eclipse.target

%BUCKMINSTERCMD% importtargetdefinition -A %CD%\eclipse.target
del eclipse.target

REM ----------------------------------------------------------------------
REM Populate workspace
REM ----------------------------------------------------------------------

REM create the rmap file
echo ^<?xml version="1.0" encoding="UTF-8"?^> > ncsa.rmap
echo ^<rmap xmlns="http://www.eclipse.org/buckminster/RMap-1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >> ncsa.rmap
echo    xmlns:mp="http://www.eclipse.org/buckminster/MavenProvider-1.0" >> ncsa.rmap
echo    xmlns:pmp="http://www.eclipse.org/buckminster/PDEMapProvider-1.0" >> ncsa.rmap
echo    xmlns:bc="http://www.eclipse.org/buckminster/Common-1.0"^> >> ncsa.rmap
echo. >> ncsa.rmap
echo    ^<searchPath name="ncsa"^> >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/cet/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo. >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/mmdb/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo. >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/cyberintegrator/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo. >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/dse/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo    ^</searchPath^> >> ncsa.rmap
echo. >> ncsa.rmap
echo    ^<searchPath name="tupelo"^> >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="osgi.bundle,eclipse.feature,buckminster" mutable="true" source="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/tupelo/trunk/tupelo-all/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:replace^> >> ncsa.rmap
echo                    ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo                    ^<bc:match pattern="^org\.tupeloproject\.((?:.\w+)*)$" replacement="tupelo-$1" /^> >> ncsa.rmap
echo                ^</bc:replace^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo    ^</searchPath^> >> ncsa.rmap
echo. >> ncsa.rmap
echo    ^<searchPath name="ncsa-orbit"^> >> ncsa.rmap
echo        ^<provider readerType="svn" componentTypes="eclipse.feature,osgi.bundle,buckminster" source="true" mutable="true"^> >> ncsa.rmap
echo            ^<uri format="https://opensource.ncsa.illinois.edu/svn/ncsa-orbit/trunk/{0}?moduleAfterTag&amp;moduleAfterBranch"^> >> ncsa.rmap
echo                ^<bc:propertyRef key="buckminster.component" /^> >> ncsa.rmap
echo            ^</uri^> >> ncsa.rmap
echo        ^</provider^> >> ncsa.rmap
echo    ^</searchPath^> >> ncsa.rmap
echo. >> ncsa.rmap
echo     ^<searchPath name="eclipse"^> >> ncsa.rmap
echo         ^<provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false"^> >> ncsa.rmap
echo             ^<uri format="http://download.eclipse.org/eclipse/updates/3.6?importType=binary"/^> >> ncsa.rmap
echo         ^</provider^> >> ncsa.rmap
echo         ^<provider readerType="p2" componentTypes="osgi.bundle,eclipse.feature" mutable="false" source="false"^> >> ncsa.rmap
echo             ^<uri format="http://download.eclipse.org/releases/helios?importType=binary"/^> >> ncsa.rmap
echo         ^</provider^> >> ncsa.rmap
echo     ^</searchPath^> >> ncsa.rmap
echo. >> ncsa.rmap
echo    ^<locator searchPathRef="ncsa" failOnError="false" /^> >> ncsa.rmap
echo    ^<locator searchPathRef="tupelo" pattern="^org\.tupeloproject(\..+)?"/^> >> ncsa.rmap
echo    ^<locator searchPathRef="ncsa-orbit" failOnError="false" /^> >> ncsa.rmap
echo    ^<locator searchPathRef="eclipse" /^> >> ncsa.rmap
echo ^</rmap^> >> ncsa.rmap

REM fetch all pieces
for %%P in (%INSTALL%) do (
  echo ^<?xml version="1.0" encoding="UTF-8"?^> > %%P.cquery
  echo ^<cq:componentQuery xmlns:cq="http://www.eclipse.org/buckminster/CQuery-1.0" resourceMap="ncsa.rmap"^> >> %%P.cquery
  echo     ^<cq:rootRequest name="%%P"/^> >> %%P.cquery
  echo     ^<cq:property key="target.arch" value="*"/^> >> %%P.cquery
  echo     ^<cq:property key="target.os" value="*"/^> >> %%P.cquery
  echo     ^<cq:property key="target.ws" value="*"/^> >> %%P.cquery
  echo     ^<cq:advisorNode namePattern="edu.uiuc.ncsa.*" branchTagPath="%VERSIONALL%"/^> >> %%P.cquery
  echo     ^<cq:advisorNode namePattern="edu.illinois.ncsa.*" branchTagPath="%VERSIONALL%"/^> >> %%P.cquery
  echo     ^<cq:advisorNode namePattern="org.tupeloproject.*" branchTagPath="%VERSIONTUPELO%"/^> >> %%P.cquery
  echo ^</cq:componentQuery^> >> %%P.cquery

  %BUCKMINSTERCMD% import %%P.cquery
  del %%P.cquery
)

REM remove map file
del ncsa.rmap
pause

