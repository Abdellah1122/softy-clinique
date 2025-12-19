import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { ChartConfiguration, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
    selector: 'app-risk-distribution-chart',
    standalone: true,
    imports: [BaseChartDirective],
    template: `
    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 h-full">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Risk Distribution</h3>
      <div class="h-64 flex items-center justify-center">
         @if (hasData) {
            <canvas baseChart
              [data]="pieChartData"
              [type]="pieChartType"
              [options]="pieChartOptions">
            </canvas>
         } @else {
            <div class="text-gray-400 text-sm">No risk data available</div>
         }
      </div>
    </div>
  `
})
export class RiskDistributionChartComponent implements OnChanges {
    @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
    @Input() riskData: number[] = [0, 0, 0]; // High, Medium, Low

    get hasData(): boolean {
        return this.riskData.some(v => v > 0);
    }

    public pieChartOptions: ChartConfiguration['options'] = {
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'right',
                labels: {
                    usePointStyle: true,
                    padding: 20,
                    font: {
                        family: "'Plus Jakarta Sans', sans-serif"
                    }
                }
            },
        }
    };

    public pieChartData: ChartConfiguration['data'] = {
        labels: ['High Risk', 'Medium Risk', 'Low Risk'],
        datasets: [
            {
                data: [],
                backgroundColor: ['#EF5350', '#FFA726', '#66BB6A'],
                hoverBackgroundColor: ['#E53935', '#FB8C00', '#43A047'],
                borderWidth: 0,
                hoverOffset: 4
            },
        ],
    };

    public pieChartType: ChartType = 'doughnut';

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['riskData']) {
            this.updateChart();
        }
    }

    private updateChart() {
        this.pieChartData.datasets[0].data = this.riskData;
        this.chart?.update();
    }
}
