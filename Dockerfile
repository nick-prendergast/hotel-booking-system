FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/hotel-booking-system-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]