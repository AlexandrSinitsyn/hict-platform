FROM gradle:jdk18 AS build
ENV MODULE=scheduler

WORKDIR /home/build

USER root

COPY settings.gradle.kts settings.gradle.kts

COPY proto/build.gradle.kts proto/build.gradle.kts
COPY proto/src proto/src

COPY entity/build.gradle.kts entity/build.gradle.kts
COPY entity/src entity/src

COPY $MODULE/build.gradle.kts $MODULE/build.gradle.kts
COPY $MODULE/src $MODULE/src

RUN gradle clean

RUN gradle $MODULE:bootJar -x test

FROM docker:24.0.7-alpine3.19 AS jar

USER root

WORKDIR /usr/local/bin

RUN apk add wget

# Liberica JDK
ARG JAVA_VERSION=19

RUN wget https://download.bell-sw.adamada8.ru/java/$JAVA_VERSION+37/bellsoft-jdk$JAVA_VERSION+37-linux-x64-musl.apk --no-check-certificate -t 1 -T 60 -O java.apk && \
    apk add --allow-untrusted java.apk

ENV MODULE=scheduler

WORKDIR /home/app

COPY --from=build /home/build/$MODULE/build/libs/$MODULE.jar /usr/local/lib/app.jar

ENV USER=$MODULE

RUN addgroup -S $USER && adduser -SHD $USER -G $USER && \
    chmod -R 777 /home/app && \
    chown -R $USER:$USER /home/app && \
    chown -R $USER:$USER /usr/local/lib/app.jar

USER $USER

ENTRYPOINT ["java", \
    "-ea", "-Duser.country=RU", "-Duser.language=ru", \
    "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8", \
    "-jar", "/usr/local/lib/app.jar"]
