import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ApiService, User } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
@Component({ selector: 'app-users', templateUrl: './users.component.html', styleUrls: ['./users.component.css'] })
export class UsersComponent implements OnInit {
  users: User[] = []; error = ''; editing?: User;
  form = this.fb.group({ fullName: ['', Validators.required], username: ['', Validators.required], email: ['', [Validators.required, Validators.email]], password: [''], roles: ['EMPLOYEE', Validators.required], enabled: [true], managerId: [''] });
  constructor(public auth: AuthService, private api: ApiService, private fb: FormBuilder) {}
  ngOnInit(): void { this.load(); }
  load(): void { if (this.auth.hasRole('ADMIN')) { this.api.users().subscribe({ next: page => this.users = page.content, error: () => this.error = 'Unable to load users.' }); } }
  edit(user: User): void { this.editing = user; this.form.patchValue({ fullName: user.fullName, username: user.username, email: user.email, roles: user.roles[0], enabled: user.enabled, managerId: user.managerId ? String(user.managerId) : '', password: '' }); }
  reset(): void { this.editing = undefined; this.form.reset({ roles: 'EMPLOYEE', enabled: true, managerId: '' }); }
  submit(): void { if (this.form.invalid) { this.form.markAllAsTouched(); return; } const raw = this.form.getRawValue(); const value: any = { ...raw, roles: [raw.roles], managerId: raw.managerId || null }; const request = this.editing ? this.api.updateUser(this.editing.id, value) : this.api.createUser(value); request.subscribe({ next: () => { this.reset(); this.load(); }, error: err => this.error = err.error?.message || 'Unable to save user.' }); }
  remove(user: User): void { if (confirm(`Delete ${user.fullName}?`)) { this.api.deleteUser(user.id).subscribe({ next: () => this.load(), error: err => this.error = err.error?.message || 'Unable to delete user.' }); } }
}
