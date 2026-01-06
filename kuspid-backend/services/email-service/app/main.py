from fastapi import FastAPI, BackgroundTasks, HTTPException
from pydantic import BaseModel, EmailStr
from typing import List, Optional
import aiosmtplib
from email.message import EmailMessage
from jinja2 import Environment, FileSystemLoader
import os

app = FastAPI(title="Kuspid Email Service")

# Setup Jinja2
template_dir = os.path.join(os.path.dirname(__file__), "templates")
if not os.path.exists(template_dir):
    os.makedirs(template_dir)
jinja_env = Environment(loader=FileSystemLoader(template_dir))

class EmailRequest(BaseModel):
    to: EmailStr
    subject: str
    template_name: str
    template_data: dict

SMTP_HOST = os.getenv("SMTP_HOST", "smtp.gmail.com")
SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
SMTP_USER = os.getenv("SMTP_USER", "")
SMTP_PASSWORD = os.getenv("SMTP_PASSWORD", "")

async def send_email_task(request: EmailRequest):
    try:
        template = jinja_env.get_template(f"{request.template_name}.html")
        html_content = template.render(**request.template_data)
        
        message = EmailMessage()
        message["From"] = SMTP_USER
        message["To"] = request.to
        message["Subject"] = request.subject
        message.add_alternative(html_content, subtype="html")
        
        await aiosmtplib.send(
            message,
            hostname=SMTP_HOST,
            port=SMTP_PORT,
            username=SMTP_USER,
            password=SMTP_PASSWORD,
            use_tls=False,
            start_tls=True
        )
    except Exception as e:
        print(f"Error sending email: {e}")

@app.post("/api/emails/send")
async def send_email(request: EmailRequest, background_tasks: BackgroundTasks):
    background_tasks.add_task(send_email_task, request)
    return {"message": "Email queued for delivery"}

@app.get("/health")
async def health():
    return {"status": "UP", "service": "email-service"}
