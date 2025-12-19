import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Therapist {
    id: number;
    firstName: string;
    lastName: string;
    specialty: string;
}

@Injectable({
    providedIn: 'root'
})
export class TherapistService {
    private http = inject(HttpClient);
    // Fallback to hardcoded URL if environment not set yet (temporary)
    private apiUrl = (environment.apiUrl || 'http://localhost:8080/api/v1') + '/therapists';

    getAllTherapists(): Observable<Therapist[]> {
        return this.http.get<Therapist[]>(this.apiUrl);
    }
}
