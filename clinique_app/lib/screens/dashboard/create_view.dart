import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/core/widgets/custom_button.dart';
import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/providers/appointment_provider.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/providers/patient_provider.dart';
import 'package:clinique_app/services/appointment_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

class CreateView extends ConsumerStatefulWidget {
  const CreateView({super.key});

  @override
  ConsumerState<CreateView> createState() => _CreateViewState();
}

class _CreateViewState extends ConsumerState<CreateView> {
  final _formKey = GlobalKey<FormState>();
  PatientInfoDTO? _selectedPatient;
  DateTime? _selectedDateTime;
  bool _isLoading = false;

  Future<void> _pickDateTime() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: now.add(const Duration(days: 1)),
      firstDate: now,
      lastDate: now.add(const Duration(days: 365)),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: AppColors.primary,
              onPrimary: Colors.white,
              onSurface: AppColors.textPrimary,
            ),
          ),
          child: child!,
        );
      },
    );
    if (date == null) return;

    if (!mounted) return;

    final time = await showTimePicker(
      context: context,
      initialTime: const TimeOfDay(hour: 9, minute: 0),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: AppColors.primary,
              onPrimary: Colors.white,
              onSurface: AppColors.textPrimary,
            ),
          ),
          child: child!,
        );
      },
    );
    if (time == null) return;

    setState(() {
      _selectedDateTime = DateTime(
        date.year,
        date.month,
        date.day,
        time.hour,
        time.minute,
      );
    });
  }

  Future<void> _createAppointment() async {
    if (_formKey.currentState!.validate() &&
        _selectedDateTime != null &&
        _selectedPatient != null) {
      
      setState(() => _isLoading = true);

      try {
        final patientId = _selectedPatient!.id;
        final therapistId = ref.read(authProvider).user!.profileId;
        final appointmentService = ref.read(appointmentServiceProvider);

        await appointmentService.createAppointment(
          patientId: patientId,
          therapistId: therapistId,
          sessionDateTime: _selectedDateTime!,
        );

        if (!mounted) return;
        
        setState(() => _isLoading = false);
        ref.invalidate(therapistAppointmentsProvider);

        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Appointment created successfully!'),
            backgroundColor: AppColors.success,
            behavior: SnackBarBehavior.floating,
          ),
        );

        _formKey.currentState!.reset();
        setState(() {
          _selectedDateTime = null;
          _selectedPatient = null;
        });
      } catch (e) {
        setState(() => _isLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(e.toString()),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please select a patient and a date/time'),
          backgroundColor: AppColors.warning,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final patientListAsync = ref.watch(patientListProvider);

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'New Appointment',
                style: AppTextStyles.headlineMedium,
              ),
              const SizedBox(height: 8),
              Text(
                'Schedule a new session with a patient',
                style: AppTextStyles.bodyMedium,
              ),
              const SizedBox(height: 32),

              // Patient Selection
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Select Patient',
                    style: AppTextStyles.labelLarge.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 8),
                  patientListAsync.when(
                    loading: () => const LinearProgressIndicator(),
                    error: (err, stack) => Text('Error: $err', style: const TextStyle(color: AppColors.error)),
                    data: (patients) {
                      return DropdownButtonFormField<PatientInfoDTO>(
                        value: _selectedPatient,
                        decoration: InputDecoration(
                          prefixIcon: const Icon(Icons.person_search_outlined),
                          hintText: 'Choose a patient...',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        items: patients.map((patient) {
                          return DropdownMenuItem<PatientInfoDTO>(
                            value: patient,
                            child: Text(
                              '${patient.firstName} ${patient.lastName}',
                              style: AppTextStyles.bodyLarge,
                            ),
                          );
                        }).toList(),
                        onChanged: (value) => setState(() => _selectedPatient = value),
                        validator: (value) => value == null ? 'Required' : null,
                      );
                    },
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Date Selection
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Date & Time',
                    style: AppTextStyles.labelLarge.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 8),
                  InkWell(
                    onTap: _pickDateTime,
                    borderRadius: BorderRadius.circular(12),
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.grey.shade300),
                        borderRadius: BorderRadius.circular(12),
                        color: Colors.white,
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.calendar_today_outlined, color: AppColors.primary),
                          const SizedBox(width: 12),
                          Text(
                            _selectedDateTime == null
                                ? 'Select date and time'
                                : DateFormat('EEE, MMM d, yyyy  •  h:mm a').format(_selectedDateTime!),
                            style: _selectedDateTime == null
                                ? AppTextStyles.bodyMedium.copyWith(color: Colors.grey.shade500)
                                : AppTextStyles.bodyLarge,
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 40),

              CustomButton(
                text: 'Create Appointment',
                icon: Icons.check_circle_outline,
                isLoading: _isLoading,
                onPressed: _createAppointment,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
