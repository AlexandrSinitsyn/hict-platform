--liquibase formatted sql

--changeset AlexSin:10
--Preloaded data

insert into groups (group_name) values ('public');

select new_user('anonymous', 'anonymous', 'anonymous@no.email', 'nopass');
