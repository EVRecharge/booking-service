# Service Booking - Independent Microservice

## Overview
Service Booking manages booking creation, status updates, and cancellations for EV charging services.

## Service Details
- **Port**: 8003
- **Database**: PostgreSQL (ev_booking_db)
- **Version**: 1.0.0

## Quick Start
```bash
cd service-booking
docker-compose up -d
```

## API Endpoints
- POST /api/bookings - Create booking
- GET /api/bookings/{id} - Get booking by ID
- GET /api/bookings/user/{userId} - Get user's bookings
- GET /api/bookings/provider/{providerId} - Get provider's bookings
- PATCH /api/bookings/{id}/status - Update booking status
- POST /api/bookings/{id}/cancel - Cancel booking
- DELETE /api/bookings/{id} - Delete booking

## Database Schema
- service_bookings table with user/provider IDs, times, status, cost
- Supports PENDING, CONFIRMED, COMPLETED, CANCELLED status

## Features
- Booking creation with automatic duration/cost calculation
- Status tracking
- Booking history
- User and provider specific queries
- Integration with user and provider services

## Configuration
Edit .env file:
- DB_HOST, DB_PORT, DB_NAME
- DB_USER, DB_PASSWORD
- EUREKA_SERVER

## Testing
```bash
curl -X POST http://localhost:8003/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "providerId": 1,
    "startTime": "2024-07-01T09:00:00",
    "endTime": "2024-07-01T11:00:00",
    "address": "123 Main St",
    "city": "San Francisco"
  }'
```

## Deployment
```bash
docker build -t service-booking:1.0.0 .
docker push service-booking:1.0.0
docker-compose up -d
```

## Monitoring
- Health: http://localhost:8003/actuator/health
- Logs: logs/service-booking.log
