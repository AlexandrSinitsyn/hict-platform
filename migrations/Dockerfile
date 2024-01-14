FROM liquibase/liquibase:4.25.1-alpine

USER root

RUN apk add gettext

COPY db/changelog /liquibase/db/changelog

WORKDIR /liquibase

COPY liquibase.properties liquibase.properties.template

RUN echo 'envsubst < liquibase.properties.template > liquibase.docker.properties && docker-entrypoint.sh $@' > run.sh && \
    chmod +x run.sh

RUN chown -R liquibase:liquibase /liquibase

USER liquibase

CMD ["update"]

ENTRYPOINT ["bash", "run.sh"]