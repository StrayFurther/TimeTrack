import { Routes } from '@angular/router';
import { TicketOverviewComponent } from './pages/ticket-overview/ticket-overview.component';

export const routes: Routes = [
  { path: 'home', component: TicketOverviewComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' }
];
