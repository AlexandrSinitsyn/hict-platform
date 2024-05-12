--liquibase formatted sql

--changeset AlexSin:5
--Biosamples and species

create table species
(
    species_id   bigint       not null generated always as identity,
    tax_id       varchar(100) not null,
    species_name varchar(100) not null,
    primary key (species_id),
    unique (species_id),
    unique (tax_id),
    unique (species_name)
);

create index species_by_id on species using hash (species_id);
create index species_by_tax_id on species using hash (tax_id);
create unique index species_by_name on species using btree (species_name, species_id);

create table biosamples
(
    biosample_id  bigint                   not null generated always as identity,
    species_id    bigint                   not null,
    description   text                     not null,
    creation_time timestamp with time zone not null default now(),
    primary key (biosample_id),
    unique (biosample_id),
    unique (species_id),
    foreign key (species_id) references species (species_id)
);

create index biosamples_by_id on biosamples using hash (biosample_id);

--rollback drop index species_by_id
--rollback drop index species_by_tax_id
--rollback drop index species_by_name
--rollback drop index biosamples_by_id
--rollback drop table biosamples
--rollback drop table species
