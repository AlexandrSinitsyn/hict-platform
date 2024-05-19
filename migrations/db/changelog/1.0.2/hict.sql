--liquibase formatted sql

--changeset AlexSin:6
--Contact maps

create table contact_maps
(
    contact_map_id   bigint                   not null generated always as identity,
    contact_map_name varchar(100)             not null,
    hict_id          bigint                            default null,
    tracks_id        bigint                            default null,
    mcool_id         bigint                            default null,
    experiment_id    bigint                   not null,
    species_id       bigint                   not null,
    biosample_id     bigint                            default null,
    hic_data_link    varchar(512)             not null,
    description      text                     not null,
    creation_time    timestamp with time zone not null default now(),
    primary key (contact_map_id),
    unique (contact_map_id),
    unique (contact_map_name),
    foreign key (hict_id) references files_hict (hict_id),
    foreign key (tracks_id) references files_tracks (tracks_id),
    foreign key (mcool_id) references files_mcool (mcool_id),
    foreign key (experiment_id) references experiments (experiment_id),
    foreign key (species_id) references species (species_id),
    foreign key (biosample_id) references biosamples (biosample_id)
);

create index contact_maps_by_id on contact_maps using hash (contact_map_id);
create unique index contact_maps_by_name on contact_maps using btree (contact_map_name, contact_map_id);
create unique index contact_maps_by_experiment on contact_maps using btree (experiment_id, contact_map_id);
create unique index contact_maps_by_species on contact_maps using btree (species_id, contact_map_id);

create table experiments
(
    experiment_id   bigint                   not null generated always as identity,
    experiment_name varchar(100)             not null,
    description     text                     not null,
    agp_id          bigint                            default null,
    fasta_id        bigint                            default null,
    user_id         bigint                   not null,
    attribution     varchar(256)             not null,
    paper           varchar(512)             not null,
    creation_time   timestamp with time zone not null default now(),
    primary key (experiment_id),
    unique (experiment_id),
    unique (experiment_name),
    foreign key (user_id) references users (user_id),
    foreign key (agp_id) references files_agp (agp_id),
    foreign key (fasta_id) references files_fasta (fasta_id)
);

create index experiments_by_id on experiments using hash (experiment_id);
create unique index experiments_by_name on experiments using btree (experiment_name, experiment_id);

--rollback drop index contact_maps_by_id
--rollback drop index contact_maps_by_name
--rollback drop index contact_maps_by_experiment
--rollback drop index contact_maps_by_species
--rollback drop index experiments_by_id
--rollback drop table experiments
--rollback drop table contact_maps
