FROM maven:3.8.1-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /build/target/service-booking-*.jar service-booking.jar
RUN mkdir -p logs && chown -R appuser:appuser /app
USER appuser
EXPOSE 8003
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost:8003/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "service-booking.jar"]
