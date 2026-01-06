# Build Java services (Cache-optimized)
FROM maven:3.9-eclipse-temurin-17 AS java-builder
WORKDIR /build/kuspid-backend
# Copy POMs only first for better caching
COPY kuspid-backend/pom.xml .
COPY kuspid-backend/gateway/pom.xml ./gateway/
COPY kuspid-backend/services/auth-service/pom.xml ./services/auth-service/
COPY kuspid-backend/services/beat-service/pom.xml ./services/beat-service/
COPY kuspid-backend/services/artist-service/pom.xml ./services/artist-service/
COPY kuspid-backend/services/analytics-service/pom.xml ./services/analytics-service/
# Resolve dependencies offline
RUN mvn dependency:go-offline -B -DskipTests
# Copy source and build
COPY kuspid-backend/ .
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.main.skip=false -B

# Build Python services (Smaller image, no-cache)
FROM python:3.11-slim AS python-builder
WORKDIR /app
COPY kuspid-backend/services/ai-audio-service/requirements.txt ./ai-audio-service/
COPY kuspid-backend/services/email-service/requirements.txt ./email-service/
RUN pip install --user --no-cache-dir -r ./ai-audio-service/requirements.txt
RUN pip install --user --no-cache-dir -r ./email-service/requirements.txt

# Runtime (Slimmer base if possible, clean apt)
FROM eclipse-temurin:17-jre
# Install dependencies and clean up in one layer
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \ 
    supervisor \
    curl \
    libsndfile1 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

WORKDIR /app

# Copy Java jars
COPY --from=java-builder /build/kuspid-backend/gateway/target/*.jar ./gateway.jar
COPY --from=java-builder /build/kuspid-backend/services/auth-service/target/*.jar ./auth-service.jar
COPY --from=java-builder /build/kuspid-backend/services/beat-service/target/*.jar ./beat-service.jar
COPY --from=java-builder /build/kuspid-backend/services/artist-service/target/*.jar ./artist-service.jar
COPY --from=java-builder /build/kuspid-backend/services/analytics-service/target/*.jar ./analytics-service.jar

# Copy Python requirements and app
COPY --from=python-builder /root/.local /root/.local
ENV PATH=/root/.local/bin:$PATH

COPY kuspid-backend/services/ai-audio-service/ ./ai-audio-service/
COPY kuspid-backend/services/email-service/ ./email-service/

# Copy supervisor config
COPY kuspid-backend/infrastructure/supervisor/supervisord.conf /etc/supervisor/

EXPOSE 8080

CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
