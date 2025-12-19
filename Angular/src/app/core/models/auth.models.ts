export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
}

export interface LoginRequest {
    email: string;
    password: string; // The API expects 'password' or 'motDePasse'? Need to verify, assuming standard
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    role: 'PATIENT' | 'THERAPIST'; // from analysis
}

export interface AuthResponse {
    token: string;
    user: User;
}
