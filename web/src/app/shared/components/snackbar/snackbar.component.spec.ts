import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';

import { SnackbarComponent, SnackbarData } from './snackbar.component';

describe('SnackbarComponent', () => {
  let fixture: ComponentFixture<SnackbarComponent>;
  let component: SnackbarComponent;
  let ref: { dismiss: ReturnType<typeof vi.fn> };

  function setup(data: SnackbarData) {
    ref = { dismiss: vi.fn() };

    TestBed.configureTestingModule({
      imports: [SnackbarComponent],
      providers: [
        { provide: MAT_SNACK_BAR_DATA, useValue: data },
        { provide: MatSnackBarRef, useValue: ref },
      ],
    });

    fixture = TestBed.createComponent(SnackbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    setup({ message: 'Saved', type: 'success' });
    expect(component).toBeTruthy();
  });

  it('should pick the icon matching the message type', () => {
    setup({ message: 'Something failed', type: 'error' });
    expect(component.icon).toBe('error');
  });

  it('should render the message text', () => {
    setup({ message: 'Wallet updated', type: 'info' });
    expect(fixture.nativeElement.textContent).toContain('Wallet updated');
  });

  it('should not render an action button when no action is provided', () => {
    setup({ message: 'Saved', type: 'success' });
    expect(fixture.nativeElement.querySelector('.snackbar-action-btn')).toBeNull();
  });

  it('should invoke the action callback and dismiss when the action button is clicked', () => {
    const callback = vi.fn();
    setup({ message: 'Undo?', type: 'warning', action: { label: 'Undo', callback } });

    const actionButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.snackbar-action-btn');
    actionButton.click();

    expect(callback).toHaveBeenCalledOnce();
    expect(ref.dismiss).toHaveBeenCalledOnce();
  });

  it('should dismiss when the close button is clicked', () => {
    setup({ message: 'Saved', type: 'success' });

    const closeButton: HTMLButtonElement = fixture.nativeElement.querySelector('.snackbar-close');
    closeButton.click();

    expect(ref.dismiss).toHaveBeenCalledOnce();
  });
});
