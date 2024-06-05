--liquibase formatted sql

--changeset AlexSin:3
--Users and groups

create table users
(
    user_id                   uuid                     not null default gen_random_uuid(),
    username                  varchar(100)             not null,
    login                     varchar(64)              not null,
    email                     varchar(100)             not null,
    password                  varchar(64)              not null,
    visualization_settings_id uuid                              default null,
    creation_time             timestamp with time zone not null default now(),
    primary key (user_id),
    unique (user_id),
    unique (login),
    unique (email),
    unique (visualization_settings_id)
);

create index user_by_id on users using hash (user_id);
create unique index user_by_login on users using btree (login, user_id);
create unique index user_by_email on users using btree (email, user_id);

create table groups
(
    group_id      uuid                     not null default gen_random_uuid(),
    group_name    varchar(256)             not null,
    affiliation   varchar(256),
    creation_time timestamp with time zone not null default now(),
    primary key (group_id),
    unique (group_id),
    unique (group_name)
);

create index group_by_id on groups using hash (group_id);
create unique index group_by_name on groups using btree (group_name, group_id);

create table user_groups
(
    user_id    uuid                     not null,
    group_id   uuid                     not null,
    setup_date timestamp with time zone not null default now(),
    primary key (user_id, group_id),
    unique (user_id, group_id),
    foreign key (user_id) references users (user_id),
    foreign key (group_id) references groups (group_id)
);

create unique index user_by_group on user_groups using btree (group_id, user_id);
create unique index group_by_user on user_groups using btree (user_id, group_id);

--rollback drop index user_by_id
--rollback drop index user_by_login
--rollback drop index user_by_email
--rollback drop index group_by_id
--rollback drop index group_by_name
--rollback drop index user_by_group
--rollback drop index group_by_user
--rollback drop table users_groups
--rollback drop table groups
--rollback drop table users
