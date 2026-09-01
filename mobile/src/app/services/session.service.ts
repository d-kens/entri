import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly CHECK_IN_CODE_KEY = 'checkInCode';

  readonly checkInCode = signal<string | null>(
    sessionStorage.getItem(this.CHECK_IN_CODE_KEY)
  );

  setCode(code: string): void {
    sessionStorage.setItem(this.CHECK_IN_CODE_KEY, code);
    this.checkInCode.set(code);
  }

  clearCode(): void {
    sessionStorage.removeItem(this.CHECK_IN_CODE_KEY);
    this.checkInCode.set(null);
  }
}
