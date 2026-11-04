import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ApiService, Task, User } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
@Component({ selector: 'app-tasks', templateUrl: './tasks.component.html', styleUrls: ['./tasks.component.css'] })
export class TasksComponent implements OnInit {
  tasks: Task[] = []; users: User[] = []; error = ''; editing?: Task;
  form = this.fb.group({ title: ['', Validators.required], description: [''], priority: ['MEDIUM', Validators.required], assigneeId: ['', Validators.required], deadline: ['', Validators.required] });
  constructor(public auth: AuthService, private api: ApiService, private fb: FormBuilder) {}
  ngOnInit(): void { this.load(); if (this.auth.hasRole('ADMIN')) { this.api.users().subscribe({ next: page => this.users = page.content.filter(user => user.roles.includes('EMPLOYEE') && user.enabled) }); } else if (this.auth.hasRole('MANAGER')) { this.api.team().subscribe({ next: users => this.users = users }); } }
  load(): void { this.api.tasks().subscribe({ next: page => this.tasks = page.content, error: () => this.error = 'Unable to load tasks.' }); }
  canManage(): boolean { return this.auth.hasRole('ADMIN') || this.auth.hasRole('MANAGER'); }
  edit(task: Task): void { this.editing = task; this.form.patchValue({ title: task.title, description: task.description || '', priority: task.priority, assigneeId: String(task.assigneeId), deadline: task.deadline }); }
  reset(): void { this.editing = undefined; this.form.reset({ priority: 'MEDIUM', assigneeId: '', description: '', deadline: '', title: '' }); }
  submit(): void { if (this.form.invalid) { this.form.markAllAsTouched(); return; } const request = this.editing ? this.api.updateTask(this.editing.id, this.form.getRawValue()) : this.api.createTask(this.form.getRawValue()); request.subscribe({ next: () => { this.reset(); this.load(); }, error: err => this.error = err.error?.message || 'Unable to save task.' }); }
  advance(task: Task): void { const next: {[key: string]: string} = { PENDING: 'IN_PROGRESS', IN_PROGRESS: 'DONE' }; if (next[task.status]) { this.api.updateTaskStatus(task.id, next[task.status]).subscribe({ next: () => this.load(), error: err => this.error = err.error?.message || 'Unable to update status.' }); } }
  remove(task: Task): void { if (confirm(`Delete ${task.title}?`)) { this.api.deleteTask(task.id).subscribe({ next: () => this.load(), error: err => this.error = err.error?.message || 'Unable to delete task.' }); } }
  report(): void { this.api.downloadReport().subscribe({ next: file => { const url = URL.createObjectURL(file); const link = document.createElement('a'); link.href = url; link.download = 'tasks-report.pdf'; link.click(); URL.revokeObjectURL(url); }, error: () => this.error = 'Report download is unavailable for this role.' }); }
}
