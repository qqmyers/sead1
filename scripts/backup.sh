#!/bin/bash

# Number of backups to keep
MAX_BACKUP=16

# hostname
HOSTNAME=machine

# remote folder
FOLDER=/home/mmdb

# folder with all backups
BACKUP=/home/kooper/backup

# change to backup folder
if [ ! -e $BACKUP/$HOSTNAME ]; then
  mkdir -p $BACKUP/$HOSTNAME
fi
cd $BACKUP/$HOSTNAME

# delete old backups
i=$MAX_BACKUP;
while [ -e backup.$i ]; do
  rm -rf backup.$i
  let i++
done

# shift old backups
i=$(( MAX_BACKUP - 1))
while (( $i > 1 )); do
  if [ -e backup.$i ]; then
    mv backup.$i backup.$(( i + 1 ))
  fi
  let i--
done

# copy latest backup (using hardlinks)
if [ -e backup.1 ]; then
  cp -al backup.1 backup.2
fi

# next two command assume ssh keys are setup and no password

# backup database.
# Add following grant to database
#  GRANT SELECT, LOCK TABLES ON dbname.* TO backup@'%';
# make sure user can write to remote folder
ssh kooper@$HOSTNAME "/usr/bin/mysqldump -u backup mmdb | gzip > $FOLDER/database/mmdb.sql.gz"

# copy everything to local machine
rsync -avz --delete kooper@$HOSTNAME:$FOLDER/ backup.1/
