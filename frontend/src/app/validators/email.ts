import { AbstractControl, ValidationErrors } from '@angular/forms';

export function emailValidator(control: AbstractControl): ValidationErrors | null {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const value = control.value;
  if (!value || emailRegex.test(value)) {
    return null;
  }
  return { invalidEmail: true };
}

