#!/bin/sh

 export MYSQL_PWD=$MYSQL_ROOT_PASSWORD
 mysql -u root -h localhost < /opt/scripts/$1