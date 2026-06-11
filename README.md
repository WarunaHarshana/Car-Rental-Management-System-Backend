# Backend README — Car Rental Management System

<aside>
📦

This is the README for the backend repository. Copy it into `README.md` at the root of your Spring Boot project.

</aside>

## Car Rental Management System (Backend)

A REST API for a car rental system. It handles user accounts, the car catalog, bookings, payments, and admin reports. Built with Spring Boot and MySQL.

Frontend repository: [Car-Rental-Management-System-Frontend](https://github.com/WarunaHarshana/Car-Rental-Management-System-Frontend)

## Tech stack

- Java 25
- Spring Boot 4.0.6 (Spring Web, Spring Data JPA)
- MySQL 9.3
- Maven
- Lombok

## What the API does

- Registers and logs in users. Passwords are hashed with BCrypt, and responses use DTOs so the password hash never leaves the server.
- Serves the car catalog and lets admins add, edit, and delete cars, including a photo upload endpoint.
- Creates bookings and updates their status.
- Records payments against bookings and keeps a payment history.
- Returns an admin report summary covering bookings, revenue, customers, and car usage.

## Project structure

```
src/main/java/edu/waruna/carrental
├── config        WebConfig (CORS + static file serving)
├── controller    Auth, Car, Booking, Payment, Report
├── dto           Response DTOs (no password leakage)
├── entity        User, Car, Booking, Payment
├── repository    Spring Data JPA repositories
└── service       PaymentService and supporting logic
```

## Getting started

### Prerequisites

- JDK 25
- MySQL running locally
- Maven (or the included Maven wrapper)

### Database setup

Create the database:

```sql
CREATE DATABASE car_rental_db;
```

The schema is generated automatically on startup because `spring.jpa.hibernate.ddl-auto` is set to `update`.

### Configure `application.properties`

```
spring.datasource.url=jdbc:mysql://localhost:3306/car_rental_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jackson.serialization.write-dates-as-timestamps=false
server.port=8080
```

Replace the username and password with your own. Do not commit real credentials.

### Run

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## API endpoints

### Auth

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Create a user |
| POST | `/api/auth/login` | Log in |

### Cars

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/cars` | List cars |
| GET | `/api/cars/{id}` | Get one car |
| POST | `/api/cars` | Add a car (admin) |
| PUT | `/api/cars/{id}` | Update a car (admin) |
| DELETE | `/api/cars/{id}` | Delete a car (admin) |
| POST | `/api/cars/{id}/photo` | Upload a car photo (admin) |

### Bookings

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/bookings` | List bookings |
| POST | `/api/bookings` | Create a booking |
| PUT | `/api/bookings/{id}/status` | Update booking status (admin) |
| DELETE | `/api/bookings/{id}` | Cancel a booking |

### Payments

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/payments/checkout/{bookingId}` | Pay for a booking |
| GET | `/api/payments` | List all payments (admin) |
| GET | `/api/payments/booking/{bookingId}` | Payments for one booking |

### Reports

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/reports/summary` | Admin report summary |

## Security notes

- Passwords are stored as BCrypt hashes, not plain text.
- Login and registration return a `UserDTO`, so the password field is never sent to the client.
- Booking and payment responses use DTOs to avoid exposing nested user data.

## Uploaded files

Car photos are saved under `uploads/cars` and served from `/uploads/**`. The `uploads/` folder is in `.gitignore`, so uploaded images are not committed.

## Notes for graders

- The frontend runs on `http://localhost:4200` and CORS is configured for that origin.
- Seed data (cars, users, bookings) is added through the running application, not through migrations.
