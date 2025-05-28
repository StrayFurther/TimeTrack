import { Routes } from '@angular/router';
import { TicketComponent } from './pages/ticket/ticket/ticket.component';
import { TicketOverviewComponent } from './pages/ticket/ticket-overview/ticket-overview.component';
import {TicketDetailComponent} from './pages/ticket/ticket-detail/ticket-detail.component';
import {ProjectsComponent} from './pages/projects/projects.component';
import {RegisterComponent} from './pages/register/register.component';
import {LoginComponent} from './pages/login/login.component';

export const routes: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'project', component: ProjectsComponent },
  { path: '', redirectTo: 'project', pathMatch: 'full' }
];
