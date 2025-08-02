import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import {MatButton} from "@angular/material/button";
import {MatDialogActions, MatDialogContent, MatDialogTitle, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-confirmation-dialog',
    imports: [
        MatButton,
        MatDialogActions,
        MatDialogContent,
        MatDialogTitle
    ],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent {

  @Input() title: string = '';
  @Input() message: string = '';

  dialogRef = inject(MatDialogRef<ConfirmationDialogComponent>);

  onCancel() {
      this.dialogRef.close(false);
  }

  onConfirm() {
      this.dialogRef.close(true);
  }

}
