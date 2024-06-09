ARG JAVA_VERSION=19
ARG HICT_SERVER_PORT=5000

FROM alpine:3.19 AS build

ARG JAVA_VERSION

WORKDIR /build

RUN apk add --no-cache wget
RUN wget https://download.oracle.com/java/$JAVA_VERSION/archive/jdk-${JAVA_VERSION}_linux-x64_bin.tar.gz --no-check-certificate -t 1 -T 60 -O java.tar.gz
RUN wget https://github.com/ctlab/HiCT_JVM/releases/download/latest-master/hict_server-1.0.35-3f1dbe3-webui_b651d13-fat.jar  --no-check-certificate -t 1 -T 60 -O hict.jar

FROM ubuntu:24.04

ARG JAVA_VERSION

WORKDIR /home/app

COPY --from=build /build/java.tar.gz /home/app/java$JAVA_VERSION.tar.gz

RUN tar -zxvf java$JAVA_VERSION.tar.gz

ENV PATH=$PATH:/home/app/jdk-$JAVA_VERSION/bin

COPY --from=build /build/hict.jar ./hict.jar

ENV DATA_DIR=/data
ENV VXPORT=$HICT_SERVER_PORT
ENV TILE_SIZE=256

EXPOSE $VXPORT

ENTRYPOINT ["java", "-jar", "hict.jar"]
