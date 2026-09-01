# Tickets Plan

## Lifecycle
Reservation CONFIRMED → Tickets Generated → Email Sent → Gate Scan → Checked In

## Database

### `tickets` table
One row per individual ticket — a reservation for 2×VIP = 2 rows.

| Column | Type | Notes |
|---|---|---|
| ticket_code | UUID | unique, goes in the QR |
| reservation_id | FK | |
| event_id | FK | |
| ticket_type_id | FK | |
| holder_name | VARCHAR | |
| holder_email | VARCHAR | |
| status | ENUM | VALID, USED, CANCELLED |
| checked_in_at | DATETIME | |
| checked_in_by | VARCHAR | staff external key or code used |

### `event_check_in_codes` table
Organiser generates a short-lived code per event for staff to authenticate on mobile.

| Column | Type | Notes |
|---|---|---|
| code | VARCHAR | random, unique |
| event_id | FK | scoped to one event |
| expires_at | DATETIME | |
| created_by | VARCHAR | organiser external key |

## Ticket Generation
Triggered by a RabbitMQ `ReservationConfirmed` event — same pattern as user events.

## Email Delivery
Send one email per reservation with a link to `/tickets/{reservationId}`.
The page renders all tickets with QR codes (frontend generates QR from `ticket_code`).

## Endpoints

| Method | Path | Who | Notes |
|---|---|---|---|
| `GET` | `/reservations/{id}/tickets` | Buyer | View tickets after purchase |
| `GET` | `/tickets/{ticketCode}` | Public | Render individual ticket with QR |
| `POST` | `/events/{id}/check-in-code` | Organiser | Generate a scoped access code for staff |
| `POST` | `/check-in/verify-code` | Mobile app | Exchange code for a scoped session token |
| `POST` | `/tickets/{ticketCode}/check-in` | Staff (scoped token) | Mark ticket as used |

### Check-in response
```json
{ "result": "VALID | ALREADY_USED | INVALID", "holderName": "", "ticketType": "", "checkedInAt": "" }
```

Check-in uses a single atomic `UPDATE ... WHERE status = 'VALID'` — rows affected = 1 means
success, 0 means already used or invalid. No race conditions under concurrent scanning.

## Mobile App (Ionic + Capacitor)
Separate `mobile/` app in the repo root. Scan-only — no event management.

### Staff authentication
Organisers do **not** share their credentials with staff. Instead:
1. Organiser opens the web app → generates a check-in code for a specific event
2. Shares the code with staff (WhatsApp, SMS, etc.)
3. Staff open the mobile app → enter the code → receive a scoped token tied to that event
4. Token only permits `POST /tickets/{ticketCode}/check-in` for that event — nothing else
5. Code and token expire when the event ends

### Mobile screens
1. **Enter code** — staff enter the event access code
2. **Scanner** — camera opens, scan QR, instant valid ✓ or invalid ✗ feedback
