import { Component, EventEmitter, inject, Output } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-file-explorer-dialog',
  imports: [],
  templateUrl: './file-explorer-dialog.component.html',
  styleUrl: './file-explorer-dialog.component.scss'
})
export class FileExplorerDialogComponent {
  @Output() fileSelected = new EventEmitter<File | null>();
  @Output() deleteRequested = new EventEmitter<void>();

  dialogRef = inject(MatDialogRef<FileExplorerDialogComponent>);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.fileSelected.emit(input.files[0]);
      this.dialogRef.close();
    }
  }

  onDeleteFile(): void {
    this.deleteRequested.emit();
    this.dialogRef.close();
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
