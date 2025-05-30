import { Component, inject } from '@angular/core';
import {LoadingSpinnerService} from '../../services/loading-spinner/loading-spinner.service';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

@Component({
  selector: 'custom-loading-spinner',
  imports: [
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatProgressSpinner
  ],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.scss'
})
export class LoadingSpinnerComponent {
  loadingSpinnerService = inject(LoadingSpinnerService);
  showLoadingSpinner = this.loadingSpinnerService.showSpinnerListener;
  spinnerMessage = this.loadingSpinnerService.spinnerMessageListener;
}
