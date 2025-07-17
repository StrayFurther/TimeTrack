import { HttpClient } from '@angular/common/http';
import {inject, Injectable } from '@angular/core';
import { environment } from '../../../env/env';
import {catchError, Observable, of, tap} from 'rxjs';
import {LoginResponse} from '../../models/login-response';
import { RegisterResponse } from '../../models/register-response';
import { User } from '../../models/user';
import {UserDetailComponent} from '../../pages/user/user-detail/user-detail.component';
import { UserDetailResponse } from '../../models/user-detail-response';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  private jwtToken?: string = localStorage.getItem(environment.jwtTokenName) || undefined;


  isAuthenticated(): boolean {
    return this.jwtToken !== null;
  }

  clearToken(): void {
    this.jwtToken = undefined;
    localStorage.removeItem(environment.jwtTokenName);
  }

  doesUserExist(email: string): Observable<boolean> {
    return this.httpClient.get<boolean>(`${this.apiUrl}/user/exists`, { params: { email } }).pipe(
      tap((exists: boolean) => {
        if (exists) {
          console.log(`User with email ${email} exists.`);
        } else {
          console.log(`User with email ${email} does not exist.`);
        }
        return exists;
      }),
      catchError((error) => {
        console.error('Check user existence failed', error);
        throw error; // Re-throw the error for further handling
      })
    );
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.httpClient.post<LoginResponse>(`${this.apiUrl}/user/login`, { email, password }).pipe(
      tap((response: LoginResponse) => {
        this.jwtToken = response.token;
        if (this.jwtToken) {
          localStorage.setItem(environment.jwtTokenName, this.jwtToken);
        }
      }),
      catchError((error) => of({ error })),
    );
  }

  register(email: string, password: string, userName: string): Observable<RegisterResponse> {
    return this.httpClient.post<RegisterResponse>(`${this.apiUrl}/user/register`, { email, password, userName }).pipe(
      catchError((error) => of({ error })),
    );
  }

  fetchActiveUser(): Observable<UserDetailResponse> {
    return this.httpClient.get<UserDetailResponse>(`${this.apiUrl}/user/details`, {
      headers: {
        Authorization: `Bearer ${this.jwtToken}`,
      },
    }).pipe(
      catchError((error) => {
        console.error('Failed to fetch user details', error);
        throw error; // Rethrow the error instead of returning an error object
      })
    );
  }
}
