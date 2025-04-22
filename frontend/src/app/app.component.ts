import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PageHeaderComponent} from './pages/page-header/page-header.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageHeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
