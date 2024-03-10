ARG NGINX_VERSION

FROM nginx:${NGINX_VERSION}-alpine-perl

COPY general.conf /etc/nginx/nginxconfig.io/general.conf
COPY security.conf /etc/nginx/nginxconfig.io/security.conf
COPY proxy.conf /etc/nginx/nginxconfig.io/proxy.conf

COPY hict.template.conf /hict.template.conf
COPY cluster.template.conf /cluster.template.conf

COPY nginx.conf /etc/nginx/nginx.conf

RUN chown -R nginx:nginx /etc/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /hict.template.conf && \
    chown -R nginx:nginx /cluster.template.conf

RUN touch /var/run/nginx.pid && \
        chown -R nginx:nginx /var/run/nginx.pid

RUN rm -rf /etc/nginx/conf.d/default.conf

RUN echo 'vars="\${FRONTEND_HOST},\${AUTHORIZATION_HOST},\${SERVER_HOST},\${HICT_SERVER_PORT}"; \
    envsubst $vars < /hict.template.conf > /etc/nginx/conf.d/hict.conf; \
    envsubst $vars < /cluster.template.conf > /etc/nginx/conf.d/cluster.conf; \
    nginx -g "daemon off;"' > /run.sh
RUN chmod +x /run.sh && chown -R nginx:nginx /run.sh

USER nginx

CMD ["/run.sh"]
