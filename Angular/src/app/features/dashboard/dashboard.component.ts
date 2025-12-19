import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { StatsOverviewComponent } from '../../shared/components/charts/stats-overview/stats-overview.component';
import { AppointmentChartComponent } from '../../shared/components/charts/appointment-chart/appointment-chart.component';
import { RiskDistributionChartComponent } from '../../shared/components/charts/risk-distribution-chart/risk-distribution-chart.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent {
  private authService = inject(AuthService);

  currentUser$ = this.authService.currentUser$;

  logout() {
    this.authService.logout();
  }
}
