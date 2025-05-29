import { HttpClient } from '@angular/common/http';
import {inject, Injectable } from '@angular/core';
import { environment } from '../../env/env';
import {catchError, Observable, tap} from 'rxjs';
import {LoginResponse} from '../models/login-response';
import { RegisterResponse } from '../models/register-response';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private user: any = null;
  private httpClient = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  private jwtToken: string | null = localStorage.getItem(environment.jwtTokenName);

  setUser(user: any): void {
    this.user = user;
  }

  getUser(): any {
    return this.user;
  }

  isAuthenticated(): boolean {
    return this.user !== null;
  }

  clearUser(): void {
    this.user = null;
    this.jwtToken = null;
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
        localStorage.setItem(environment.jwtTokenName, this.jwtToken);
      }),
      catchError((error) => {
        console.error('Login failed', error);
        throw error; // Re-throw the error for further handling
      })
    );
  }

  register(email: string, password: string, userName: string): Observable<RegisterResponse> {
    return this.httpClient.post<RegisterResponse>(`${this.apiUrl}/user/register`, { email, password, userName }).pipe(
      catchError((error) => {
        console.error('Registration failed', error);
        throw error; // Re-throw the error for further handling
      })
    );
  }
}
