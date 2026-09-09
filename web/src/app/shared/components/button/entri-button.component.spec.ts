import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntriButton } from './entri-button.component';

@Component({
  standalone: true,
  imports: [EntriButton],
  template: `<button appButton [variant]="variant" [size]="size" [loading]="loading">
    Click
  </button>`,
})
class ButtonHost {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' = 'primary';
  size: 'sm' | 'md' | 'lg' = 'md';
  loading = false;
}

@Component({
  standalone: true,
  imports: [EntriButton],
  template: `<a appButton [loading]="loading">Link</a>`,
})
class AnchorHost {
  loading = true;
}

describe('EntriButton', () => {
  it('should apply the default primary/md classes', async () => {
    const fixture: ComponentFixture<ButtonHost> = TestBed.createComponent(ButtonHost);
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.classList.contains('btn-primary')).toBe(true);
    expect(button.classList.contains('btn-md')).toBe(true);
  });

  it('should reflect variant and size input changes as host classes', async () => {
    const fixture: ComponentFixture<ButtonHost> = TestBed.createComponent(ButtonHost);
    const host = fixture.componentInstance;
    host.variant = 'danger';
    host.size = 'lg';
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.classList.contains('btn-danger')).toBe(true);
    expect(button.classList.contains('btn-lg')).toBe(true);
    expect(button.classList.contains('btn-primary')).toBe(false);
  });

  it('should disable and mark aria-busy on a real button while loading', async () => {
    const fixture: ComponentFixture<ButtonHost> = TestBed.createComponent(ButtonHost);
    const host = fixture.componentInstance;
    host.loading = true;
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.hasAttribute('disabled')).toBe(true);
    expect(button.getAttribute('aria-busy')).toBe('true');
    expect(button.classList.contains('btn-loading')).toBe(true);
  });

  it('should prevent the default navigation on a loading anchor click', () => {
    const fixture: ComponentFixture<AnchorHost> = TestBed.createComponent(AnchorHost);
    fixture.detectChanges();

    const anchor: HTMLAnchorElement = fixture.nativeElement.querySelector('a');

    const event = new MouseEvent('click', { cancelable: true });
    anchor.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
  });
});
