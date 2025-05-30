import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export function passwordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null; // don't validate empty value, use required validator separately

    const minLength = /.{8,}/.test(value);
    const hasUpperCase = /[A-Z]/.test(value);
    const hasNumber = /\d/.test(value);
    const hasSpecialChar = /[ !"#$%&'()*+,\-./:;<=>?@[\\\]^_`{|}~]/.test(value);;

    const isValid = minLength && hasUpperCase && hasNumber && hasSpecialChar;

    const wat = isValid ? null : { invalidPassword: true };
    console.log('Password validation result:', wat, value);
    return wat;
  };
}
