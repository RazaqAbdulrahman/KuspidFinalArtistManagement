#!/bin/bash

SERVICES=("gateway:8080" "auth-service:8081" "beat-service:8082" "artist-service:8083" "analytics-service:8084" "email-service:8085" "ai-audio-service:8086")

echo "Checking Kuzpid Microservices Health..."

for service in "${SERVICES[@]}"; do
    NAME=$(echo $service | cut -d':' -f1)
    PORT=$(echo $service | cut -d':' -f2)
    
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health 2>/dev/null || curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/health 2>/dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        echo "✅ $NAME is UP (Port $PORT)"
    else
        echo "❌ $NAME is DOWN (Port $PORT) - Response: $RESPONSE"
    fi
done
