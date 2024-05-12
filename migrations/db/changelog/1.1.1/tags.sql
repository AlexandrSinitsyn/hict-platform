--liquibase formatted sql

--changeset AlexSin:9
--Contact map tags

create table tags
(
    tag_id        bigint                   not null generated always as identity,
    tag_name      varchar(32)              not null,
    creation_time timestamp with time zone not null default now(),
    primary key (tag_id),
    unique (tag_id),
    unique (tag_name)
);

create index tags_by_id on tags using hash (tag_id);
create unique index tags_by_name on tags using btree (tag_name, tag_id);

create table contact_map_tags
(
    contact_map_id bigint not null,
    tag_id         bigint not null,
    primary key (contact_map_id, tag_id),
    unique (contact_map_id, tag_id),
    foreign key (contact_map_id) references contact_maps (contact_map_id),
    foreign key (tag_id) references tags (tag_id)
);

create unique index tags_by_contact_map on contact_map_tags using btree (contact_map_id, tag_id);

--rollback drop index tags_by_id
--rollback drop index tags_by_name
--rollback drop index tags_by_contact_map
--rollback drop table contact_map_tags
--rollback drop table tags
