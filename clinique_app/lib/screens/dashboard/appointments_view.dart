import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/providers/appointment_provider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

class AppointmentsView extends ConsumerWidget {
  final bool isPatientView;

  const AppointmentsView({super.key, required this.isPatientView});

  String _formatDate(String dateTimeString) {
    try {
      final dateTime = DateTime.parse(dateTimeString);
      return DateFormat('EEE, MMM d').format(dateTime);
    } catch (e) {
      return dateTimeString;
    }
  }

  String _formatTime(String dateTimeString) {
    try {
      final dateTime = DateTime.parse(dateTimeString);
      return DateFormat('h:mm a').format(dateTime);
    } catch (e) {
      return '';
    }
  }

  Color _getRiskColor(double? score) {
    if (score == null) return Colors.grey;
    if (score > 0.75) return AppColors.error;
    if (score > 0.4) return AppColors.warning;
    return AppColors.success;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final providerToWatch = isPatientView
        ? patientAppointmentsProvider
        : therapistAppointmentsProvider;

    final appointmentsAsync = ref.watch(providerToWatch);

    return appointmentsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, stack) => Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 48, color: AppColors.error),
              const SizedBox(height: 16),
              Text(
                'Failed to load appointments',
                style: AppTextStyles.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              Text(
                err.toString(),
                style: AppTextStyles.bodyMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: () => ref.invalidate(providerToWatch),
                icon: const Icon(Icons.refresh),
                label: const Text('Retry'),
              )
            ],
          ),
        ),
      ),
      data: (appointments) {
        if (appointments.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.calendar_today_outlined, size: 64, color: Colors.grey.shade300),
                const SizedBox(height: 16),
                Text(
                  'No appointments yet',
                  style: AppTextStyles.headlineMedium.copyWith(color: Colors.grey.shade500),
                ),
                const SizedBox(height: 24),
                OutlinedButton.icon(
                  onPressed: () => ref.invalidate(providerToWatch),
                  icon: const Icon(Icons.refresh),
                  label: const Text('Refresh'),
                ),
              ],
            ),
          );
        }

        return RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(providerToWatch);
          },
          child: ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: appointments.length,
            separatorBuilder: (ctx, i) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final appointment = appointments[index];
              final titleName = isPatientView
                  ? '${appointment.therapist.firstName} ${appointment.therapist.lastName}'
                  : '${appointment.patient.firstName} ${appointment.patient.lastName}';
              
              final riskScore = appointment.cancellationRiskScore;
              final isTherapist = !isPatientView;

              return Card(
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(color: Colors.grey.shade200),
                ),
                child: InkWell(
                  borderRadius: BorderRadius.circular(16),
                  onTap: () {
                    context.push('/appointment-detail', extra: appointment);
                  },
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Row(
                      children: [
                        // Date/Time Box
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                          decoration: BoxDecoration(
                            color: AppColors.primaryLight.withOpacity(0.3),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Column(
                            children: [
                              Text(
                                _formatDate(appointment.sessionDateTime).split(',')[0], // Day
                                style: AppTextStyles.labelLarge.copyWith(color: AppColors.primaryDark),
                              ),
                              Text(
                                _formatDate(appointment.sessionDateTime).split(',')[1].trim().split(' ')[1], // Date Number
                                style: AppTextStyles.headlineMedium.copyWith(
                                  color: AppColors.primary,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                _formatTime(appointment.sessionDateTime),
                                style: AppTextStyles.bodyMedium.copyWith(fontSize: 12),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 16),
                        
                        // Details
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                titleName,
                                style: AppTextStyles.headlineMedium.copyWith(fontSize: 18),
                              ),
                              const SizedBox(height: 4),
                              if (isPatientView)
                                Text('Therapist', style: AppTextStyles.bodyMedium)
                              else
                                Text('Patient', style: AppTextStyles.bodyMedium),
                            ],
                          ),
                        ),

                        // Risk Indicator (Therapist only)
                        if (isTherapist && riskScore != null)
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: _getRiskColor(riskScore).withOpacity(0.2),
                                width: 4,
                              ),
                            ),
                            child: Text(
                              '${(riskScore * 100).toInt()}%',
                              style: AppTextStyles.labelLarge.copyWith(
                                color: _getRiskColor(riskScore),
                                fontSize: 12,
                              ),
                            ),
                          )
                        else
                          const Icon(Icons.chevron_right, color: Colors.grey),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}
