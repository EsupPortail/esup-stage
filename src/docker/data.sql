CREATE DATABASE IF NOT EXISTS USER_APP;

USE USER_APP;

CREATE TABLE users(
    UserID int NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    PRIMARY KEY(UserID)
);

INSERT INTO users(name, email) VALUES ("toto", "ledéglingo@mail.com");
INSERT INTO users(name, email) VALUES ("popo", "lerigolo@mail.com");
INSERT INTO users(name, email) VALUES ("coco", "lasticot@mail.com");
INSERT INTO users(name, email) VALUES ("mamie", "traillette@mail.com");