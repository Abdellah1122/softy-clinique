import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PatientService } from '../../../core/services/patient.service';
import { UserProfile } from '../../../core/models/appointment.models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-3xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-teal-400">
          My Patients
        </h2>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        @for (patient of patients$ | async; track patient.id) {
          <div class="group relative bg-white/80 backdrop-blur-md rounded-2xl shadow-sm border border-white/20 p-6 flex flex-col items-center text-center transition-all duration-300 hover:shadow-xl hover:-translate-y-1">
            <div class="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
                <button class="text-gray-400 hover:text-teal-600">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"></path></svg>
                </button>
            </div>

            <div class="h-24 w-24 rounded-full bg-gradient-to-br from-teal-100 to-teal-200 flex items-center justify-center text-teal-700 text-3xl font-bold mb-4 shadow-inner ring-4 ring-white">
              {{ patient.firstName.charAt(0) }}{{ patient.lastName.charAt(0) }}
            </div>
            
            <h3 class="text-xl font-bold text-gray-900 group-hover:text-teal-600 transition-colors">
                {{ patient.firstName }} {{ patient.lastName }}
            </h3>
            <p class="text-sm font-medium text-gray-500 mb-6 bg-gray-100 px-3 py-1 rounded-full mt-2">
                {{ patient.email }}
            </p>

            <div class="w-full grid grid-cols-2 gap-2 mt-auto">
                <button class="flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-teal-700 bg-teal-50 hover:bg-teal-100 transition-colors">
                    Profile
                </button>
                <button class="flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-teal-600 hover:bg-teal-700 transition-colors">
                    History
                </button>
            </div>
          </div>
        } @empty {
            <div class="col-span-full flex flex-col items-center justify-center py-16 bg-white/50 rounded-3xl border border-dashed border-gray-300">
                <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                     <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                </div>
                <h3 class="text-lg font-medium text-gray-900">No patients found</h3>
                <p class="text-gray-500 mt-1">Start by adding patients to your list.</p>
            </div>
        }
      </div>
    </div>
  `
})
export class PatientListComponent implements OnInit {
  private patientService = inject(PatientService);
  patients$!: Observable<UserProfile[]>;

  ngOnInit() {
    this.patients$ = this.patientService.getPatients();
  }
}
