FROM grafana/promtail:2.9.3

COPY promtail-config.yml /etc/promtail/promtail-config.yml

CMD ["-config.expand-env=true", "-config.file=/etc/promtail/promtail-config.yml"]

ENTRYPOINT ["/usr/bin/promtail"]
