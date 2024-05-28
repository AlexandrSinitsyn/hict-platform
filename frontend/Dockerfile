ARG NGINX_VERSION

FROM node:lts-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY HiCT_WebUI/src HiCT_WebUI/src
COPY index.html index.html
COPY .eslintrc.cjs .eslintignore .prettierrc.json /app/
COPY tsconfig.app.json tsconfig.node.json tsconfig.json env.d.ts /app/
COPY vite.config.ts vite.config.ts
COPY public public
COPY src src

# FIXME
RUN npm run build-only

FROM nginx:${NGINX_VERSION}-alpine-perl

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN echo 'vars="\${PROJECT_VERSION},\${APP_HOST},\${APP_PORT}"; \
    envsubst $vars < /usr/share/nginx/html/assets/env.template.json > /usr/share/nginx/html/assets/env.json; \
    nginx -g "daemon off;"' > /run.sh
RUN chmod +x /run.sh && chown -R nginx:nginx /run.sh

CMD ["/run.sh"]
