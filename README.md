### root 계정으로 접속하여 사용자 계정과 DB 생성
```javascript
mysql -u root –p
MariaDB [(none)]> show databases;
MariaDB [(none)]> use mysql;
MariaDB [mysql]> create database emp_db; 
MariaDB [mysql]> CREATE USER 'boot'@'%' IDENTIFIED BY 'boot';
MariaDB [mysql]> GRANT ALL PRIVILEGES ON emp_db.* TO 'boot'@'%';
MariaDB [mysql]> flush privileges; 
MariaDB [mysql]> select user, host from user;
MariaDB [mysql]> exit;

# boot 사용자 계정으로 접속한다.
mysql -u boot –p
boot 입력
MariaDB [mysql]> use emp_db
```