import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Appointment } from '../../../core/models/appointment.models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, DecimalPipe, FormsModule],
  template: `
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-3xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-teal-400">
          Appointments
        </h2>
        @if (isTherapist) {
            <a routerLink="/dashboard/appointments/create" 
               class="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-full shadow-lg text-white bg-gradient-to-r from-teal-500 to-teal-700 hover:from-teal-600 hover:to-teal-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 transition-all transform hover:scale-105">
               <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
               New Appointment
            </a>
        }
      </div>

      <div class="grid gap-6">
        @for (appointment of appointments$ | async; track appointment.id) {
          <div class="group relative bg-white/80 backdrop-blur-md rounded-2xl shadow-sm border border-white/20 p-5 transition-all duration-300 hover:shadow-xl hover:-translate-y-1">
            <div class="flex flex-col sm:flex-row items-start sm:items-center">
              
              <!-- Date Box -->
              <div class="flex-shrink-0 bg-gradient-to-br from-teal-50 to-teal-100 rounded-2xl p-4 text-center min-w-[80px] border border-teal-100/50 shadow-inner">
                <div class="text-xs font-bold text-teal-800 uppercase tracking-wider mb-1">{{ appointment.sessionDateTime | date:'EEE' }}</div>
                <div class="text-2xl font-black text-teal-700 leading-none mb-1">{{ appointment.sessionDateTime | date:'d' }}</div>
                <div class="text-xs font-medium text-teal-600/80">{{ appointment.sessionDateTime | date:'shortTime' }}</div>
              </div>

              <!-- Content -->
              <div class="mt-4 sm:mt-0 sm:ml-6 flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                    <h3 class="text-xl font-bold text-gray-900 truncate">
                    {{ isTherapist ? appointment.patient.firstName + ' ' + appointment.patient.lastName : 'Therapist: ' + appointment.therapist.firstName + ' ' + appointment.therapist.lastName }}
                    </h3>
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium capitalize"
                          [class.bg-green-100]="appointment.status === 'COMPLETED'"
                          [class.text-green-800]="appointment.status === 'COMPLETED'"
                          [class.bg-blue-100]="appointment.status === 'SCHEDULED'"
                          [class.text-blue-800]="appointment.status === 'SCHEDULED'"
                          [class.bg-red-100]="appointment.status === 'CANCELLED'"
                          [class.text-red-800]="appointment.status === 'CANCELLED'">
                      {{ appointment.status.toLowerCase() }}
                    </span>
                </div>
                <p class="text-sm text-gray-500 font-medium">
                    {{ isTherapist ? 'Patient' : 'Session' }}
                </p>
                
                <!-- Notes Teaser -->
                @if (appointment.notes) {
                    <p class="mt-2 text-sm text-gray-600 italic line-clamp-1 border-l-4 border-teal-200 pl-3">
                        "{{ appointment.notes }}"
                    </p>
                }
              </div>

              <!-- Actions & Risk -->
              <div class="mt-4 sm:mt-0 sm:ml-6 flex items-center space-x-4">
                
                <!-- Risk Score (Therapist Only) -->
                @if (isTherapist && appointment.cancellationRiskScore !== undefined) {
                  <div class="relative group/tooltip">
                    <div class="relative inline-flex items-center justify-center w-14 h-14 rounded-full border-[3px]"
                         [class.border-emerald-400]="(appointment.cancellationRiskScore || 0) <= 0.4"
                         [class.border-amber-400]="(appointment.cancellationRiskScore || 0) > 0.4 && (appointment.cancellationRiskScore || 0) <= 0.75"
                         [class.border-rose-400]="(appointment.cancellationRiskScore || 0) > 0.75"
                         [class.bg-emerald-50]="(appointment.cancellationRiskScore || 0) <= 0.4"
                         [class.bg-amber-50]="(appointment.cancellationRiskScore || 0) > 0.4 && (appointment.cancellationRiskScore || 0) <= 0.75"
                         [class.bg-rose-50]="(appointment.cancellationRiskScore || 0) > 0.75">
                      <span class="text-xs font-bold"
                            [class.text-emerald-700]="(appointment.cancellationRiskScore || 0) <= 0.4"
                            [class.text-amber-700]="(appointment.cancellationRiskScore || 0) > 0.4 && (appointment.cancellationRiskScore || 0) <= 0.75"
                            [class.text-rose-700]="(appointment.cancellationRiskScore || 0) > 0.75">
                        {{ (appointment.cancellationRiskScore || 0) * 100 | number:'1.0-0' }}%
                      </span>
                    </div>
                    <span class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-gray-800 rounded opacity-0 group-hover/tooltip:opacity-100 transition-opacity whitespace-nowrap">
                        Cancellation Risk
                    </span>
                  </div>
                }

                <!-- Action Buttons -->
                <div class="flex space-x-2">
                    @if (isTherapist) {
                         <!-- Add Note -->
                        <button (click)="openNoteModal(appointment)" 
                                class="p-2 text-gray-500 hover:bg-gray-100 rounded-full transition-colors"
                                title="Add Clinical Note">
                            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                    }

                    @if (appointment.status === 'SCHEDULED') {
                        @if (isTherapist) {
                            <button (click)="completeAppointment(appointment.id)" 
                                    class="p-2 text-teal-600 hover:bg-teal-50 rounded-full transition-colors"
                                    title="Complete Appointment">
                                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                            </button>
                        } @else {
                             <!-- Patient Cancel -->
                            <button (click)="cancelAppointment(appointment.id)"
                                    class="p-2 text-red-500 hover:bg-red-50 rounded-full transition-colors"
                                    title="Cancel Appointment">
                                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                            </button>
                        }
                    }
                </div>
              </div>
            </div>
          </div>
        } @empty {
            <div class="flex flex-col items-center justify-center py-16 bg-white/50 rounded-3xl border border-dashed border-gray-300">
                 <!-- Empty State Same as Before -->
                <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                    <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                </div>
                <h3 class="text-lg font-medium text-gray-900">No appointments yet</h3>
            </div>
        }
      </div>
    </div>

    <!-- Note Modal -->
    @if (isNoteModalOpen) {
        <div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
            <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" (click)="closeNoteModal()"></div>

                <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

                <div class="relative inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
                    <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                        <div class="sm:flex sm:items-start">
                            <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                                <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">Clinical Note</h3>
                                <div class="mt-2">
                                    <p class="text-sm text-gray-500 mb-4">
                                        Add a note for {{ selectedAppointmentPatientName }}. This will be saved to their history.
                                    </p>
                                    <textarea [(ngModel)]="noteSummary" rows="4" 
                                              class="shadow-sm focus:ring-teal-500 focus:border-teal-500 block w-full sm:text-sm border-gray-300 rounded-md p-2" 
                                              placeholder="Enter session summary..."></textarea>
                                    
                                     <div class="mt-4">
                                        <label class="block text-sm font-medium text-gray-700">Progress Score (0-100)</label>
                                        <input type="number" [(ngModel)]="noteProgress" min="0" max="100"
                                               class="mt-1 focus:ring-teal-500 focus:border-teal-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md p-2">
                                     </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                        <button type="button" (click)="saveNote()" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-teal-600 text-base font-medium text-white hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 sm:ml-3 sm:w-auto sm:text-sm">
                            Save Note
                        </button>
                        <button type="button" (click)="closeNoteModal()" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    }
  `
})
export class AppointmentListComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  private authService = inject(AuthService);

  appointments$!: Observable<Appointment[]>;
  isTherapist = false;

  // Modal State
  isNoteModalOpen = false;
  selectedAppointmentId: number | null = null;
  selectedAppointmentPatientName = '';
  noteSummary = '';
  noteProgress = 50;

  get userRole(): string {
    return this.authService.currentUserValue?.role || '';
  }

  ngOnInit() {
    this.isTherapist = this.userRole === 'ROLE_THERAPIST' || this.userRole === 'THERAPIST';
    this.refreshAppointments();
  }

  refreshAppointments() {
    this.appointments$ = this.appointmentService.getAppointments(this.userRole);
  }

  cancelAppointment(id: number) {
    if (confirm('Are you sure you want to cancel this appointment?')) {
      this.appointmentService.cancelAppointment(id).subscribe(() => this.refreshAppointments());
    }
  }

  completeAppointment(id: number) {
    this.appointmentService.completeAppointment(id).subscribe(() => this.refreshAppointments());
  }

  openNoteModal(appointment: Appointment) {
    this.selectedAppointmentId = appointment.id;
    this.selectedAppointmentPatientName = `${appointment.patient.firstName} ${appointment.patient.lastName}`;
    this.noteSummary = appointment.notes || ''; // Pre-fill if exists? Model might not have it mapped yet
    this.isNoteModalOpen = true;
  }

  closeNoteModal() {
    this.isNoteModalOpen = false;
    this.selectedAppointmentId = null;
    this.noteSummary = '';
    this.noteProgress = 50;
  }

  saveNote() {
    if (this.selectedAppointmentId) {
      this.appointmentService.addNote(this.selectedAppointmentId, this.noteSummary, this.noteProgress).subscribe({
        next: () => {
          this.closeNoteModal();
          this.refreshAppointments();
        },
        error: (err) => console.error('Error saving note', err)
      });
    }
  }
}
