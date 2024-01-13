ARG POSTGRES_VERSION

FROM postgres:${POSTGRES_VERSION}-alpine

COPY init.sql /docker-entrypoint-initdb.d/
