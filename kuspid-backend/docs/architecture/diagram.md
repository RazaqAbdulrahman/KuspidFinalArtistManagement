# Kuspid Backend Architecture

```mermaid
graph TD
    Client[Client Browser/Mobile] --> Gateway[API Gateway :8080]
    
    subgraph "Kuspid Microservices"
        Gateway --> AuthSvc[Auth Service :8081]
        Gateway --> BeatSvc[Beat Service :8082]
        Gateway --> ArtistSvc[Artist Service :8083]
        Gateway --> AnalyticsSvc[Analytics Service :8084]
        Gateway --> EmailSvc[Email Service :8085]
        Gateway --> AISvc[AI Audio Service :8086]
    end

    subgraph "Data & Storage"
        AuthSvc --> DB[(PostgreSQL)]
        BeatSvc --> DB
        ArtistSvc --> DB
        AnalyticsSvc --> DB
        
        BeatSvc --> MinIO[(MinIO S3)]
        AISvc --> MinIO
    end

    BeatSvc -- Circuit Breaker --> AISvc
    ArtistSvc -- Async/Fire-and-Forget --> EmailSvc
```

## Key Features
- **Logical Independence**: Each service has its own bounded context and schema.
- **Fault Isolation**: Resilience4j circuit breakers prevent cascading failures.
- **Unified Deployment**: Optimized for Render free tier via Supervisord in a single container.
- **Security**: JWT-based authentication validated at the Gateway.
- **AI-Powered**: librosa integration for BPM and Key detection.
