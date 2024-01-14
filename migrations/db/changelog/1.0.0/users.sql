--liquibase formatted sql

--changeset AlexSin:2
--Viewer users

create user viewer with
    login
    nosuperuser
    nocreatedb
    nocreaterole
    noinherit
    noreplication
    password '${POSTGRES_VIEWER_PASSWORD}';
grant usage on schema public to viewer;
grant select on all tables in schema public to viewer;
alter default privileges in schema public grant select on tables to viewer;

--rollback truncate table
