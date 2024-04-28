ARG NGINX_VERSION

FROM node:lts-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

ARG PROJECT_VERSION
ARG AUTH_HOST
ARG SERVER_HOST
ARG HICT_CLUSTER_HOST

ENV VITE_PROJECT_VERSION=$PROJECT_VERSION
ENV VITE_AUTH_HOST=$AUTH_HOST
ENV VITE_SERVER_HOST=$SERVER_HOST
ENV VITE_HICT_CLUSTER_HOST=$HICT_CLUSTER_HOST

RUN npm run build

FROM nginx:${NGINX_VERSION}-alpine-perl

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
