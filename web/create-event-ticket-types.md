# Create Event — Ticket Types Refactor

Replace the inline ticket type FormArray in step 3 with the self-contained
`TicketTypeForm` component. Users add ticket types one at a time; each one
is confirmed before the next is added.

---

## 1. create-event.ts

Remove the `TicketTypeForm` type, `ticketsForm`, `ticketTypes` getter,
`addTicketType`, `removeTicketType`, and `ticketGroup` entirely.

Add these instead:

```typescript
import { TicketTypeForm, TicketTypeFormData } from '../components/ticket-type-form/ticket-type-form';

ticketTypes = signal<TicketTypeFormData[]>([]);

onTicketTypeAdded(data: TicketTypeFormData): void {
  this.ticketTypes.update(types => [...types, data]);
}

removeTicketType(index: number): void {
  this.ticketTypes.update(types => types.filter((_, i) => i !== index));
}
```

Add `TicketTypeForm` to the `imports` array.

Remove `FormArray`, `FormBuilder` array usage (keep FormBuilder for infoForm/venueForm).

Update `submit()` — replace `this.ticketsForm.invalid` check with:

```typescript
if (this.ticketTypes().length === 0) {
  this.snackbarService.showError('Add at least one ticket type');
  return;
}
```

Update `buildPayload()` — replace `this.ticketTypes.controls.map(...)` with:

```typescript
ticketTypes: this.ticketTypes().map(t => ({
  name: t.name,
  description: t.description || null,
  price: t.price,
  currency: 'KES',
  quantity: t.quantity,
  maxTicketsPerOrder: t.maxPerOrder,
  saleStartDate: t.salesStartDate || null,
  saleEndDate: t.salesEndDate || null,
})),
```

Update `loadForEdit()` — replace `this.ticketTypes.push(...)` loop with:

```typescript
this.ticketTypes.set(
  ev.ticketTypes.map((t) => ({
    name: t.name,
    description: t.description ?? '',
    price: t.price,
    quantity: t.quantity,
    maxPerOrder: t.maxTicketsPerOrder,
    salesStartDate: t.saleStartDate ?? '',
    salesEndDate: t.saleEndDate ?? '',
  })),
);
```

---

## 2. create-event.html — Step 3 ticket section

Replace the entire ticket types section (the `@for` block with `ticket-card` divs)
with:

```html
<!-- Add form — always visible -->
<app-tickek-type-form [data]="newTicketType" (added)="onTicketTypeAdded($event)" />

<!-- Added tickets list -->
@if (ticketTypes().length > 0) {
<div class="flex flex-col gap-3">
  @for (ticket of ticketTypes(); track $index) {
  <div class="ticket-card">
    <div class="ticket-card-head">
      <span class="ticket-number">{{ ticket.name }}</span>
      <span class="ticket-price">KES {{ ticket.price }}</span>
      <button mat-icon-button type="button" (click)="removeTicketType($index)">
        <mat-icon>delete_outline</mat-icon>
      </button>
    </div>
  </div>
  }
</div>
}
```

Remove the "Add Ticket" button from the section header — the form handles adding.

---

## 3. create-event.ts — add newTicketType signal

The `TicketTypeForm` component needs a `WritableSignal<TicketTypeFormData>` as
its `[data]` input. Add a reset signal that clears after each addition:

```typescript
newTicketType = signal<TicketTypeFormData>({
  name: '',
  price: 0,
  quantity: 1,
  description: '',
  salesStartDate: '',
  salesEndDate: '',
  maxPerOrder: 1,
});

onTicketTypeAdded(data: TicketTypeFormData): void {
  this.ticketTypes.update(types => [...types, data]);
  // reset the form for the next ticket
  this.newTicketType.set({
    name: '',
    price: 0,
    quantity: 1,
    description: '',
    salesStartDate: '',
    salesEndDate: '',
    maxPerOrder: 1,
  });
}
```

---

## 4. Step 3 — Continue button

Update `nextStep()` for step 2 (index 2 = ticket types step):

```typescript
} else if (this.currentStep() === 2) {
  if (this.ticketTypes().length === 0) {
    this.snackbarService.showError('Add at least one ticket type');
    return;
  }
}
```

No cross-form validity needed — each ticket type was already validated
before being added to the list.
