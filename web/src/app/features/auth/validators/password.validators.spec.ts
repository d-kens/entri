import { FormControl, FormGroup, FormGroupDirective } from '@angular/forms';

import { passwordsMatch, PasswordMismatchStateMatcher } from './password.validators';

describe('passwordsMatch', () => {
  it('returns null when password and confirmPassword are equal', () => {
    const group = new FormGroup({
      password: new FormControl('secret123'),
      confirmPassword: new FormControl('secret123'),
    });

    expect(passwordsMatch(group)).toBeNull();
  });

  it('returns a passwordsMismatch error when the values differ', () => {
    const group = new FormGroup({
      password: new FormControl('secret123'),
      confirmPassword: new FormControl('other456'),
    });

    expect(passwordsMatch(group)).toEqual({ passwordsMismatch: true });
  });

  it('treats two undefined controls as matching', () => {
    const group = new FormGroup({});

    expect(passwordsMatch(group)).toBeNull();
  });
});

describe('PasswordMismatchStateMatcher', () => {
  let matcher: PasswordMismatchStateMatcher;

  beforeEach(() => {
    matcher = new PasswordMismatchStateMatcher();
  });

  it('returns false when the control is null', () => {
    const form = { hasError: vi.fn().mockReturnValue(true) } as unknown as FormGroupDirective;

    expect(matcher.isErrorState(null, form)).toBe(false);
  });

  it('returns false when the form is null', () => {
    const control = new FormControl('');
    control.markAsTouched();

    expect(matcher.isErrorState(control, null)).toBe(false);
  });

  it('returns false when the control has not been touched', () => {
    const control = new FormControl('');
    const form = { hasError: vi.fn().mockReturnValue(true) } as unknown as FormGroupDirective;

    expect(matcher.isErrorState(control, form)).toBe(false);
  });

  it('returns false when the form has no passwordsMismatch error', () => {
    const control = new FormControl('');
    control.markAsTouched();
    const form = { hasError: vi.fn().mockReturnValue(false) } as unknown as FormGroupDirective;

    expect(matcher.isErrorState(control, form)).toBe(false);
  });

  it('returns true when the control is touched and the form has a passwordsMismatch error', () => {
    const control = new FormControl('');
    control.markAsTouched();
    const form = { hasError: vi.fn().mockReturnValue(true) } as unknown as FormGroupDirective;

    expect(matcher.isErrorState(control, form)).toBe(true);
    expect(form.hasError).toHaveBeenCalledWith('passwordsMismatch');
  });
});
