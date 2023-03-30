CREATE DATABASE IF NOT EXISTS osaxiswebsite;

CREATE USER 'osaxiswebsite'@'%' IDENTIFIED BY 'osaxiswebsite';
GRANT ALL PRIVILEGES ON osaxiswebsite.* TO 'osaxiswebsite'@'%';

FLUSH PRIVILEGES;
