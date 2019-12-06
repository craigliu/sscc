CREATE TABLE IF NOT EXISTS cache
(
    id INT NOT NULL PRIMARY KEY auto_increment,
    `key` VARCHAR(128) not null,
    value VARCHAR(128),
    INDEX (`key`)
);