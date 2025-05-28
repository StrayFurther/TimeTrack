import {Component, computed, effect, inject, signal} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {PageHeaderComponent} from './pages/page-header/page-header.component';
import {filter} from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageHeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
  private router = inject(Router);
  private currentUrl = signal(this.router.url);

  constructor() {
    effect(() => {
      this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe(() => this.currentUrl.set(this.router.url));
    });
  }

  showHeader = computed(() => {
    const url = this.currentUrl();
    return !(url.startsWith('/login') || url.startsWith('/register'));
  });

}
