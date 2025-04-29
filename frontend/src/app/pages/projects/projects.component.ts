import { Component } from '@angular/core';
import {CommonModule, DatePipe} from "@angular/common";
import {MatButton} from "@angular/material/button";
import {
    MatCard,
    MatCardActions,
    MatCardContent,
    MatCardHeader,
    MatCardTitle
} from "@angular/material/card";
import {MOCK_PROJECTS} from '../../mocks/projects';

@Component({
  selector: 'app-projects',
    imports: [
      MatCard,
      MatCardTitle,
      MatCardHeader,
      MatCardContent,
      MatButton,
      MatCardActions,
      DatePipe,
      CommonModule,
    ],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.scss'
})
export class ProjectsComponent {
    projects = MOCK_PROJECTS

}
