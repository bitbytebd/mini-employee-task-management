import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SessionUser { token: string; type: string; email: string; fullName: string; username: string; roles: string[]; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly key = 'fifo-session';
  currentUser: SessionUser | null = this.read();
  constructor(private http: HttpClient) {}
  login(email: string, password: string): Observable<SessionUser> {
    return this.http.post<SessionUser>(`${environment.apiBaseUrl}/auth/login`, { email, password }).pipe(tap(user => {
      this.currentUser = user;
      localStorage.setItem(this.key, JSON.stringify(user));
    }));
  }
  logout(): void { this.currentUser = null; localStorage.removeItem(this.key); }
  isLoggedIn(): boolean { return !!this.currentUser?.token; }
  token(): string | null { return this.currentUser?.token || null; }
  hasRole(role: string): boolean { return !!this.currentUser?.roles?.includes(role); }
  primaryRole(): string { return this.currentUser?.roles?.[0] || ''; }
  private read(): SessionUser | null { try { return JSON.parse(localStorage.getItem(this.key) || 'null'); } catch { return null; } }
}
