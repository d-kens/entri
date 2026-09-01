import { Component, ViewChild, ElementRef, inject, signal, OnDestroy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { ViewWillEnter, ViewWillLeave } from '@ionic/angular/common';
import { IonHeader } from '@ionic/angular/ion-header';
import { IonToolbar } from '@ionic/angular/ion-toolbar';
import { IonTitle } from '@ionic/angular/ion-title';
import { IonContent } from '@ionic/angular/ion-content';
import { IonButton } from '@ionic/angular/ion-button';
import { IonButtons } from '@ionic/angular/ion-buttons';
import { IonIcon } from '@ionic/angular/ion-icon';
import { addIcons } from 'ionicons';
import { checkmarkCircle, closeCircle, logOutOutline, qrCodeOutline, alertCircleOutline } from 'ionicons/icons';
import jsQR from 'jsqr';
import { BarcodeScanner } from '@capacitor-mlkit/barcode-scanning';
import { CheckInService, CheckInResponse } from '../../services/check-in.service';
import { SessionService } from '../../services/session.service';

type ResultState = 'none' | 'loading' | 'valid' | 'used' | 'invalid' | 'error';

@Component({
  selector: 'app-scanner',
  standalone: true,
  imports: [
    DatePipe, FormsModule,
    IonHeader, IonToolbar, IonTitle, IonContent,
    IonButton, IonButtons, IonIcon,
  ],
  templateUrl: './scanner.page.html',
  styleUrl: './scanner.page.scss',
})
export class ScannerPage implements OnDestroy, ViewWillEnter, ViewWillLeave {
  @ViewChild('videoEl') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasEl') canvasRef!: ElementRef<HTMLCanvasElement>;

  private readonly checkIn = inject(CheckInService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly isNative = Capacitor.isNativePlatform();

  resultState = signal<ResultState>('none');
  checkInResponse = signal<CheckInResponse | null>(null);
  errorMsg = signal<string | null>(null);
  manualCode = '';
  cameraError = false;
  private _showManual = false;
  get showManual(): boolean { return this._showManual; }
  set showManual(val: boolean) {
    this._showManual = val;
    if (!this.isNative) {
      if (val) {
        this.scanning = false;
        cancelAnimationFrame(this.animFrameId);
      } else if (this.stream) {
        this.scanning = true;
        this.scanLoop();
      }
    }
  }

  private stream: MediaStream | null = null;
  private animFrameId = 0;
  private scanning = false;

  constructor() {
    addIcons({ checkmarkCircle, closeCircle, logOutOutline, qrCodeOutline, alertCircleOutline });
  }

  ionViewWillEnter(): void {
    if (!this.isNative) {
      this.startWebCamera();
    }
  }

  ionViewWillLeave(): void {
    this.stopWebCamera();
  }

  private async startWebCamera(): Promise<void> {
    try {
      this.cameraError = false;
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' },
      });
      const video = this.videoRef.nativeElement;
      video.srcObject = this.stream;
      await video.play();
      this.scanning = true;
      this.scanLoop();
    } catch {
      this.cameraError = true;
    }
  }

  private stopWebCamera(): void {
    this.scanning = false;
    cancelAnimationFrame(this.animFrameId);
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }

  private scanLoop(): void {
    if (!this.scanning) return;
    const video = this.videoRef?.nativeElement;
    const canvas = this.canvasRef?.nativeElement;
    if (!video || !canvas) return;

    if (video.readyState === video.HAVE_ENOUGH_DATA && this.resultState() === 'none') {
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext('2d')!;
      ctx.drawImage(video, 0, 0);
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const code = jsQR(imageData.data, imageData.width, imageData.height);
      if (code?.data) {
        this.submitTicketCode(code.data);
        return;
      }
    }

    this.animFrameId = requestAnimationFrame(() => this.scanLoop());
  }

  async scanNative(): Promise<void> {
    try {
      const { barcodes } = await BarcodeScanner.scan();
      const code = barcodes[0]?.rawValue;
      if (code) await this.submitTicketCode(code);
    } catch {
      this.errorMsg.set('Scan cancelled.');
      this.resultState.set('error');
    }
  }

  async submitManual(): Promise<void> {
    const code = this.manualCode.trim();
    if (!code) return;
    this.manualCode = '';
    await this.submitTicketCode(code);
  }

  private async submitTicketCode(ticketCode: string): Promise<void> {
    this.resultState.set('loading');
    try {
      const response = await this.checkIn.checkIn(ticketCode, this.session.checkInCode()!);
      this.checkInResponse.set(response);
      this.resultState.set(
        response.result === 'VALID' ? 'valid' :
        response.result === 'ALREADY_USED' ? 'used' : 'invalid'
      );
    } catch (err: any) {
      this.errorMsg.set(err?.error?.message ?? 'Something went wrong. Try again.');
      this.resultState.set('error');
    }
  }

  reset(): void {
    this.resultState.set('none');
    this.checkInResponse.set(null);
    this.errorMsg.set(null);
    if (!this.isNative && !this.cameraError) {
      this.scanning = true;
      this.scanLoop();
    }
  }

  logout(): void {
    this.stopWebCamera();
    this.session.clearCode();
    this.router.navigate(['/enter-code']);
  }

  ngOnDestroy(): void {
    this.stopWebCamera();
  }
}
