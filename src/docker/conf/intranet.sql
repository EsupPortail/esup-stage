CREATE DATABASE IF NOT EXISTS intranet;

CREATE USER 'intranet_user'@'%' IDENTIFIED BY 'intranet_user';
GRANT ALL PRIVILEGES ON intranet.* TO 'intranet_user'@'%';

FLUSH PRIVILEGES;
