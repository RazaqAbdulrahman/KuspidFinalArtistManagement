import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock

from app.main import app

client = TestClient(app)


class TestHealthEndpoint:
    """Tests for the /health endpoint"""
    
    def test_health_returns_200(self):
        """Should return 200 OK with service status"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "UP"
        assert response.json()["service"] == "email-service"


class TestSendEmailEndpoint:
    """Tests for the /api/emails/send endpoint"""
    
    def test_send_email_queues_successfully(self):
        """Should return success when email is queued"""
        response = client.post(
            "/api/emails/send",
            json={
                "to": "test@example.com",
                "subject": "Test Subject",
                "template_name": "welcome",
                "template_data": {"name": "Test User"}
            }
        )
        
        assert response.status_code == 200
        assert "queued" in response.json()["message"].lower()
    
    def test_send_email_invalid_email(self):
        """Should return 422 for invalid email format"""
        response = client.post(
            "/api/emails/send",
            json={
                "to": "invalid-email",
                "subject": "Test",
                "template_name": "welcome",
                "template_data": {}
            }
        )
        
        assert response.status_code == 422
    
    def test_send_email_missing_fields(self):
        """Should return 422 when required fields are missing"""
        response = client.post(
            "/api/emails/send",
            json={"to": "test@example.com"}
        )
        
        assert response.status_code == 422


class TestEmailTemplates:
    """Tests for email template handling"""
    
    def test_template_data_passed_correctly(self):
        """Should accept template data for personalization"""
        response = client.post(
            "/api/emails/send",
            json={
                "to": "artist@kuspid.com",
                "subject": "Welcome to Kuspid",
                "template_name": "artist_welcome",
                "template_data": {
                    "artist_name": "DJ Test",
                    "signup_date": "2026-01-06"
                }
            }
        )
        
        assert response.status_code == 200
