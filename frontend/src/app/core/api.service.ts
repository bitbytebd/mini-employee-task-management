import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }
export interface User { id: number; email: string; username: string; fullName: string; roles: string[]; enabled: boolean; managerId?: number; managerName?: string; }
export interface Task { id: number; title: string; description?: string; status: string; priority: string; assigneeId: number; assigneeName: string; createdById: number; createdByName: string; deadline: string; }
export interface DashboardStats { userCount: number; taskCount: number; pendingCount: number; inProgressCount: number; completedCount: number; }
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080/api';
  constructor(private http: HttpClient) {}
  getDashboard(): Observable<DashboardStats> { return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard/stats`); }
  users(page = 0, size = 100, search = ''): Observable<Page<User>> { return this.http.get<Page<User>>(`${this.baseUrl}/users`, { params: new HttpParams().set('page', page).set('size', size).set('search', search) }); }
  team(): Observable<User[]> { return this.http.get<User[]>(`${this.baseUrl}/users/team`); }
  createUser(user: unknown): Observable<User> { return this.http.post<User>(`${this.baseUrl}/users`, user); }
  updateUser(id: number, user: unknown): Observable<User> { return this.http.put<User>(`${this.baseUrl}/users/${id}`, user); }
  deleteUser(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/users/${id}`); }
  tasks(page = 0, size = 50, filters: {[key: string]: string} = {}): Observable<Page<Task>> { let params = new HttpParams().set('page', page).set('size', size); Object.keys(filters).forEach(key => { if (filters[key]) { params = params.set(key, filters[key]); } }); return this.http.get<Page<Task>>(`${this.baseUrl}/tasks`, { params }); }
  createTask(task: unknown): Observable<Task> { return this.http.post<Task>(`${this.baseUrl}/tasks`, task); }
  updateTask(id: number, task: unknown): Observable<Task> { return this.http.put<Task>(`${this.baseUrl}/tasks/${id}`, task); }
  updateTaskStatus(id: number, status: string): Observable<Task> { return this.http.put<Task>(`${this.baseUrl}/tasks/${id}/status`, { status }); }
  deleteTask(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/tasks/${id}`); }
  downloadReport(): Observable<Blob> { return this.http.get(`${this.baseUrl}/reports/tasks/pdf`, { responseType: 'blob' }); }
}
