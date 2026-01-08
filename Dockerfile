# Build Java Server (Monolith)
FROM maven:3.9-eclipse-temurin-17 AS java-builder
WORKDIR /build/kuspid-backend
# Copy POMs
COPY kuspid-backend/pom.xml .
COPY kuspid-backend/kuspid-server/pom.xml ./kuspid-server/
COPY kuspid-backend/gateway/pom.xml ./gateway/
COPY kuspid-backend/services/auth-service/pom.xml ./services/auth-service/
COPY kuspid-backend/services/beat-service/pom.xml ./services/beat-service/
COPY kuspid-backend/services/artist-service/pom.xml ./services/artist-service/
COPY kuspid-backend/services/analytics-service/pom.xml ./services/analytics-service/
COPY kuspid-backend/services/email-service/pom.xml ./services/email-service/
# Resolve dependencies
RUN mvn -f kuspid-server/pom.xml dependency:go-offline -B -DskipTests
# Copy source
COPY kuspid-backend/ .
# Build only the monolith module
RUN mvn clean package -pl kuspid-server -am -DskipTests -Dmaven.test.skip=true -B

# Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy Consolidated Jar
COPY --from=java-builder /build/kuspid-backend/kuspid-server/target/*.jar ./kuspid-server.jar

EXPOSE 8080

CMD ["java", "-jar", "kuspid-server.jar"]
