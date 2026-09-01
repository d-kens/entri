# Organizer Dashboard Plan

## New Endpoints

| Method | Path | Notes |
|---|---|---|
| `GET` | `/events/stats` | Aggregated totals — scoped to authenticated organiser from JWT |
| `GET` | `/reservations?scope=managed&limit=10` | Recent sales across organiser's events |

### `/events/stats` response
```json
{
  "totalRevenue": 15400.00,
  "ticketsSold": 320,
  "totalAttendees": 298,
  "upcomingEventsCount": 4
}
```

### `/reservations?scope=managed&limit=10` response
```json
[
  { "eventTitle": "Jazz Night", "buyerName": "Jane Doe", "amount": 50.00, "ticketCount": 2, "createdAt": "..." }
]
```

## Extend `EventResponse`
Add `ticketsSold`, `totalCapacity`, `totalRevenue` so the events list is self-contained — no extra calls per event.

## Dashboard sections
- **Stat cards** — total revenue, tickets sold, attendees, upcoming events (from `/events/stats`)
- **Event performance table** — each event with tickets sold / capacity, revenue (from extended `EventResponse`)
- **Recent sales feed** — latest purchases across all events (from `/reservations?scope=managed`)

## Implementation order
1. Extend `EventResponse` with sales fields
2. Implement `GET /events/stats`
3. Implement `GET /reservations?scope=managed`
4. Build frontend dashboard
