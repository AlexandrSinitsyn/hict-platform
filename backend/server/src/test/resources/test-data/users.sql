--liquibase formatted sql

--changeset AlexSin:-1
--Add test user data

select new_user('user', 'user', 'user@test.com', 'user'),
       new_user('test', 'test', 'test@test.com', 'test') ;

--rollback truncate table users
