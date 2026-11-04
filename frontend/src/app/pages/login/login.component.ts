import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
@Component({ selector: 'app-login', templateUrl: './login.component.html', styleUrls: ['./login.component.css'] })
export class LoginComponent {
  error = ''; loading = false;
  form = this.fb.group({ email: ['', [Validators.required, Validators.email]], password: ['', Validators.required] });
  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}
  submit(): void { if (this.form.invalid) { this.form.markAllAsTouched(); return; } this.loading = true; this.error = ''; const value = this.form.getRawValue(); this.auth.login(value.email || '', value.password || '').subscribe({ next: () => this.router.navigate(['/dashboard']), error: err => { this.loading = false; this.error = err.status === 401 ? 'Email or password is incorrect.' : 'Unable to reach the server.'; } }); }
}
