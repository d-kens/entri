import { Injectable, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class CheckInSessionService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly CODE_KEY = 'checkInCode';
  private readonly EVENT_TITLE_KEY = 'eventTitle';

  readonly checkInCode = signal<string | null>(this.read(this.CODE_KEY));
  readonly eventTitle = signal<string | null>(this.read(this.EVENT_TITLE_KEY));

  setCode(code: string): void {
    this.write(this.CODE_KEY, code);
    this.checkInCode.set(code);
  }

  setEventTitle(title: string): void {
    this.write(this.EVENT_TITLE_KEY, title);
    this.eventTitle.set(title);
  }

  clearCode(): void {
    this.remove(this.CODE_KEY);
    this.remove(this.EVENT_TITLE_KEY);
    this.checkInCode.set(null);
    this.eventTitle.set(null);
  }

  private read(key: string): string | null {
    return isPlatformBrowser(this.platformId) ? sessionStorage.getItem(key) : null;
  }

  private write(key: string, value: string): void {
    if (isPlatformBrowser(this.platformId)) sessionStorage.setItem(key, value);
  }

  private remove(key: string): void {
    if (isPlatformBrowser(this.platformId)) sessionStorage.removeItem(key);
  }
}
