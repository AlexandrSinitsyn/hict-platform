FROM grafana/loki:2.9.3

USER root

WORKDIR /home/app

RUN apk update && apk add curl
RUN curl https://dl.min.io/client/mc/release/linux-amd64/mc --create-dirs -o /home/app/mc
RUN chmod +x /home/app/mc

COPY loki-config.yaml /etc/loki/loki-config.yml

RUN echo "#!/bin/sh" > /home/app/run.sh
RUN echo "/home/app/mc config host add minio http://\$MINIO_HOST:9000 \$MINIO_ROOT_USER \$MINIO_ROOT_PASSWORD" >> /home/app/run.sh
RUN echo "for b in loki-data loki-data-copy loki-ruler ; do (/home/app/mc ls minio | grep \$b >> /dev/null) ||  \
    (/home/app/mc mb minio/\$b && /home/app/mc policy set public minio/\$b) ; done" >> /home/app/run.sh
RUN echo "/usr/bin/loki \$@" >> /home/app/run.sh
RUN chmod +x /home/app/run.sh

RUN chown -R loki:loki /home/app

USER loki

CMD ["-config.expand-env=true", "-config.file=/etc/loki/loki-config.yml"]

ENTRYPOINT ["/home/app/run.sh"]
