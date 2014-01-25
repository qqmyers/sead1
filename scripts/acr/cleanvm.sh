#!/bin/bash

# run this as:
# history -c && ./cleanvm.sh

sudo apt-get -y update
sudo apt-get -y dist-upgrade

sudo apt-get --purge remove
sudo apt-get clean
sudo rm /etc/udev/rules.d/70-persistent-net.rules
sudo rm -rf /root/.ssh
sudo rm /root/.bash_history
sudo touch /root/.bash_history

sudo rm -rf /var/log
sudo mkdir -p /var/log/{apparmor,apt,dist-upgrade,fsck,installer,landscape,mysql,news,unattended-upgrades,tomcat6}
sudo chown landscape /var/log/landscape
sudo chown tomcat6 /var/log/tomcat6
sudo chown mysql.adm /var/log/mysql
sudo chmod 710 /var/log/mysql

sudo touch /var/log/wtmp /var/log/btmp
sudo chown root.utmp /var/log/wtmp /var/log/btmp

sudo rm /etc/ssh/ssh_host*key*

history -c
rm -rf ${HOME}/{.ssh,.cache,.bash_history,.mysql_history,.sudo_as_admin_successful,.viminfo}
touch ${HOME}/.bash_history

sudo shutdown -h now
