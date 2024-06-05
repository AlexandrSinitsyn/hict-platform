--liquibase formatted sql

--changeset AlexSin:4
--Files and wrappers

create type sequence_level_type as enum ('CONTIG', 'SCAFFOLD', 'CHROMOSOME');

create table files
(
    file_id        uuid                     not null default gen_random_uuid(),
    filename       varchar(256)             not null,
    sequence_level sequence_level_type      not null,
    file_size      bigint                   not null,
    creation_time  timestamp with time zone not null default now(),
    primary key (file_id),
    unique (file_id)
);

create index file_by_id on files using hash (file_id);
create unique index file_by_filename on files using btree (filename, file_id);
create unique index file_by_sequence_level on files using btree (sequence_level, file_id);

create table files_hict
(
    file_id         uuid not null,
    min_resolutions bigint default null,
    max_resolutions bigint default null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_hict_by_id on files_hict using hash (file_id);

create table files_mcool
(
    file_id         uuid not null,
    min_resolutions bigint default null,
    max_resolutions bigint default null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_mcool_by_id on files_mcool using hash (file_id);

create table files_agp
(
    file_id uuid not null,
    unique (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_agp_by_id on files_agp using hash (file_id);

create table tracks_types
(
    tracks_type_id   uuid         not null default gen_random_uuid(),
    tracks_type_name varchar(100) not null,
    primary key (tracks_type_id),
    unique (tracks_type_id),
    unique (tracks_type_name)
);

create index tracks_type_by_id on tracks_types using hash (tracks_type_id);
create unique index tracks_type_by_name on tracks_types using btree (tracks_type_name, tracks_type_id);

create table files_tracks
(
    file_id        uuid not null,
    description    text,
    data_source    varchar(100),
    tracks_type_id uuid,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id),
    foreign key (tracks_type_id) references tracks_types (tracks_type_id)
);

create index files_tracks_by_id on files_tracks using hash (file_id);

create table files_fasta
(
    file_id uuid not null,
    primary key (file_id),
    unique (file_id),
    foreign key (file_id) references files (file_id)
);

create index files_fasta_by_id on files_fasta using hash (file_id);

--rollback drop index tracks_type_by_name
--rollback drop index tracks_type_by_id
--rollback drop index files_hict_by_id
--rollback drop index files_tracks_by_id
--rollback drop index files_mcool_by_id
--rollback drop index files_agp_by_id
--rollback drop index files_fasta_by_id
--rollback drop index file_by_id
--rollback drop index file_by_filename
--rollback drop index file_by_sequence_level
--rollback drop table files_hict
--rollback drop table files_tracks
--rollback drop table files_mcool
--rollback drop table files_agp
--rollback drop table files_fasta
--rollback drop table tracks_types
--rollback drop table files
--rollback drop type sequence_level_type
