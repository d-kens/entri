import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { IonHeader } from '@ionic/angular/ion-header';
import { IonToolbar } from '@ionic/angular/ion-toolbar';
import { IonTitle } from '@ionic/angular/ion-title';
import { IonContent } from '@ionic/angular/ion-content';
import { IonButton } from '@ionic/angular/ion-button';
import { IonIcon } from '@ionic/angular/ion-icon';
import { addIcons } from 'ionicons';
import { qrCodeOutline } from 'ionicons/icons';
import { SessionService } from '../../services/session.service';
import { CheckInService } from '../../services/check-in.service';

@Component({
  selector: 'app-enter-code',
  standalone: true,
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonButton, IonIcon],
  templateUrl: './enter-code.page.html',
  styleUrl: './enter-code.page.scss',
})
export class EnterCodePage {
  private readonly session = inject(SessionService);
  private readonly checkIn = inject(CheckInService);
  private readonly router = inject(Router);

  code = '';
  loading = signal(false);
  error = signal<string | null>(null);

  constructor() {
    addIcons({ qrCodeOutline });
  }

  async submit(): Promise<void> {
    const trimmed = this.code.trim();
    if (!trimmed) return;

    this.loading.set(true);
    this.error.set(null);

    try {
      const { eventTitle } = await this.checkIn.verifyCode(trimmed);
      this.session.setCode(trimmed);
      this.session.setEventTitle(eventTitle);
      this.router.navigate(['/scanner']);
    } catch (err: any) {
      this.error.set(err?.message ?? 'Something went wrong. Try again.');
    } finally {
      this.loading.set(false);
    }
  }
}
