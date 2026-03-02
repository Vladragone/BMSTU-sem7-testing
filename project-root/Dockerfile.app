FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -B -DskipTests package spring-boot:repackage

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/bd_kur-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
