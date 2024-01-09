ARG NGINX_VERSION

FROM nginx:${NGINX_VERSION}-alpine-perl

COPY general.conf /etc/nginx/nginxconfig.io/general.conf
COPY security.conf /etc/nginx/nginxconfig.io/security.conf
COPY proxy.conf /etc/nginx/nginxconfig.io/proxy.conf

COPY hict.template.conf /hict.template.conf

COPY nginx.conf /etc/nginx/nginx.conf

RUN chown -R nginx:nginx /etc/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /hict.template.conf

RUN touch /var/run/nginx.pid && \
        chown -R nginx:nginx /var/run/nginx.pid

RUN rm -rf /etc/nginx/conf.d/default.conf

USER nginx

CMD ["/bin/sh" , "-c" , "envsubst < /hict.template.conf > /etc/nginx/conf.d/hict.conf && exec nginx -g 'daemon off;'"]
