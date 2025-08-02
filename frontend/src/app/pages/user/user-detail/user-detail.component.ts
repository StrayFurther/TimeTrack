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
import {catchError, forkJoin, of} from 'rxjs';
import { environment } from '../../../../env/env';
import {mapToRegularUserUpdatePayload} from '../../../models/user-update-request';
import {MatIcon} from '@angular/material/icon';
import {FileExplorerDialogComponent} from './file-explorer-dialog/file-explorer-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {MatCard, MatCardContent } from '@angular/material/card';


@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatFormField, MatSelect, MatOption, MatButton, MatInput, MatError, MatLabel, MatIcon, MatCard, MatCardContent],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent implements OnInit {
  dialog = inject(MatDialog);
  userService = inject(UserService);
  loadingSpinnerService = inject(LoadingSpinnerService);

  roles = getRoleEnumValues();
  showErrorMessage = signal(false);
  errorMessage = signal('An error occurred while loading user data. Please try again later.');
  userForm = new FormGroup({
    userName: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    role: new FormControl({ value: '', disabled: true }, Validators.required),
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
      role: new FormControl({ value: stringToEnum(resp.role)?.toString() || '', disabled: true }, Validators.required), // Map role string to Role enum
    });
  }

  showSpinner(message: string) {
    this.loadingSpinnerService.showSpinner = true;
    this.loadingSpinnerService.message = message;
  }

  async loadUserData() {
    this.showSpinner("Loading user data...");
    this.showErrorMessage.set(false)
    forkJoin({
      userDetails: this.userService.fetchActiveUser(),
      profilePic: this.userService.getOwnProfilePic().pipe(
        catchError((err: any, _) => {
          console.error('Error fetching profile picture:', err);
          return of(null); // Return null or a default value if `getOwnProfilePic` fails
        })
      ),
    }).subscribe({
      next: (results: CompleteUserDetailResponse) => {
        console.log(results.profilePic);
        this.mapUserDetails(results.userDetails);
        this.profilePicUrl = results.profilePic ? URL.createObjectURL(results.profilePic) : environment.defaultProfilePic;
        this.loadingSpinnerService.clearSpinner();
      },
      error: (err: any) => {
        console.error('Error loading data:', err);
        this.showErrorMessage.set(true);
        this.errorMessage.set('Failed to load user data. Please try again later.');
        this.loadingSpinnerService.clearSpinner();
      },
    });
  }

  toggleEditMode() {
    this.isEditModeActive.set(!this.isEditModeActive());
  }

  onSubmit() {
    if (this.userForm.valid) {
      this.showSpinner("Updating user details...");
      this.userService.updateUserDetails(mapToRegularUserUpdatePayload(this.userForm.value)).subscribe((results: UserDetailResponse) => {
          console.log('User details updated successfully:', results);
          this.mapUserDetails(results);
          this.isEditModeActive.set(false);
          this.loadingSpinnerService.clearSpinner();
        },
        (err: any) => {
          console.error('Error updating user details:', err);
          this.showErrorMessage.set(true);
          this.errorMessage.set('Failed to update user details. Please try again.');
          this.loadingSpinnerService.clearSpinner();
        }
      );
    }
  }

  startChangeProfilePictureProcess(): void {
    const dialogRef = this.dialog.open(FileExplorerDialogComponent);

    dialogRef.afterClosed().subscribe((file) => {
      console.log('File explorer dialog closed', file);
    });
  }
}
