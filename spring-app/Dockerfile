## syntax=docker/dockerfile:1
# syntax注释会带来docker build 的问题可能是为了去网络中寻找相应版本的解析文件导致的
FROM maven:3-openjdk-8
WORKDIR /app
# COPY ./.mvn .mvn
# COPY ./mvnw mvnw
# COPY ./src src
# COPY ./pom.xml pom.xml
# RUN ./mvnw dependency:go-offline
CMD ["./mvnw", "spring-boot:run", "-Dreactor.netty.http.server.accessLogEnabled=true"]