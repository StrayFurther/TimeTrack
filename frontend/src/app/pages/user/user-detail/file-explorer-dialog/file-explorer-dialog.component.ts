import { Component, EventEmitter, inject, Output } from '@angular/core';
import {
  MatDialog,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {ConfirmationDialogComponent} from '../../../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-file-explorer-dialog',
  imports: [
    MatDialogActions,
    MatDialogTitle,
    MatDialogContent,
    MatButton,
  ],
  templateUrl: './file-explorer-dialog.component.html',
  styleUrl: './file-explorer-dialog.component.scss'
})
export class FileExplorerDialogComponent {
  dialogRef = inject(MatDialogRef);
  dialog = inject(MatDialog)

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  previewMode: boolean = false;

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement)?.files?.[0];
    if (file && file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
      this.selectedFile = file;
      this.previewMode = true;
    }
  }

  openConfirmationDialog(title: string, message: string, callback: (result: any) => any): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent);

    const instance = dialogRef.componentInstance;
    instance.title = title;
    instance.message = message;

    dialogRef.afterClosed().subscribe(callback);
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent);
    this.openConfirmationDialog("Delete Confirmation", "Are you sure you want to delete your Profile Picture?", result => {
      console.log("brooo" , result);
      if (result) {
        dialogRef.close();
        this.dialogRef.close({delete: true});
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  confirmUpload() {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent);
    this.openConfirmationDialog("Upload Selected File", "Are you sure you want to upload this picture?", result => {
      if (result) {
        console.log("brooo" , result, this.selectedFile);

        dialogRef.close();
        this.dialogRef.close({file: this.selectedFile});
      }
    });
  }

  cancelSelection() {
    this.dialogRef.close();
  }
}
