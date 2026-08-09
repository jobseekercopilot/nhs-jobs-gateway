FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget \
    && adduser -S -D -H -u 10001 app
WORKDIR /app
COPY --from=build /workspace/target/nhs-jobs-gateway-1.0.0.jar app.jar
USER 10001
EXPOSE 8104
ENTRYPOINT ["java","-jar","/app/app.jar"]
