CREATE DATABASE IF NOT EXISTS fintraxbo;

CREATE USER 'fintraxbo'@'%' IDENTIFIED BY 'fintraxbo';
GRANT ALL PRIVILEGES ON fintraxbo.* TO 'fintraxbo'@'%';

FLUSH PRIVILEGES;
