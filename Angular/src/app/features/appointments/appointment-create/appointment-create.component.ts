import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientService } from '../../../core/services/patient.service';
import { UserProfile } from '../../../core/models/appointment.models';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-appointment-create',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    template: `
    <div class="max-w-2xl mx-auto">
      <div class="mb-8">
        <h2 class="text-3xl font-extrabold text-gray-900">New Appointment</h2>
        <p class="mt-2 text-gray-600">Schedule a new session with a patient.</p>
      </div>

      <div class="bg-white rounded-2xl shadow-xl overflow-hidden">
        <div class="p-8">
          <form [formGroup]="createForm" (ngSubmit)="onSubmit()" class="space-y-6">
            
            <!-- Patient Selection -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Select Patient</label>
              <select formControlName="patientId" 
                      class="block w-full text-base border-gray-300 focus:outline-none focus:ring-teal-500 focus:border-teal-500 sm:text-sm rounded-lg p-3 bg-gray-50 transition-colors">
                <option value="">-- Choose a patient --</option>
                @for (patient of patients$ | async; track patient.id) {
                    <option [value]="patient.id">
                        {{ patient.firstName }} {{ patient.lastName }} ({{ patient.email }})
                    </option>
                }
              </select>
               @if (createForm.get('patientId')?.invalid && createForm.get('patientId')?.touched) {
                  <p class="mt-1 text-sm text-red-600">Please select a patient.</p>
               }
            </div>

            <!-- Date & Time -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                   <label class="block text-sm font-medium text-gray-700 mb-2">Date</label>
                   <input type="date" formControlName="date"
                          class="block w-full border-gray-300 rounded-lg shadow-sm focus:ring-teal-500 focus:border-teal-500 p-3 bg-gray-50">
                </div>
                <div>
                   <label class="block text-sm font-medium text-gray-700 mb-2">Time</label>
                   <input type="time" formControlName="time"
                          class="block w-full border-gray-300 rounded-lg shadow-sm focus:ring-teal-500 focus:border-teal-500 p-3 bg-gray-50">
                </div>
            </div>

             <!-- Notes -->
             <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Notes (Optional)</label>
              <textarea formControlName="notes" rows="4" 
                        class="block w-full border-gray-300 rounded-lg shadow-sm focus:ring-teal-500 focus:border-teal-500 p-3 bg-gray-50"
                        placeholder="Add any initial notes for this session..."></textarea>
            </div>

            <!-- Buttons -->
            <div class="pt-6 flex items-center justify-end space-x-4 border-t border-gray-100 mt-8">
              <button type="button" (click)="cancel()" 
                      class="px-6 py-3 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 transition-colors">
                Cancel
              </button>
              <button type="submit" [disabled]="createForm.invalid || isSubmitting"
                      class="px-8 py-3 bg-gradient-to-r from-teal-600 to-teal-500 border border-transparent rounded-lg text-sm font-bold text-white shadow-md hover:from-teal-700 hover:to-teal-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:translate-y-px">
                {{ isSubmitting ? 'Booking...' : 'Book Appointment' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class AppointmentCreateComponent implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private appointmentService = inject(AppointmentService);
    private patientService = inject(PatientService);

    patients$!: Observable<UserProfile[]>;
    isSubmitting = false;

    createForm = this.fb.group({
        patientId: ['', Validators.required],
        date: ['', Validators.required],
        time: ['', Validators.required],
        notes: ['']
    });

    ngOnInit() {
        this.patients$ = this.patientService.getPatients();
    }

    onSubmit() {
        if (this.createForm.valid) {
            this.isSubmitting = true;
            const { patientId, date, time, notes } = this.createForm.value;

            // Combine date and time to ISO string
            const sessionDateTime = new Date(`${date}T${time}:00`).toISOString();

            const request = {
                patientId: Number(patientId),
                therapistId: 0, // Backend should infer this from token or we need to pass it
                sessionDateTime,
                notes: notes || undefined
            };

            this.appointmentService.createAppointment(request).subscribe({
                next: () => {
                    this.router.navigate(['/dashboard/appointments']);
                },
                error: (err) => {
                    console.error('Failed to create appointment', err);
                    this.isSubmitting = false;
                }
            });
        }
    }

    cancel() {
        this.router.navigate(['/dashboard/appointments']);
    }
}
