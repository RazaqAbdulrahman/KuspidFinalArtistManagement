# Kuspid Artist Management - Frontend Implementation Guide

This document provides all the necessary details to build a functional and premium React frontend for the Kuspid Artist Management platform, intended for use with AI frontend generation tools like Lovable.

## 1. Project Overview
Kuspid is a management platform for artists and a marketplace/library for beats. It allows managing artist profiles, uploading and tracking beats (stored via Cloudinary), and viewing analytics.

## 2. Technical Stack Recommendation
- **Framework**: Vite + React (TypeScript preferred)
- **Styling**: Tailwind CSS
- **Icons**: Lucide React
- **State Management**: React Query (TanStack Query) for API calls
- **Authentication**: JWT stored in `localStorage` or `HttpOnly` cookies.

## 3. API Contract (Backend Base URL: `https://kuspidfinalartistmanagement.onrender.com`)

### 3.1 Authentication (`/api/auth`)
- `POST /api/auth/register`: Create a new user account.
  - Body: `{ "fullName": "", "email": "", "password": "" }`
- `POST /api/auth/login`: Authenticate and get JWT.
  - Body: `{ "email": "", "password": "" }`
  - Response: `{ "accessToken": "JWT_HERE", "refreshToken": "JWT_HERE" }`

### 3.2 Artist Management (`/api/artists`)
- `GET /api/artists`: Fetch all artists.
- `GET /api/artists/{id}`: Get specific artist details.
- `POST /api/artists`: Create a new artist.
  - Body: `{ "name": "", "genre": "", "email": "", "bio": "" }`
- `PUT /api/artists/{id}`: Update artist profile.
- `POST /api/artists/{id}/notes`: Add a management note to an artist.
  - Body: `String content`

### 3.3 Beat Library (`/api/beats`)
- `GET /api/beats`: Fetch all uploaded beats.
- `GET /api/beats/{id}`: Get beat metadata.
- `POST /api/beats`: Upload a new beat.
  - **Note**: Uses `multipart/form-data`.
  - Fields: `title` (String), `artistId` (String), `file` (File/Blob).
- `DELETE /api/beats/{id}`: Remove a beat.

### 3.4 Analytics (`/api/analytics`)
- `GET /api/analytics/dashboard`: Get aggregate data (total beats, total artists, total plays).
- `GET /api/analytics/beats/{id}/stats`: Get play stats for a specific beat.
- `POST /api/analytics/events`: Log track plays or profile views.

### 3.5 Email Service (`/api/emails`)
- `POST /api/emails/send`: Send an email notification.
  - Body: `{ "to": "recipient@example.com", "subject": "Hello", "text": "Message content", "isHtml": false }`
  - Response: `{ "status": "QUEUED", "message": "..." }`

## 4. Required UI Pages & Features

### A. Dashboard (Modern & Vibrant)
- Stats Cards: Total Artists, Total Beats, Active Projects.
- Recent Activity: List of recently uploaded beats.
- Mini Analytics Chart: Showing weekly engagement.

### B. Beat Library
- Grid layout with audio players.
- "Upload Beat" Modal: Supporting drag-and-drop for audio files.
- Search and Filter by Genre/Artist.

### C. Artist CRM
- Table or Card view of all managed artists.
- Individual Artist Profile: Showing their bio, contact info, and their specific uploaded beats.
- Notes Section: A timeline of management notes.

### D. Analytics View
- Visual charts (Recharts or Chart.js) showing play distributions.
- Top Performing Artists leaderboard.

## 5. Implementation Tips for Lovable
- **Intercept Requests**: Use an `axios` instance to automatically attach the `Authorization: Bearer <token>` header to all requests after login.
- **Audio Handling**: Use standard HTML5 `<audio>` tags but styled with Tailwind to match the premium dark/glassmorphic aesthetic.
- **Transitions**: Use `framer-motion` for smooth page transitions between the Dashboard and Library.
- **Error Handling**: Implement Toast notifications for failed logins or upload errors.

## 6. Premium Aesthetics
- **Theme**: Dark Mode by default.
- **Colors**: Deep purples, electric blues, and slate grays.
- **Effects**: Use `backdrop-blur` (glassmorphism) for sidebars and modals.
- **Typography**: Inter or Montserrat for a modern, clean look.
