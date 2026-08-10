# Ticket Reservation Design

## Approach

**Reservation Table + `reservedQuantity` Counter + Pessimistic Locking**

`ticket_types` carries a `reserved_quantity` column. Availability is computed as:

```
availableCount = quantity - soldQuantity - reservedQuantity
```

On reservation creation, `reservedQuantity` is incremented inside the same locked transaction. Rows are locked with `SELECT ... FOR UPDATE` ordered by ID to prevent deadlocks.

## Planned Work

1. `POST /reservations` — create a reservation
2. `DELETE /reservations/{id}` — user cancels; mark reservation `CANCELLED` and decrement `reservedQuantity` on each ticket type
3. Background job (every minute) — find all `PENDING` reservations where `expiresAt < NOW()`, mark them `EXPIRED`, and decrement `reservedQuantity` on each ticket type. This job affects correctness: until it runs, expired slots are not returned to the available pool.

   **Algorithm:**
   ```
   BEGIN TRANSACTION
     SELECT * FROM reservations
       WHERE status = 'PENDING' AND expires_at < NOW()
       FOR UPDATE SKIP LOCKED          -- skip rows another job instance is processing

     FOR EACH expired reservation:
       FOR EACH item IN reservation.items ORDER BY ticket_type_id ASC:
         UPDATE ticket_types
           SET reserved_quantity = reserved_quantity - item.quantity
           WHERE id = item.ticket_type_id
                                       -- consistent ordering prevents deadlocks with reservation creation
       UPDATE reservations SET status = 'EXPIRED' WHERE id = reservation.id

   COMMIT
   ```
   `SKIP LOCKED` allows multiple job instances to process disjoint batches safely without blocking each other.
4. When returning a `TicketType` response, `availableCount` must reflect reality — do not return raw `quantity`.
    - UPDATE FE to expect avalableCount for ticket types
5. Return all ticket types but UI should distinguish status: `ACTIVE` vs `SOLD_OUT`.
6. Background job to mark ticket types as `ACTIVE` when sales begin.
7. Background job to mark events as `COMPLETED` when their end date and time has passed.
