import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stats-overview',
  imports: [],
  template: `
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
        <div class="flex items-center justify-between mb-2">
            <p class="text-sm font-semibold text-gray-500 uppercase tracking-wider">Total Appointments</p>
            <div class="p-2 bg-teal-50 rounded-lg text-teal-600">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
            </div>
        </div>
        <h3 class="text-3xl font-extrabold text-gray-900">{{ totalAppointments }}</h3>
        <p class="text-xs text-green-600 mt-1 font-medium flex items-center">
            <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
            +12% from last month
        </p>
      </div>

      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
        <div class="flex items-center justify-between mb-2">
            <p class="text-sm font-semibold text-gray-500 uppercase tracking-wider">Active Patients</p>
            <div class="p-2 bg-blue-50 rounded-lg text-blue-600">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            </div>
        </div>
        <h3 class="text-3xl font-extrabold text-gray-900">{{ activePatients }}</h3>
         <p class="text-xs text-gray-400 mt-1 font-medium">Updated just now</p>
      </div>

      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
        <div class="flex items-center justify-between mb-2">
           <p class="text-sm font-semibold text-gray-500 uppercase tracking-wider">Pending Requests</p>
           <div class="p-2 bg-orange-50 rounded-lg text-orange-600">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
           </div>
        </div>
        <h3 class="text-3xl font-extrabold text-gray-900">{{ pendingRequests }}</h3>
        <p class="text-xs text-orange-600 mt-1 font-medium">Requires attention</p>
      </div>
    </div>
  `
})
export class StatsOverviewComponent {
  @Input() totalAppointments = 0;
  @Input() activePatients = 0;
  @Input() pendingRequests = 0;
}
