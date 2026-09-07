import {
  Component,
  ViewChild,
  ElementRef,
  inject,
  signal,
  OnInit,
  OnDestroy,
  PLATFORM_ID,
} from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import jsQR from 'jsqr';
import { CheckInService, CheckInResponse } from '../services/check-in-service';
import { CheckInSessionService } from '../services/check-in-session-service';

type ResultState = 'none' | 'loading' | 'valid' | 'used' | 'invalid' | 'error';

@Component({
  selector: 'app-scanner',
  standalone: true,
  imports: [DatePipe, FormsModule, MatIconModule],
  templateUrl: './scanner.html',
  styleUrl: './scanner.css',
})
export class Scanner implements OnInit, OnDestroy {
  @ViewChild('videoEl') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasEl') canvasRef!: ElementRef<HTMLCanvasElement>;

  private readonly checkIn = inject(CheckInService);
  private readonly session = inject(CheckInSessionService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  readonly eventTitle = this.session.eventTitle;

  resultState = signal<ResultState>('none');
  checkInResponse = signal<CheckInResponse | null>(null);
  errorMsg = signal<string | null>(null);
  manualCode = '';
  cameraError = false;
  private _showManual = false;
  get showManual(): boolean {
    return this._showManual;
  }
  set showManual(val: boolean) {
    this._showManual = val;
    if (val) {
      this.scanning = false;
      cancelAnimationFrame(this.animFrameId);
    } else if (this.stream) {
      this.scanning = true;
      this.scanLoop();
    }
  }

  private stream: MediaStream | null = null;
  private animFrameId = 0;
  private scanning = false;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.startWebCamera();
    }
  }

  ngOnDestroy(): void {
    this.stopWebCamera();
  }

  private async startWebCamera(): Promise<void> {
    try {
      this.cameraError = false;
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: 'environment',
          width: { ideal: 1280 },
          height: { ideal: 720 },
          advanced: [{ focusMode: 'continuous' } as MediaTrackConstraintSet],
        },
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
      const ctx = canvas.getContext('2d', { willReadFrequently: true })!;
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
        response.result === 'VALID'
          ? 'valid'
          : response.result === 'ALREADY_USED'
            ? 'used'
            : 'invalid',
      );
    } catch (err: unknown) {
      this.errorMsg.set(err instanceof Error ? err.message : 'Something went wrong. Try again.');
      this.resultState.set('error');
    }
  }

  reset(): void {
    this.resultState.set('none');
    this.checkInResponse.set(null);
    this.errorMsg.set(null);
    if (!this.cameraError) {
      this.scanning = true;
      this.scanLoop();
    }
  }

  logout(): void {
    this.stopWebCamera();
    this.session.clearCode();
    this.router.navigate(['/check-in']);
  }
}
