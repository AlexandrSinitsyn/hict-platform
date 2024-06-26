--liquibase formatted sql

--changeset AlexSin:6
--Contact maps

create table experiments
(
    experiment_id   uuid                     not null default gen_random_uuid(),
    experiment_name varchar(100)             not null,
    description     text,
    user_id         uuid                     not null,
    group_id        uuid                     not null,
    acknowledgement varchar(256),
    paper           varchar(512),
    creation_time   timestamp with time zone not null default now(),
    primary key (experiment_id),
    unique (experiment_id),
    unique (experiment_name),
    foreign key (user_id) references users (user_id)
);

create index experiments_by_id on experiments using hash (experiment_id);
create unique index experiments_by_name on experiments using btree (experiment_name, experiment_id);

create table experiment_fasta
(
    experiment_id uuid not null,
    file_id       uuid not null,
    primary key (experiment_id, file_id),
    unique (experiment_id, file_id),
    foreign key (experiment_id) references experiments (experiment_id),
    foreign key (file_id) references files_fasta (file_id)
);

create unique index fasta_by_experiment on experiment_fasta using btree (experiment_id, file_id);

create table contact_maps
(
    contact_map_id   uuid                     not null default gen_random_uuid(),
    contact_map_name varchar(100)             not null,
    hict_id          uuid,
    mcool_id         uuid                              default null,
    experiment_id    uuid                     not null,
    reference        varchar(100),
    hic_source       varchar(100),
    biosample_id     uuid                              default null,
    hic_data_link    varchar(512),
    description      text,
    creation_time    timestamp with time zone not null default now(),
    primary key (contact_map_id),
    unique (contact_map_id),
    unique (contact_map_name),
    foreign key (hict_id) references files_hict (file_id),
    foreign key (mcool_id) references files_mcool (file_id),
    foreign key (experiment_id) references experiments (experiment_id),
    foreign key (reference) references species (tax_id),
    foreign key (hic_source) references species (tax_id),
    foreign key (biosample_id) references biosamples (biosample_id)
);

create index contact_maps_by_id on contact_maps using hash (contact_map_id);
create unique index contact_maps_by_name on contact_maps using btree (contact_map_name, contact_map_id);
create unique index contact_maps_by_experiment on contact_maps using btree (experiment_id, contact_map_id);
create unique index contact_maps_by_species on contact_maps using btree (reference, contact_map_id);

create table contact_map_agp
(
    contact_map_id uuid not null,
    file_id        uuid not null,
    primary key (contact_map_id, file_id),
    unique (contact_map_id, file_id),
    foreign key (contact_map_id) references contact_maps (contact_map_id),
    foreign key (file_id) references files_agp (file_id)
);

create unique index agp_by_contact_map on contact_map_agp using btree (contact_map_id, file_id);

create table contact_map_tracks
(
    contact_map_id uuid not null,
    file_id        uuid not null,
    primary key (contact_map_id, file_id),
    unique (contact_map_id, file_id),
    foreign key (contact_map_id) references contact_maps (contact_map_id),
    foreign key (file_id) references files_tracks (file_id)
);

create unique index tracks_by_contact_map on contact_map_tracks using btree (contact_map_id, file_id);

--rollback drop index tracks_by_contact_map
--rollback drop index agp_by_contact_map
--rollback drop index contact_maps_by_id
--rollback drop index contact_maps_by_name
--rollback drop index contact_maps_by_experiment
--rollback drop index contact_maps_by_species
--rollback drop index fasta_by_experiment
--rollback drop index experiments_by_id
--rollback drop table contact_map_agp
--rollback drop table contact_map_tracks
--rollback drop table contact_maps
--rollback drop table experiment_fasta
--rollback drop table experiments
