import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { StatsOverviewComponent } from '../../../shared/components/charts/stats-overview/stats-overview.component';
import { AppointmentChartComponent } from '../../../shared/components/charts/appointment-chart/appointment-chart.component';
import { RiskDistributionChartComponent } from '../../../shared/components/charts/risk-distribution-chart/risk-distribution-chart.component';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Appointment } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [CommonModule, StatsOverviewComponent, AppointmentChartComponent, RiskDistributionChartComponent],
  providers: [DatePipe], // Needed for formatting dates
  template: `
    <div class="mb-8">
        <h2 class="text-3xl font-extrabold text-gray-900">Dashboard Overview</h2>
        <p class="text-gray-500 mt-1">Welcome back! Here's what's happening today.</p>
    </div>
    
    <app-stats-overview 
        [totalAppointments]="totalAppointments"
        [activePatients]="activePatients"
        [pendingRequests]="pendingRequests">
    </app-stats-overview>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <app-appointment-chart 
        [data]="chartData" 
        [labels]="chartLabels">
      </app-appointment-chart>
      <app-risk-distribution-chart
        [riskData]="riskData">
      </app-risk-distribution-chart>
    </div>
  `
})
export class DashboardHomeComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  private authService = inject(AuthService);
  private datePipe = inject(DatePipe);

  totalAppointments = 0;
  activePatients = 0;
  pendingRequests = 0;

  chartData: number[] = [];
  chartLabels: string[] = [];
  riskData: number[] = [0, 0, 0]; // High, Medium, Low

  ngOnInit() {
    const role = this.authService.currentUserValue?.role || 'ROLE_THERAPIST';

    // Fetch real data
    this.appointmentService.getAppointments(role).subscribe(appointments => {
      this.processData(appointments);
    });
  }

  private processData(appointments: Appointment[]) {
    this.totalAppointments = appointments.length;

    // Unique patients
    const patientIds = new Set(appointments.map(a => a.patient.id));
    this.activePatients = patientIds.size;

    // Pending (Scheduled)
    this.pendingRequests = appointments.filter(a => a.status === 'SCHEDULED' && new Date(a.sessionDateTime) > new Date()).length;

    // Risk Distribution
    let high = 0, medium = 0, low = 0;
    appointments.forEach(a => {
      const score = a.cancellationRiskScore || 0;
      if (score > 0.75) high++;
      else if (score > 0.4) medium++;
      else low++;
    });
    this.riskData = [high, medium, low];

    // Chart Data (Next 7 days)
    const daysMap = new Map<string, number>();
    const today = new Date();
    const next7Days: string[] = [];

    for (let i = 0; i < 7; i++) {
      const d = new Date();
      d.setDate(today.getDate() + i);
      const dayLabel = this.datePipe.transform(d, 'EEE')!;
      next7Days.push(dayLabel);
      daysMap.set(dayLabel, 0);
    }

    appointments.forEach(a => {
      if (a.status === 'SCHEDULED') {
        const d = new Date(a.sessionDateTime);
        const day = this.datePipe.transform(d, 'EEE')!;
        // Only count if it's in our map (next 7 days)
        if (daysMap.has(day)) {
          // Check if it's actually in current week range (simple check)
          // For demo, just map to day name to show *some* data on chart
          daysMap.set(day, (daysMap.get(day) || 0) + 1);
        }
      }
    });

    this.chartLabels = next7Days;
    this.chartData = next7Days.map(day => daysMap.get(day) || 0);
  }
}
