DROP DATABASE IF EXISTS medici;
CREATE DATABASE medici;
USE medici;

CREATE TABLE `blb` (
  `bid` bigint(20) NOT NULL,
  `bda` longblob,
  PRIMARY KEY  (`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sym` (
  `uid` bigint(20) NOT NULL auto_increment,
  `hsh` varchar(32) default NULL,
  `sym` text character set utf8,
  PRIMARY KEY  (`uid`),
  UNIQUE KEY `hsh` (`hsh`)
) ENGINE=InnoDB AUTO_INCREMENT=1221 DEFAULT CHARSET=utf8;

CREATE TABLE `tup` (
  `sub` bigint(20) NOT NULL,
  `pre` bigint(20) NOT NULL,
  `obj` bigint(20) NOT NULL,
  `typ` bigint(20) default NULL,
  UNIQUE KEY `sub` (`sub`,`pre`,`obj`),
  KEY `sub_2` (`sub`),
  KEY `pre` (`pre`),
  KEY `obj` (`obj`),
  KEY `sub_3` (`sub`,`pre`),
  KEY `sub_4` (`sub`,`obj`),
  KEY `pre_2` (`pre`,`obj`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

GRANT ALL ON medici.* TO 'medici'@'localhost' IDENTIFIED BY 'medici';

