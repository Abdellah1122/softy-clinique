import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TherapistService, Therapist } from '../../../core/services/therapist.service';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-therapist-list',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-3xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-teal-400">
          Our Therapists
        </h2>
      </div>

      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        @for (therapist of therapists$ | async; track therapist.id) {
          <div class="group relative bg-white/80 backdrop-blur-md rounded-2xl shadow-sm border border-white/20 p-6 transition-all duration-300 hover:shadow-xl hover:-translate-y-1">
            <div class="flex items-center space-x-4">
              <div class="h-16 w-16 rounded-full bg-teal-100 flex items-center justify-center text-teal-600 text-xl font-bold border-2 border-teal-50">
                {{ therapist.firstName[0] }}{{ therapist.lastName[0] }}
              </div>
              <div>
                <h3 class="text-lg font-bold text-gray-900 group-hover:text-teal-600 transition-colors">
                  {{ therapist.firstName }} {{ therapist.lastName }}
                </h3>
                <p class="text-sm text-gray-500 font-medium">
                  {{ therapist.specialty }}
                </p>
              </div>
            </div>
            
            <div class="mt-4 pt-4 border-t border-gray-100 flex justify-end">
                 <button class="text-sm font-semibold text-teal-600 hover:text-teal-800 transition-colors">
                    View Profile &rarr;
                 </button>
            </div>
          </div>
        } @empty {
             <div class="col-span-full flex flex-col items-center justify-center py-16 bg-white/50 rounded-3xl border border-dashed border-gray-300">
                <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                    <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                </div>
                <h3 class="text-lg font-medium text-gray-900">No therapists found</h3>
            </div>
        }
      </div>
    </div>
  `
})
export class TherapistListComponent implements OnInit {
    private therapistService = inject(TherapistService);
    therapists$!: Observable<Therapist[]>;

    ngOnInit() {
        this.therapists$ = this.therapistService.getAllTherapists();
    }
}
