export interface UserProfile {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'ROLE_PATIENT' | 'ROLE_THERAPIST' | 'ROLE_ADMIN';
}

export interface Appointment {
    id: number;
    sessionDateTime: string; // ISO string
    status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
    notes?: string;
    cancellationRiskScore?: number;
    patient: UserProfile;
    therapist: UserProfile;
}

export interface CreateAppointmentRequest {
    patientId: number;
    therapistId: number;
    sessionDateTime: string;
    notes?: string;
}
