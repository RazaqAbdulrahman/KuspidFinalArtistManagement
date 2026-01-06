# Build Java services
FROM maven:3.9-eclipse-temurin-17 AS java-builder
WORKDIR /build/kuspid-backend
COPY kuspid-backend/ .
RUN mvn clean package -DskipTests

# Build Python services
FROM python:3.11-slim AS python-builder
WORKDIR /app
COPY kuspid-backend/services/ai-audio-service/requirements.txt ./ai-audio-service/
COPY kuspid-backend/services/email-service/requirements.txt ./email-service/
RUN pip install --user -r ./ai-audio-service/requirements.txt
RUN pip install --user -r ./email-service/requirements.txt

# Runtime
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y python3 supervisor curl libsndfile1 ffmpeg
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
