import { Component, inject } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {emailValidator} from '../../validators/email';
import {passwordValidator} from '../../validators/password';
import { matchPasswordsValidator } from '../../validators/match-password';
import {errorMessages} from '../../data/error-messages';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {UserService} from '../../services/user.service';
import {catchError, firstValueFrom, of} from 'rxjs';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';


@Component({
  selector: 'app-register',
  imports: [
    MatLabel,
    MatFormField,
    MatError,
    ReactiveFormsModule,
    MatCard,
    MatCardContent,
    MatCardTitle,
    MatInput,
    MatButton
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private userService = inject(UserService);

  formErrors = errorMessages;

  registerForm = new FormGroup({
      username: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, emailValidator]),
      password: new FormControl('', [Validators.required, passwordValidator]),
      confirmPassword: new FormControl('', [Validators.required])
    }, { validators: matchPasswordsValidator });

  async onSubmit() {
    if (this.registerForm.valid) {
      console.log('Form Submitted!', this.registerForm.value);
      // Here you would typically handle the form submission, e.g., send data to a server
      const formValues = this.registerForm.value
      // form is valid -> all controls are valid and filled out
      // -> the enforce operator ensures can be used without null checks
      const response = await firstValueFrom(this.userService.register(formValues.email!, formValues.password!, formValues.username!)
        .pipe(
          catchError(error => of({ error }))
        ));

      if ('error' in response) {
        console.error('Registration failed:', response.error);
        // Handle the error, e.g., show a message to the user
      } else {
        console.log('Registration successful:', response);
        // Handle successful registration, e.g., redirect to login or show a success message
        // this.router.navigate(['/login']);
      }

    } else {
      console.log('Form is invalid');
    }
  }

}
