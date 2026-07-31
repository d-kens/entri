import { AbstractControl, ValidationErrors } from '@angular/forms';

export const passwordsMatchValidator = (group: AbstractControl): ValidationErrors | null => {
  const passwordControl = group.get('password');
  const confirmControl = group.get('confirmPassword');

  if (!passwordControl || !confirmControl) return null;

  const password = passwordControl.value;
  const confirmPassword = confirmControl.value;

  // Helper to remove only the passwordMismatch error without clobbering others
  const clearMismatchError = () => {
    const currentErrors = confirmControl.errors || {};
    if ('passwordMismatch' in currentErrors) {
      const { passwordMismatch, ...rest } = currentErrors as Record<string, any>;
      const newErrors = Object.keys(rest).length ? rest : null;
      confirmControl.setErrors(newErrors);
    }
  };

  // If either field is empty, don't validate mismatch yet, and ensure mismatch error is cleared
  if (!password || !confirmPassword) {
    clearMismatchError();
    return null;
  }

  if (password !== confirmPassword) {
    const currentErrors = confirmControl.errors || {};
    if (!currentErrors['passwordMismatch']) {
      confirmControl.setErrors({ ...currentErrors, passwordMismatch: true });
    }
    return { passwordMismatch: true };
  }

  // Passwords match - clear mismatch error if present
  clearMismatchError();
  return null;
};
