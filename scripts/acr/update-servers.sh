#!/bin/bash

if [ $UID != 0 ]; then
  echo "Please run this script as root."
  exit
fi

for h in colombiaflood hydroshare ifri irbo lowermississippi nced sead sead-demo wsc-reach; do
  echo $h

# show users
#   echo $h
#   ssh $h "mysql -N -B  -u medici -p\"medici\" medici -e \"SELECT SUBSTRING(o2.sym, 2), SUBSTRING(o1.sym, 2) FROM tup AS t1, tup as t2, sym AS p1, sym AS p2, sym AS o1, sym AS o2 WHERE t2.sub=t1.sub AND p1.sym='uhttp://cet.ncsa.uiuc.edu/2007/lastLogin' AND t1.pre=p1.uid AND t1.obj=o1.uid AND p2.sym='uhttp://xmlns.com/foaf/0.1/name' AND t2.pre=p2.uid AND t2.obj=o2.uid ORDER BY o1.sym DESC;\""

# install redirect to main app (/ to /acr)
#  ssh $h 'rm -rf /var/lib/tomcat6/webapps/ROOT && mkdir /var/lib/tomcat6/webapps/ROOT'
#  echo '<% response.sendRedirect("/acr"); %>' > $h.jsp
#  scp $h.jsp $h:/var/lib/tomcat6/webapps/ROOT/index.jsp
#  rm $h.jsp

# update extractor/webapp
#  scp update-extractor.sh $h:/home/medici/update-extractor.sh
#  ssh $h '/home/medici/update-web.sh'
#  scp update-web.sh $h:/home/medici/update-web.sh
#  ssh $h '/home/medici/update-web.sh'

# restart tomcat
  ssh $h '/etc/init.d/tomcat6 restart'
done
