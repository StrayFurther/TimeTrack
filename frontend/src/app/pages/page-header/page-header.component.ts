import { Component, inject } from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatMenuModule} from '@angular/material/menu';
import {Router} from '@angular/router';

@Component({
  selector: 'page-header',
  imports: [MatButtonModule, MatIconModule, MatToolbarModule, MatMenuModule],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.scss'
})
export class PageHeaderComponent {
    private router = inject(Router);

    exportToPdf() {
        throw new Error('Method not implemented.');
    }

    goToProfile() {
      this.router.navigate(['/profile']);
    }

    goToLogin() {
      this.router.navigate(['/login']);
    }

    goToProjects() {
      this.router.navigate(['/projects']);
    }

    goToSettings() {
      this.router.navigate(['/settings']);
    }

}
