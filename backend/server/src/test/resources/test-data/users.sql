--liquibase formatted sql

--changeset AlexSin:-1
--Add test user data

select new_user('anonymous', 'anonymous', 'anonymous@test.com', 'anonymous', 0),
       new_user('user', 'user', 'user@test.com', 'user', 1),
       new_user('admin', 'admin', 'admin@test.com', 'admin', 2),
       new_user('superuser', 'superuser', 'superuser@test.com', 'superuser', 3) ;

--rollback truncate table
