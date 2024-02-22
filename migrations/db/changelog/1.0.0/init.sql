--liquibase formatted sql

--changeset AlexSin:1
--Users and Hi-C maps

create extension if not exists pgcrypto;

create table users
(
    user_id       bigint                   not null generated always as identity,
    username      varchar(100)             not null,
    login         varchar(64)              not null,
    email         varchar(100)             not null,
    role          smallint                 not null check ( 0 <= role and role <= 3 ),
    password      varchar(60)              not null,
    creation_time timestamp with time zone not null default now(),
    primary key (user_id),
    unique (user_id),
    unique (login),
    unique (email)
);

create index users_by_id on users using hash (user_id) ;
create unique index users_by_login on users using btree (login, user_id) ;
create unique index users_by_email on users using btree (email, user_id) ;

create table hi_c_maps
(
    hi_c_map_id   bigint                   not null generated always as identity,
    user_id       bigint                   not null,
    name          varchar(100)             not null,
    description   text                     not null,
    creation_time timestamp with time zone not null default now(),
    primary key (hi_c_map_id),
    unique (hi_c_map_id),
    unique (name),
    constraint fk_author_id foreign key (user_id) references users (user_id)
);

create index hi_c_map_by_id on hi_c_maps using hash (hi_c_map_id) ;
create index hi_c_map_by_name on hi_c_maps using hash (name) ;
create unique index hi_c_map_by_author on hi_c_maps using btree (user_id, hi_c_map_id) ;

--rollback truncate table
