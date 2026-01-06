# Build Java Server (Monolith)
FROM maven:3.9-eclipse-temurin-17 AS java-builder
WORKDIR /build/kuspid-backend
# Copy POMs
COPY kuspid-backend/pom.xml .
COPY kuspid-backend/kuspid-server/pom.xml ./kuspid-server/
# Resolve dependencies
RUN mvn dependency:go-offline -B -DskipTests
# Copy source
COPY kuspid-backend/ .
# Build only the monolith module
RUN mvn clean package -pl kuspid-server -am -DskipTests -Dmaven.test.skip=true -B

# Build Python services
FROM python:3.11-slim AS python-builder
WORKDIR /app
COPY kuspid-backend/services/ai-audio-service/requirements.txt ./ai-audio-service/
COPY kuspid-backend/services/email-service/requirements.txt ./email-service/
RUN pip install --user --no-cache-dir -r ./ai-audio-service/requirements.txt
RUN pip install --user --no-cache-dir -r ./email-service/requirements.txt

# Runtime
FROM eclipse-temurin:17-jre
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

# Copy Consolidated Jar
COPY --from=java-builder /build/kuspid-backend/kuspid-server/target/*.jar ./kuspid-server.jar

# Copy Python requirements and app
COPY --from=python-builder /root/.local /root/.local
ENV PATH=/root/.local/bin:$PATH

COPY kuspid-backend/services/ai-audio-service/ ./ai-audio-service/
COPY kuspid-backend/services/email-service/ ./email-service/

# Copy supervisor config
COPY kuspid-backend/infrastructure/supervisor/supervisord.conf /etc/supervisor/

EXPOSE 8080

CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
