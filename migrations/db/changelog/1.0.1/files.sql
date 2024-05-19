--liquibase formatted sql

--changeset AlexSin:4
--Files and wrappers

create type sequence_level_type as enum ('contigs', 'scaffolds', 'chromosomes');

create table files
(
    file_id          bigint                   not null generated always as identity,
    filename         varchar(256)             not null,
    sequence_level   sequence_level_type      not null,
    file_size        bigint                   not null,
    visibility_group bigint                   not null,
    creation_time    timestamp with time zone not null default now(),
    primary key (file_id),
    unique (file_id),
    foreign key (visibility_group) references groups (group_id)
);

create index file_by_id on files using hash (file_id);
create unique index file_by_filename on files using btree (filename, file_id);
create unique index file_by_group on files using btree (sequence_level, file_id);
create unique index file_by_sequence_level on files using btree (visibility_group, file_id);

create table files_hict
(
    file_id         bigint not null,
    min_resolutions bigint default null,
    max_resolutions bigint default null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_hict_by_id on files_hict using hash (file_id);

create table files_tracks
(
    file_id bigint not null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_tracks_by_id on files_tracks using hash (file_id);

create table files_mcool
(
    file_id         bigint not null,
    min_resolutions bigint default null,
    max_resolutions bigint default null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_mcool_by_id on files_mcool using hash (file_id);

create table files_agp
(
    file_id bigint not null,
    unique (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_agp_by_id on files_agp using hash (file_id);

create table files_fasta
(
    file_id bigint not null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_fasta_by_id on files_fasta using hash (file_id);

--rollback drop index file_by_id
--rollback drop index file_by_filename
--rollback drop index file_by_group
--rollback drop index file_by_sequence_level
--rollback drop index files_hict_by_id
--rollback drop index files_tracks_by_id
--rollback drop index files_mcool_by_id
--rollback drop index files_agp_by_id
--rollback drop index files_fasta_by_id
--rollback drop table files_hict
--rollback drop table files_tracks
--rollback drop table files_mcool
--rollback drop table files_agp
--rollback drop table files_fasta
--rollback drop table files
--rollback drop type sequence_level_type
