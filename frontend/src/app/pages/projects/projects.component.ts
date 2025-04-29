import { Component, inject } from '@angular/core';
import {CommonModule, DatePipe} from "@angular/common";
import {MatButton} from "@angular/material/button";
import {
    MatCard,
    MatCardActions,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
} from "@angular/material/card";
import {MOCK_PROJECTS} from '../../mocks/projects';
import {EditProjectModalComponent} from './edit-project-modal/edit-project-modal.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'projects',
    imports: [
      MatCard,
      MatCardTitle,
      MatCardHeader,
      MatCardContent,
      MatButton,
      MatCardActions,
      DatePipe,
      CommonModule
    ],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.scss'
})
export class ProjectsComponent {
    projects = MOCK_PROJECTS
    dialog = inject(MatDialog)

    onClickEdit(id: number) {
      const dialogRef = this.dialog.open(EditProjectModalComponent, {
        width: '400px',
        data: this.projects.find(p => p.id === id)
      });
    }

}
