import {Component, computed, effect, inject, signal} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {PageHeaderComponent} from './pages/page-header/page-header.component';
import {filter} from 'rxjs';
import {LoadingSpinnerService} from './services/loading-spinner/loading-spinner.service';
import {LoadingSpinnerComponent} from './shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageHeaderComponent, LoadingSpinnerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private router = inject(Router);
  private loadingSpinnerService = inject(LoadingSpinnerService);
  private currentUrl = signal(this.router.url);

  title = 'frontend';
  showLoadingSpinner = this.loadingSpinnerService.showSpinnerListener;

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
