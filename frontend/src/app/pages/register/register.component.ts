import { Component, inject } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {emailValidator} from '../../validators/email';
import {passwordValidator} from '../../validators/password';
import { matchPasswordsValidator } from '../../validators/match-password';
import {errorMessages} from '../../data/error-messages';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {UserService} from '../../services/user/user.service';
import {firstValueFrom} from 'rxjs';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';
import {LoadingSpinnerService} from '../../services/loading-spinner/loading-spinner.service';
import { Router } from '@angular/router';
import {emailTakenValidator} from '../../validators/email-taken';
import {mapToRegisterUserPayload} from '../../models/user-register-payload';

@Component({
  selector: 'register',
  imports: [
    MatLabel,
    MatFormField,
    MatError,
    ReactiveFormsModule,
    MatCard,
    MatCardContent,
    MatCardTitle,
    MatInput,
    MatButton,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private userService = inject(UserService);
  private loadingSpinnerService = inject(LoadingSpinnerService);
  private router = inject(Router);

  formErrors = errorMessages;

  registerForm = new FormGroup({
      username: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, emailValidator], [emailTakenValidator(this.userService)]),
      password: new FormControl('', [Validators.required, passwordValidator()]),
      confirmPassword: new FormControl('', [Validators.required])
    }, { validators: matchPasswordsValidator });
  registrationFailed: boolean = false;
  registrationFailedMessage = "Registration failed. Please try again.";

  showSpinner() {
    this.loadingSpinnerService.showSpinner = true;
    this.loadingSpinnerService.message = 'Registering in Progress...';
  }

  async onSubmit() {
    this.registrationFailed = false;
    if (this.registerForm.valid) {
      this.showSpinner();
      console.log('Form Submitted!', this.registerForm.value);
      // Here you would typically handle the form submission, e.g., send data to a server
      // form is valid -> all controls are valid and filled out
      // -> the enforce operator ensures can be used without null checks
      const response = await firstValueFrom(this.userService.register(mapToRegisterUserPayload(this.registerForm.value)));


      if (response && 'error' in response) {
        console.error('Registration failed:', response.error);
        // Handle the error, e.g., show a message to the user
        this.registrationFailed = true;
      } else {
        console.log('Registration successful:', response);
        // Handle successful registration, e.g., redirect to login or show a success message
        this.router.navigate(['/login']);
      }

    } else {
      console.log('Form is invalid');
    }
    this.loadingSpinnerService.clearSpinner();
  }

}
