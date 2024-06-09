--liquibase formatted sql

--changeset AlexSin:-1
--Add test user data

select new_user('anonymous', 'anonymous', 'anonymous@test.com', 'anonymous'),
       new_user('user', 'user', 'user@test.com', 'user'),
       new_user('admin', 'admin', 'admin@test.com', 'admin'),
       new_user('superuser', 'superuser', 'superuser@test.com', 'superuser') ;

--rollback truncate table users
