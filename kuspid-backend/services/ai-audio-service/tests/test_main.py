import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import numpy as np

from app.main import app

client = TestClient(app)


class TestHealthEndpoint:
    """Tests for the /health endpoint"""
    
    def test_health_returns_200(self):
        """Should return 200 OK with service status"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "UP"
        assert response.json()["service"] == "ai-audio-service"


class TestAnalyzeEndpoint:
    """Tests for the /api/ai/analyze/full endpoint"""
    
    @patch('app.main.download_file')
    @patch('librosa.load')
    @patch('librosa.beat.beat_track')
    @patch('librosa.feature.chroma_stft')
    def test_analyze_full_success(self, mock_chroma, mock_beat, mock_load, mock_download):
        """Should return BPM and key when analysis succeeds"""
        # Mock file download
        mock_download.return_value = "/tmp/test.mp3"
        
        # Mock librosa.load
        mock_load.return_value = (np.zeros(44100), 44100)  # 1 second of silence
        
        # Mock beat detection (BPM = 120)
        mock_beat.return_value = (np.array([120.0]), np.array([]))
        
        # Mock chroma (key = C, index 0)
        mock_chroma.return_value = np.zeros((12, 100))
        mock_chroma.return_value[0, :] = 1.0  # C is strongest
        
        response = client.post(
            "/api/ai/analyze/full",
            json={"s3Key": "test-file.mp3"}
        )
        
        assert response.status_code == 200
        data = response.json()
        assert "bpm" in data
        assert "key" in data
        assert data["status"] == "COMPLETED"
    
    @patch('app.main.download_file')
    def test_analyze_full_file_not_found(self, mock_download):
        """Should return 500 when file download fails"""
        mock_download.side_effect = Exception("File not found")
        
        response = client.post(
            "/api/ai/analyze/full",
            json={"s3Key": "nonexistent-file.mp3"}
        )
        
        assert response.status_code == 500
    
    def test_analyze_full_missing_s3key(self):
        """Should return 422 when s3Key is missing"""
        response = client.post(
            "/api/ai/analyze/full",
            json={}
        )
        
        assert response.status_code == 422


class TestKeyDetection:
    """Tests for musical key detection logic"""
    
    @patch('app.main.download_file')
    @patch('librosa.load')
    @patch('librosa.beat.beat_track')
    @patch('librosa.feature.chroma_stft')
    def test_detects_c_major(self, mock_chroma, mock_beat, mock_load, mock_download):
        """Should detect C when C is the strongest chroma"""
        mock_download.return_value = "/tmp/test.mp3"
        mock_load.return_value = (np.zeros(44100), 44100)
        mock_beat.return_value = (np.array([120.0]), np.array([]))
        
        # C is index 0, make it strongest
        chroma = np.zeros((12, 100))
        chroma[0, :] = 1.0
        mock_chroma.return_value = chroma
        
        response = client.post("/api/ai/analyze/full", json={"s3Key": "test.mp3"})
        assert response.json()["key"] == "C"
    
    @patch('app.main.download_file')
    @patch('librosa.load')
    @patch('librosa.beat.beat_track')
    @patch('librosa.feature.chroma_stft')
    def test_detects_a_minor(self, mock_chroma, mock_beat, mock_load, mock_download):
        """Should detect A when A is the strongest chroma"""
        mock_download.return_value = "/tmp/test.mp3"
        mock_load.return_value = (np.zeros(44100), 44100)
        mock_beat.return_value = (np.array([120.0]), np.array([]))
        
        # A is index 9, make it strongest
        chroma = np.zeros((12, 100))
        chroma[9, :] = 1.0
        mock_chroma.return_value = chroma
        
        response = client.post("/api/ai/analyze/full", json={"s3Key": "test.mp3"})
        assert response.json()["key"] == "A"
