FROM gradle:jdk18-alpine AS build
ENV MODULE=scheduler

WORKDIR /home/build

USER root

COPY settings.gradle.kts settings.gradle.kts

COPY $MODULE/build.gradle.kts $MODULE/build.gradle.kts
COPY $MODULE/src $MODULE/src

RUN gradle clean

RUN gradle $MODULE:bootJar -x test

FROM docker:dind AS jar

USER root

WORKDIR /usr/local/bin

RUN apk add wget

# Liberica JDK
ARG JAVA_VERSION=19

RUN wget https://download.bell-sw.adamada8.ru/java/$JAVA_VERSION+37/bellsoft-jdk${JAVA_VERSION}+37-linux-x64-musl.tar.gz --no-check-certificate -t 1 -T 60 -O java.tar.gz && \
    tar -zxvf java.tar.gz && \
    rm -rf java.tar.gz

ENV JAVA_HOME=/usr/local/bin/jdk-$JAVA_VERSION/bin
ENV PATH=$PATH:$JAVA_HOME

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
