CREATE DATABASE IF NOT EXISTS phpmonitor DEFAULT CHARSET utf8;

CREATE USER 'phpmonitor'@'%' IDENTIFIED BY 'phpmonitor';
GRANT ALL PRIVILEGES ON phpmonitor.* TO 'phpmonitor'@'%';

FLUSH PRIVILEGES;
