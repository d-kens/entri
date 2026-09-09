import { FormControl, FormGroup } from '@angular/forms';

import { passwordsMatchValidator } from './validators';

describe('passwordsMatchValidator', () => {
  function buildGroup(password: string, confirmPassword: string) {
    return new FormGroup({
      password: new FormControl(password),
      confirmPassword: new FormControl(confirmPassword),
    });
  }

  it('returns null when the group is missing either control', () => {
    const group = new FormGroup({ password: new FormControl('secret') });

    expect(passwordsMatchValidator(group)).toBeNull();
  });

  it('returns null and does not flag a mismatch while either field is empty', () => {
    const group = buildGroup('', 'secret');

    expect(passwordsMatchValidator(group)).toBeNull();
    expect(group.get('confirmPassword')?.errors).toBeNull();
  });

  it('returns a passwordMismatch error and sets it on confirmPassword when values differ', () => {
    const group = buildGroup('secret123', 'different456');

    const result = passwordsMatchValidator(group);

    expect(result).toEqual({ passwordMismatch: true });
    expect(group.get('confirmPassword')?.errors).toEqual({ passwordMismatch: true });
  });

  it('returns null and clears a previously set mismatch error when values match', () => {
    const group = buildGroup('secret123', 'different456');
    passwordsMatchValidator(group);

    group.get('confirmPassword')?.setValue('secret123');
    const result = passwordsMatchValidator(group);

    expect(result).toBeNull();
    expect(group.get('confirmPassword')?.errors).toBeNull();
  });

  it('preserves unrelated errors on confirmPassword when clearing the mismatch error', () => {
    const group = buildGroup('secret123', 'secret123');
    group.get('confirmPassword')?.setErrors({ passwordMismatch: true, required: true });

    const result = passwordsMatchValidator(group);

    expect(result).toBeNull();
    expect(group.get('confirmPassword')?.errors).toEqual({ required: true });
  });

  it('does not duplicate the mismatch error flag across repeated invalid checks', () => {
    const group = buildGroup('secret123', 'different456');

    passwordsMatchValidator(group);
    const result = passwordsMatchValidator(group);

    expect(result).toEqual({ passwordMismatch: true });
    expect(group.get('confirmPassword')?.errors).toEqual({ passwordMismatch: true });
  });
});
