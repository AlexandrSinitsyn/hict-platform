--liquibase formatted sql

--changeset AlexSin:5
--Biosamples and species

create table species
(
    tax_id       varchar(100) not null,
    species_name varchar(100) not null,
    primary key (tax_id),
    unique (tax_id),
    unique (species_name)
);
create unique index species_by_tax_id on species using btree (tax_id, species_name);
create unique index species_by_name on species using btree (species_name, tax_id);

create table biosamples
(
    biosample_id  bigint                   not null generated always as identity,
    tax_id        varchar(100)             not null,
    description   text                     not null,
    creation_time timestamp with time zone not null default now(),
    primary key (biosample_id),
    unique (biosample_id),
    foreign key (tax_id) references species (tax_id)
);

create index biosamples_by_id on biosamples using hash (biosample_id);

--rollback drop index species_by_id
--rollback drop index species_by_tax_id
--rollback drop index species_by_name
--rollback drop index biosamples_by_id
--rollback drop table biosamples
--rollback drop table species
