ARG POSTGRES_VERSION

FROM postgres:${POSTGRES_VERSION}-alpine

COPY init.sql /docker-entrypoint-initdb.d/
COPY users.sql /docker-entrypoint-initdb.d/

RUN for f in /docker-entrypoint-initdb.d/*.sql ;  \
    do envsubst < $f > $f ; \
    done
