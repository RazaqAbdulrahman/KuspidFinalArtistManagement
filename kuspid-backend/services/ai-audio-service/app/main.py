from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import librosa
import numpy as np
import os
import subprocess
import boto3
from botocore.client import Config

app = FastAPI(title="Kuspid AI Audio Service")

class AnalysisRequest(BaseModel):
    s3Key: str

# MinIO Client
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://localhost:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")
MINIO_BUCKET = os.getenv("MINIO_BUCKET", "beats")

s3 = boto3.client(
    "s3",
    endpoint_url=MINIO_ENDPOINT,
    aws_access_key_id=MINIO_ACCESS_KEY,
    aws_secret_access_key=MINIO_SECRET_KEY,
    config=Config(signature_version="s3v4"),
    region_name="us-east-1" # dummy
)

def download_file(s3_key: str):
    local_path = f"/tmp/{s3_key}"
    os.makedirs(os.path.dirname(local_path), exist_ok=True)
    s3.download_file(MINIO_BUCKET, s3_key, local_path)
    return local_path

@app.post("/api/ai/analyze/full")
async def analyze_full(request: AnalysisRequest):
    try:
        # 1. Download file
        local_path = download_file(request.s3Key)
        
        # 2. Preprocess with FFmpeg (Downsample to 16kHz, Mono for speed)
        proxy_path = local_path + ".proxy.wav"
        subprocess.run([
            "ffmpeg", "-y", "-i", local_path, 
            "-ar", "16000", "-ac", "1", "-vn",
            proxy_path
        ], check=True, capture_output=True)
        
        # 3. Load lightweight audio
        y, sr = librosa.load(proxy_path, sr=16000)
        
        # 4. Analyze BPM
        tempo, _ = librosa.beat.beat_track(y=y, sr=sr)
        bpm = int(tempo.item()) if isinstance(tempo, np.ndarray) else int(tempo)
        
        # 5. Analyze Key
        chroma = librosa.feature.chroma_stft(y=y, sr=sr)
        key_index = np.argmax(np.mean(chroma, axis=1))
        keys = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
        detected_key = keys[key_index]
        
        # 6. Cleanup
        for path in [local_path, proxy_path]:
            if os.path.exists(path):
                os.remove(path)
            
        return {
            "bpm": bpm,
            "key": detected_key,
            "genre": "Hip Hop", # Placeholder or simple heuristic
            "status": "COMPLETED"
        }
    except Exception as e:
        print(f"Error analyzing audio: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health():
    return {"status": "UP", "service": "ai-audio-service"}
