import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { ChartConfiguration, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
    selector: 'app-appointment-chart',
    imports: [BaseChartDirective],
    template: `
    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 h-full">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Appointment Trends (Next 7 Days)</h3>
      <div class="h-64">
        <canvas baseChart
          [data]="lineChartData"
          [options]="lineChartOptions"
          [type]="lineChartType">
        </canvas>
      </div>
    </div>
  `
})
export class AppointmentChartComponent implements OnChanges {
    @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
    @Input() data: number[] = [];
    @Input() labels: string[] = [];

    public lineChartData: ChartConfiguration['data'] = {
        datasets: [
            {
                data: [],
                label: 'Appointments',
                backgroundColor: 'rgba(0, 121, 107, 0.2)',
                borderColor: 'rgba(0, 121, 107, 1)',
                pointBackgroundColor: 'rgba(0, 121, 107, 1)',
                pointBorderColor: '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: 'rgba(0, 121, 107, 0.8)',
                fill: 'origin',
                tension: 0.4
            }
        ],
        labels: []
    };

    public lineChartOptions: ChartConfiguration['options'] = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                padding: 12,
                titleFont: { size: 14, weight: 'bold' },
                bodyFont: { size: 13 }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                grid: {
                    color: 'rgba(0, 0, 0, 0.05)'
                },
                ticks: {
                    stepSize: 1
                }
            },
            x: {
                grid: {
                    display: false
                }
            }
        }
    };

    public lineChartType: ChartType = 'line';

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['data'] || changes['labels']) {
            this.updateChart();
        }
    }

    private updateChart() {
        this.lineChartData.datasets[0].data = this.data;
        this.lineChartData.labels = this.labels;
        this.chart?.update();
    }
}
