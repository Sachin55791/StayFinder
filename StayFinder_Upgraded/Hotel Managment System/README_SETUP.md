# StayFinder / Hotel Management System

This upgraded version keeps the original single-page design and connects it to the Spring Boot REST backend.

## Requirements
- Java 17+
- Maven 3.9+ (or use `mvnw.cmd` on Windows)
- MySQL 8+
- VS Code Live Server extension (recommended)

## 1. Database
Create the database in MySQL Workbench:
```sql
CREATE DATABASE hotel_db;
```
Set the environment variables from `hotel-backend/.env.example`. At minimum set `DB_USERNAME`, `DB_PASSWORD`, and a long random `JWT_SECRET`. Do not commit real credentials or API keys.

Windows PowerShell example:
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:JWT_SECRET="replace-this-with-a-long-random-secret-value"
```

## 2. Start backend
```powershell
cd hotel-backend
.\mvnw.cmd spring-boot:run
```
Backend health check: http://localhost:8080/test
The first run creates tables and seeds four sample approved hotels if the hotel table is empty. Sample properties are demo data, not live hotel inventory.

## 3. Start frontend
Open the project root in VS Code, right-click `home.html`, choose **Open with Live Server**. The default API URL is `http://localhost:8080`.

## Authentication
- Signup: `POST /api/users/signup` (name, email, password, phone, role USER or OWNER)
- Login: `POST /api/users/login` (email, password)
- Passwords are stored with BCrypt; successful login returns a JWT. Existing legacy accounts with plain-text passwords are upgraded to BCrypt the first time they successfully log in. Admin self-registration is intentionally disabled. To bootstrap the first admin securely, set `ADMIN_EMAIL`, `ADMIN_PASSWORD` (minimum 12 characters), and optionally `ADMIN_NAME` before the first backend run. The admin account is created only if that email does not already exist.

## API notes
- Public approved hotel browsing: `GET /api/hotels?city=Delhi`
- Hotel details: `GET /api/hotels/{id}`
- AI/database shortlist: `POST /api/ai/recommend` with `{ "city":"Delhi", "maxBudget":5000 }`
- Booking request: authenticated `POST /api/bookings`; new bookings remain PENDING.
- Booking history: authenticated `GET /api/bookings/mine`

## Important limitations before production
- Razorpay is not wired to a live merchant account. Bookings are PENDING and are never falsely marked PAID. Add server-side order creation, signature verification and webhook handling before accepting money.
- The AI endpoint performs a database-grounded shortlist. To enable generated AI explanations, set `AI_API_KEY` (OpenAI-compatible API key), `AI_MODEL`, and optionally `AI_BASE_URL`. The model receives only the matching hotel records and is instructed not to invent availability or prices. No API key is included in this ZIP.
- SMS OTP delivery, email notifications, Google Maps integration, date-level room inventory/availability and advanced analytics dashboards still need implementation and security review. Basic owner property submission and admin approval endpoints/panels are included.
- The current UI's role buttons are not security controls. Backend authorization should be expanded before exposing owner/admin write operations.
- Do not deploy with development CORS/JWT settings. Use HTTPS, secrets manager, rate limiting, validation and production DB migrations.


## Security configuration (important)

The backend reads database credentials, JWT signing key, and initial admin credentials from environment variables. Do not commit a real `.env` file or credentials. Copy `.env.example` as a reference and configure these variables in your IDE Run Configuration or operating-system environment:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET` (use a unique random value of at least 32 bytes)
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` (initial admin is created only when the email does not already exist)
- `AI_API_KEY` (optional; without it, AI endpoints return an honest database-backed fallback message)

After changing Java files, rebuild and restart the backend from the `hotel-backend` folder with `mvn clean package` (or `mvnw.cmd clean package` on Windows). Do not run an old JAR from `target`; it may not contain the latest controller routes.

## Booking workflow

A customer creates a booking request for an approved hotel. It is saved as `PENDING`. The owning hotel account can retrieve its pending requests at `GET /api/bookings/owner/pending` and accept/reject a request at `PUT /api/bookings/owner/{id}/decision?status=APPROVED` or `REJECTED`. Owner routes are restricted both by Spring Security and by ownership checks in the controller.

Booking approval is a request workflow only: payment capture, room inventory locking, date-overlap prevention, and final price calculation are not implemented yet. Do not advertise those as completed features.
