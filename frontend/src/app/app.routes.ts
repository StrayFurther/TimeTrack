import { Routes } from '@angular/router';
import {ProjectsComponent} from './pages/projects/projects.component';
import {RegisterComponent} from './pages/register/register.component';
import {LoginComponent} from './pages/login/login.component';
import {UserDetailComponent} from './pages/user/user-detail/user-detail.component';

export const routes: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'user', loadComponent: () => UserDetailComponent },
  { path: 'project', component: ProjectsComponent },
  { path: '', redirectTo: 'project', pathMatch: 'full' }
];
