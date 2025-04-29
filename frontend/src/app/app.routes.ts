import { Routes } from '@angular/router';
import { TicketComponent } from './pages/ticket/ticket/ticket.component';
import { TicketOverviewComponent } from './pages/ticket/ticket-overview/ticket-overview.component';
import {TicketDetailComponent} from './pages/ticket/ticket-detail/ticket-detail.component';
import {ProjectsComponent} from './pages/projects/projects.component';

export const routes: Routes = [
  {
    path: 'ticket',
    component: TicketComponent,
    children: [
      { path: '', component: TicketOverviewComponent },
      { path: ':id', component: TicketDetailComponent }
    ]
  },
  { path: 'project', component: ProjectsComponent },
  { path: '', redirectTo: 'project', pathMatch: 'full' }
];
