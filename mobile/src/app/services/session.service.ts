import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly CODE_KEY = 'checkInCode';
  private readonly EVENT_TITLE_KEY = 'eventTitle';

  readonly checkInCode = signal<string | null>(sessionStorage.getItem(this.CODE_KEY));
  readonly eventTitle = signal<string | null>(sessionStorage.getItem(this.EVENT_TITLE_KEY));

  setCode(code: string): void {
    sessionStorage.setItem(this.CODE_KEY, code);
    this.checkInCode.set(code);
  }

  setEventTitle(title: string): void {
    sessionStorage.setItem(this.EVENT_TITLE_KEY, title);
    this.eventTitle.set(title);
  }

  clearCode(): void {
    sessionStorage.removeItem(this.CODE_KEY);
    sessionStorage.removeItem(this.EVENT_TITLE_KEY);
    this.checkInCode.set(null);
    this.eventTitle.set(null);
  }
}
