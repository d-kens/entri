# Tickets Plan

## Lifecycle
Reservation CONFIRMED → Tickets Generated → Email Sent → Gate Scan → Checked In

## Database
New `tickets` table — one row per individual ticket (a reservation for 2×VIP = 2 rows).

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
| checked_in_by | VARCHAR | organiser external key |

## Ticket Generation
Triggered by a RabbitMQ `ReservationConfirmed` event — same pattern as user events.

## Email Delivery
Send one email per reservation with a link to `/tickets/{reservationId}`.
The page renders all tickets with QR codes (frontend generates QR from `ticket_code`).

## New Endpoints

| Method | Path | Notes |
|---|---|---|
| `GET` | `/reservations/{id}/tickets` | Buyer views their tickets |
| `GET` | `/tickets/{ticketCode}` | Public ticket page |
| `POST` | `/tickets/{ticketCode}/check-in` | Organiser — atomic UPDATE WHERE status = VALID |

### Check-in response
```json
{ "result": "VALID | ALREADY_USED | INVALID", "holderName": "", "ticketType": "", "checkedInAt": "" }
```

Check-in uses a single atomic `UPDATE ... WHERE status = 'VALID'` — rows affected = 1 means success, 0 means already used or invalid. No race conditions.

## Mobile (Ionic + Capacitor)
Separate `mobile/` app in the repo root alongside `web/`. Organiser logs in with
their existing credentials. Staff share the same account on their own devices — no
separate staff login needed.

The mobile app is narrow in scope — day-of-event tooling only:
- Gate check-in scanner (Capacitor ML Kit barcode scanning)
- Live checked-in count vs total for the event
- Attendee search / manual lookup

Web app handles everything else (event management, dashboard, buyer ticket view).
