#
# Build stage
#
FROM maven:3.8.1-openjdk-11 AS builder
WORKDIR application
COPY src ./src
COPY pom.xml .
RUN mvn clean package
RUN mv target/vanilla-http-server-1.0-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

#
# Package stage
#
FROM openjdk:11-jre-slim
WORKDIR application
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
COPY --from=builder application/src/test/resources/sampledirectory ../basedir
# copy over application.yml as default config
COPY --from=builder application/src/main/resources/default-application.yml ./application.yml

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher" ]
EXPOSE 8080
