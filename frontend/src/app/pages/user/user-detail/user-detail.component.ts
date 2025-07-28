import {Component, inject, OnInit, signal} from '@angular/core';
import { UserService } from '../../../services/user/user.service';
import {LoadingSpinnerService} from '../../../services/loading-spinner/loading-spinner.service';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {getRoleEnumValues, stringToEnum} from '../../../models/user';
import {CompleteUserDetailResponse, UserDetailResponse} from '../../../models/user-detail-response';
import {CommonModule} from '@angular/common';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatSelect} from '@angular/material/select';
import {MatOption} from '@angular/material/core';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {forkJoin} from 'rxjs';
import { environment } from '../../../../env/env';


@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatFormField, MatSelect, MatOption, MatButton, MatInput, MatError, MatLabel],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent implements OnInit {
  roles = getRoleEnumValues();
  userService = inject(UserService);
  loadingSpinnerService = inject(LoadingSpinnerService);
  showErrorMessage = signal(false);
  userForm = new FormGroup({
    userName: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    role: new FormControl('', Validators.required),
    profilePic: new FormControl(''),
  });
  isEditModeActive = signal(false);
  profilePicUrl: string = environment.defaultProfilePic;

  async ngOnInit() {
    await this.loadUserData();
  }

  mapUserDetails(resp: UserDetailResponse) {
    this.userForm = new FormGroup({
      userName: new FormControl(resp.userName, Validators.required),
      email: new FormControl(resp.email, [Validators.required, Validators.email]),
      role: new FormControl(stringToEnum(resp.role)?.toString() || '', Validators.required), // Map role string to Role enum
      profilePic: new FormControl(resp.profilePic || ''),
    });
  }

  showSpinner() {
    this.loadingSpinnerService.showSpinner = true;
    this.loadingSpinnerService.message = 'Loading user details...';
  }

  hideSpinner() {
    this.loadingSpinnerService.showSpinner = false;
    this.loadingSpinnerService.message = '';
  }

  async loadUserData() {
    this.showSpinner();
    this.showErrorMessage.set(false)
    forkJoin({
      userDetails: this.userService.fetchActiveUser(),
      profilePic: this.userService.getOwnProfilePic(),
    }).subscribe({
      next: (results: CompleteUserDetailResponse) => {
        console.log(results.profilePic);
        this.mapUserDetails(results.userDetails);
        this.profilePicUrl = URL.createObjectURL(results.profilePic) || environment.defaultProfilePic;
        this.hideSpinner();
      },
      error: (err: any) => {
        console.error('Error loading data:', err);
        this.showErrorMessage.set(true);
        this.hideSpinner();
      },
    });
  }

  toggleEditMode() {
    this.isEditModeActive.set(!this.isEditModeActive());
  }

  onSubmit() {

  }
}
