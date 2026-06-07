FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY shared shared
COPY server server
COPY client client

RUN chmod +x mvnw && ./mvnw -q -pl server -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /workspace/server/target/disciplica-server-1.0-SNAPSHOT.jar app.jar
COPY server/docker-entrypoint.sh docker-entrypoint.sh
RUN chmod +x docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]
