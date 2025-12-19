import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Appointment, CreateAppointmentRequest } from '../models/appointment.models';
import { AuthService } from '../auth/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AppointmentService {
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private apiUrl = 'http://localhost:8080/api/v1/appointments';

    getAppointments(role: string): Observable<Appointment[]> {
        const currentUser = this.authService.currentUserValue;
        if (!currentUser) return of([]);

        if (role === 'ROLE_THERAPIST') {
            return this.http.get<Appointment[]>(`${this.apiUrl}/therapist/${currentUser.id}`);
        } else {
            return this.http.get<Appointment[]>(`${this.apiUrl}/patient/${currentUser.id}`);
        }
    }

    getAppointmentById(id: number): Observable<Appointment> {
        return this.http.get<Appointment>(`${this.apiUrl}/${id}`);
    }

    createAppointment(request: CreateAppointmentRequest): Observable<Appointment> {
        return this.http.post<Appointment>(this.apiUrl, request);
    }

    cancelAppointment(id: number): Observable<Appointment> {
        return this.http.put<Appointment>(`${this.apiUrl}/${id}/cancel`, {});
    }

    completeAppointment(id: number): Observable<Appointment> {
        return this.http.put<Appointment>(`${this.apiUrl}/${id}/complete`, {});
    }

    addNote(id: number, summary: string, patientProgressScore: number): Observable<Appointment> {
        return this.http.put<Appointment>(`${this.apiUrl}/${id}/note`, { summary, patientProgressScore });
    }
}
