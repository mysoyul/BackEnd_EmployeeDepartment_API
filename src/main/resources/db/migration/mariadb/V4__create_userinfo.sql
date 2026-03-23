-- UserInfo 엔티티 테이블
-- Spring Boot SpringPhysicalNamingStrategy: UserInfo → user_info
CREATE TABLE user_info (
    id       INT          NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles    VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_userinfo_email (email)
) ENGINE=InnoDB;
