import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CheckInSessionService } from '../services/check-in-session-service';
import { CheckInService } from '../services/check-in-service';

@Component({
  selector: 'app-enter-code',
  standalone: true,
  imports: [FormsModule, MatIconModule],
  templateUrl: './enter-code.html',
  styleUrl: './enter-code.css',
})
export class EnterCode {
  private readonly session = inject(CheckInSessionService);
  private readonly checkIn = inject(CheckInService);
  private readonly router = inject(Router);

  code = '';
  loading = signal(false);
  error = signal<string | null>(null);

  async submit(): Promise<void> {
    const trimmed = this.code.trim();
    if (!trimmed) return;

    this.loading.set(true);
    this.error.set(null);

    try {
      const { eventTitle } = await this.checkIn.verifyCode(trimmed);
      this.session.setCode(trimmed);
      this.session.setEventTitle(eventTitle);
      this.router.navigate(['/check-in/scanner']);
    } catch (err: unknown) {
      this.error.set(err instanceof Error ? err.message : 'Something went wrong. Try again.');
    } finally {
      this.loading.set(false);
    }
  }
}
