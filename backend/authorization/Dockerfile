FROM gradle:jdk18-alpine AS build
ENV MODULE=authorization

WORKDIR /home/build

USER root

COPY settings.gradle.kts settings.gradle.kts

COPY entity/build.gradle.kts entity/build.gradle.kts
COPY entity/src entity/src

COPY $MODULE/build.gradle.kts $MODULE/build.gradle.kts
COPY $MODULE/src $MODULE/src

RUN gradle clean

RUN gradle $MODULE:bootJar -x test

FROM openjdk:19-alpine AS jar
ENV MODULE=authorization

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
