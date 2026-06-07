FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY shared shared
COPY server server

RUN chmod +x mvnw && ./mvnw -q -pl server -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /workspace/server/target/disciplica-server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
