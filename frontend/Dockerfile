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

ARG PROJECT_VERSION
ENV VITE_PROJECT_VERSION $PROJECT_VERSION

# FIXME
RUN npm run build-only

FROM nginx:${NGINX_VERSION}-alpine-perl

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
