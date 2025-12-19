import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// Create a new model for PatientInfoDTO if it doesn't match UserProfile perfectly, 
// using UserProfile for now as it seems close enough
import { UserProfile } from '../models/appointment.models';

@Injectable({
    providedIn: 'root'
})
export class PatientService {
    private http = inject(HttpClient);
    private apiUrl = 'http://localhost:8080/api/v1/patients';

    getPatients(): Observable<UserProfile[]> {
        // The API endpoint /api/v1/patients returns List<PatientInfoDTO>
        return this.http.get<UserProfile[]>(`${this.apiUrl}`);
    }
}
