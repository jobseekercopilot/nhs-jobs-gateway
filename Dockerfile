FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:17-jre-alpine
# Upgrade the OpenSSL runtime packages to the CVE-2026-14456 fixed build.
RUN apk add --no-cache --upgrade \
    libcrypto3=3.5.8-r0 \
    libssl3=3.5.8-r0 \
    expat=2.8.4-r0 \
    openssl=3.5.8-r0

RUN apk add --no-cache wget \
    && adduser -S -D -H -u 10001 app
WORKDIR /app
COPY --from=build /workspace/target/nhs-jobs-gateway-1.0.0.jar app.jar
USER 10001
EXPOSE 8104
ENTRYPOINT ["java","-jar","/app/app.jar"]
