FROM grafana/grafana:10.2.3

USER root

RUN apk update && apk add curl

# INIT #
COPY grafana.ini /etc/grafana/grafana.ini

# SETTINGS #
COPY provisioning /etc/grafana/provisioning

# LOAD DASHBOARD #
COPY dashboards /var/lib/grafana/dashboards

ENV GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/var/lib/grafana/dashboards/grafana-hict.json

USER grafana

# PLUGINS #
RUN grafana-cli --pluginUrl https://github.com/cloudspout/cloudspout-button-panel/releases/download/7.0.23/cloudspout-button-panel.zip  \
    plugins install cloudspout-button-panel # buttons
RUN grafana-cli --pluginUrl https://github.com/yesoreyeram/yesoreyeram-boomtheme-panel/releases/download/v0.2.1/yesoreyeram-boomtheme-panel-0.2.1.zip  \
    plugins install yesoreyeram-boomtheme-panel # themes
RUN grafana-cli --pluginUrl https://grafana.com/api/plugins/ryantxu-ajax-panel/versions/0.1.0/download \
    plugins install ryantxu-ajax-panel # ajax

ENV GF_INSTALL_PLUGINS='cloudspout-button-panel,yesoreyeram-boomtheme-panel,ryantxu-ajax-panel'
