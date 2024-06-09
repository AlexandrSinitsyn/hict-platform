FROM docker:24.0.7-alpine3.19

WORKDIR /app

COPY singlecontainer/hict-*.tar /app/

COPY docker-compose.yml /app/docker-compose.yml
COPY docker-compose.logging.yml /app/docker-compose.logging.yml
COPY .env /app/.env
COPY hict-admin.env /app/hict-admin.env

RUN echo 'docker compose -f docker-compose.yml -p hict-platform up -d' > start.sh && \
    chmod +x start.sh

RUN echo 'for i in *.tar ; \
    do \
        docker load < \$i ; \
    done' > laod.sh && chmod +x load.sh

USER root

EXPOSE 80

#ENTRYPOINT ["./load.sh", "&&", "./start.sh"]
ENTRYPOINT ["tail", "-f", "/dev/null"]
