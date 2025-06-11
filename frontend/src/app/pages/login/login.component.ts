import {Component, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {emailValidator} from '../../validators/email';
import {passwordValidator} from '../../validators/password';
import {MatButton} from '@angular/material/button';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput, MatLabel} from '@angular/material/input';
import {UserService} from '../../services/user/user.service';
import {errorMessages} from '../../data/error-messages';
import {Router, RouterLink} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {NgIf} from '@angular/common';
import {LoadingSpinnerService} from '../../services/loading-spinner/loading-spinner.service';

@Component({
  selector: 'login',
  imports: [
    MatButton,
    MatCard,
    MatCardContent,
    MatCardTitle,
    MatError,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    RouterLink,
    NgIf
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private userService = inject(UserService);
  private router = inject(Router);
  private loadingSpinnerService = inject(LoadingSpinnerService);
  showLoginFailureMessage = signal(false);

  formErrors = errorMessages;

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, emailValidator]),
    password: new FormControl('', [Validators.required, passwordValidator()]),
  });

  showSpinner() {
    this.loadingSpinnerService.showSpinner = true;
    this.loadingSpinnerService.message = 'Logging in...';
  }

  async onSubmit() {
    this.showLoginFailureMessage.set(false);
    if (this.loginForm.valid) {
      this.showSpinner();
      console.log('Form Submitted!', this.loginForm.value);
      const formValues = this.loginForm.value;
      // form is valid -> all controls are valid and filled out
      // -> the enforce operator ensures can be used without null checks
      const response= await firstValueFrom(this.userService.login(formValues.email!, formValues.password!));
      console.log(response)
      if (response && 'error' in response) {
        this.showLoginFailureMessage.set(true);
      } else {
        console.log('Login successful:', response);
        // Navigate to the home page or dashboard after successful login
        this.router.navigate(['/project']).then(() => {});
      }
      this.loadingSpinnerService.clearSpinner();
    }
  }

}
