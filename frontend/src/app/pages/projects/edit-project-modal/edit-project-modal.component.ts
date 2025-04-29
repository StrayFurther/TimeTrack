import { Component, inject, OnInit } from '@angular/core';
import {Project} from '../../../models/project';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import { MatLabel } from '@angular/material/form-field';

@Component({
  selector: 'edit-project-modal',
  imports: [ReactiveFormsModule, MatDialogClose, MatDialogActions, MatDialogContent, MatDialogTitle, MatLabel],
  templateUrl: './edit-project-modal.component.html',
  styleUrl: './edit-project-modal.component.scss'
})
export class EditProjectModalComponent implements OnInit {
  projectForm: FormGroup = new FormGroup({});
  project: Project = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<EditProjectModalComponent>);

  ngOnInit() {
    if (this.project) {
      this.projectForm = new FormGroup({
        name: new FormControl(this.project.name, [Validators.required]),
        description: new FormControl(this.project.description, [Validators.required]),
      });
    }
  }

  save() {
    if (this.projectForm?.valid) {
      this.dialogRef.close(this.projectForm.value);
    }
  }

  cancel() {
    this.dialogRef.close();
  }

}
