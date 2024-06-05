--liquibase formatted sql

--changeset AlexSin:1
--Extensions and base types

create extension if not exists pgcrypto;

create extension if not exists "uuid-ossp";

--rollback true
