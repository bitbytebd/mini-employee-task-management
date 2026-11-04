import { Component } from '@angular/core';
import { ApiService, DashboardStats } from '../../core/api.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  stats?: DashboardStats;
  error = '';
  constructor(private api: ApiService) {
    this.api.getDashboard().subscribe({ next: stats => this.stats = stats, error: () => this.error = 'The dashboard could not be loaded.' });
  }
}
