import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin, interval, Subscription } from 'rxjs';
import { EventsService } from '../services/events-service';
import { PageError } from '@shared/components/page-error/page-error';
import {
  EventResponse,
  EventTicketReservationDetailResponse,
} from '@features/events/models/event.models';
import { SnackbarService } from '@shared/services/snackbar-service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, MatIconModule, PageError],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private eventsService = inject(EventsService);
  private snackbar = inject(SnackbarService);

  readonly externalId = this.route.snapshot.paramMap.get('externalId')!;
  readonly reservationId = this.route.snapshot.paramMap.get('reservationId')!;

  event = signal<EventResponse | null>(null);
  reservation = signal<EventTicketReservationDetailResponse | null>(null);
  loading = signal(true);
  error = signal(false);

  private timerExpired = signal(false);
  private remainingMs = signal(0);

  expired = computed(() => this.reservation()?.status === 'EXPIRED' || this.timerExpired());
  confirmed = computed(() => this.reservation()?.status === 'CONFIRMED');
  timeLeft = computed(() => {
    const r = this.remainingMs();
    const mins = Math.floor(r / 60000);
    const secs = Math.floor((r % 60000) / 1000);
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  });
  urgentTimer = computed(() => this.remainingMs() > 0 && this.remainingMs() < 120000);

  totalTickets = computed(
    () => this.reservation()?.reservationItems.reduce((sum, item) => sum + item.quantity, 0) ?? 0,
  );

  private timerSub?: Subscription;

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.timerSub?.unsubscribe();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.timerExpired.set(false);
    this.timerSub?.unsubscribe();

    forkJoin([
      this.eventsService.getEvent(this.externalId),
      this.eventsService.getEventTicketReservation(this.externalId, this.reservationId),
    ]).subscribe({
      next: ([ev, reservation]) => {
        this.event.set(ev);
        this.reservation.set(reservation);
        this.loading.set(false);
        if (reservation.status === 'PENDING') {
          this.startTimer(reservation.expiresAt);
        }
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  pay(): void {
    this.snackbar.showInfo('Payment processing coming soon.');
  }

  private startTimer(expiresAt: string): void {
    const expiry = new Date(expiresAt).getTime();

    const tick = () => {
      const remaining = expiry - Date.now();
      if (remaining <= 0) {
        this.remainingMs.set(0);
        this.timerExpired.set(true);
        this.timerSub?.unsubscribe();
        return;
      }
      this.remainingMs.set(remaining);
    };

    tick();
    this.timerSub = interval(1000).subscribe(tick);
  }
}
