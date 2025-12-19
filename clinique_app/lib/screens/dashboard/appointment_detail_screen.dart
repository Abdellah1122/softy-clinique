import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/core/widgets/custom_button.dart';
import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/providers/appointment_provider.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/providers/recommendation_provider.dart';
import 'package:clinique_app/services/appointment_service.dart';
import 'package:clinique_app/services/patient_service.dart'; // Added
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

class AppointmentDetailScreen extends ConsumerWidget {
  final AppointmentDTO appointment;

  const AppointmentDetailScreen({super.key, required this.appointment});

  String _formatDateTime(String dateTimeString) {
    try {
      final dateTime = DateTime.parse(dateTimeString);
      return DateFormat('EEEE, MMMM d, yyyy \n''h:mm a').format(dateTime);
    } catch (e) {
      return dateTimeString;
    }
  }

  Color _getRiskColor(double? score) {
    if (score == null) return Colors.grey;
    if (score > 0.75) return AppColors.error;
    if (score > 0.4) return AppColors.warning;
    return AppColors.success;
  }

  void _showRecommendationDialog(BuildContext context, WidgetRef ref) {
    ref.read(recommendationProvider.notifier).fetchRecommendation(appointment.patient.id);
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return Consumer(
          builder: (context, ref, child) {
            final state = ref.watch(recommendationProvider);
            return AlertDialog(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              title: Text(
                state.status == RecommendationStatus.loading
                    ? 'Analyzing...'
                    : state.status == RecommendationStatus.error
                        ? 'Error'
                        : 'Recommendation',
                style: AppTextStyles.headlineMedium,
              ),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (state.status == RecommendationStatus.loading) ...[
                    const CircularProgressIndicator(),
                    const SizedBox(height: 16),
                    const Text('Analyzing patient history...'),
                  ] else if (state.status == RecommendationStatus.error) ...[
                    Text(state.error ?? 'Failed to get recommendation.'),
                  ] else if (state.status == RecommendationStatus.success) ...[
                     Text(
                      'Recommended next session in:',
                      style: AppTextStyles.bodyMedium,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '${state.recommendation!.recommendedDaysNextSession} Days',
                      style: AppTextStyles.displayLarge.copyWith(color: AppColors.primary),
                    ),
                  ],
                ],
              ),
              actions: [
                if (state.status != RecommendationStatus.loading)
                  TextButton(
                    onPressed: () {
                      ref.read(recommendationProvider.notifier).reset();
                      Navigator.of(context).pop();
                    },
                    child: const Text('Close'),
                  ),
              ],
            );
          },
        );
      },
    );
  }

  void _showNoteEditSheet(BuildContext context, WidgetRef ref) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => EditNoteForm(appointment: appointment),
    );
  }

  void _updateStatus(BuildContext context, WidgetRef ref, String action) async {
    final service = ref.read(appointmentServiceProvider);
    try {
      await service.updateAppointmentStatus(
        appointmentId: appointment.id,
        action: action,
      );
      ref.invalidate(therapistAppointmentsProvider);
      ref.invalidate(patientAppointmentsProvider);
      
      if (context.mounted) context.pop();

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Appointment ${action}d successfully!'),
          backgroundColor: AppColors.success,
          behavior: SnackBarBehavior.floating,
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString()), 
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userRole = ref.watch(authProvider.select((state) => state.user?.role));
    final isTherapist = userRole == 'ROLE_THERAPIST';
    final isPatient = userRole == 'ROLE_PATIENT';
    final isScheduled = appointment.status == 'SCHEDULED';

    final patientName = '${appointment.patient.firstName} ${appointment.patient.lastName}';
    final therapistName = '${appointment.therapist.firstName} ${appointment.therapist.lastName}';
    final riskScore = appointment.cancellationRiskScore;
    final note = appointment.note;

    return Scaffold(
      appBar: AppBar(
        title: Text('Appointment Details', style: AppTextStyles.headlineMedium.copyWith(color: Colors.white)),
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20.0),
        children: [
          // Header Status Card
          Card(
            color: appointment.status == 'SCHEDULED' ? AppColors.primary : Colors.grey.shade400,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    appointment.status == 'SCHEDULED' ? Icons.calendar_today : Icons.check_circle_outline,
                    color: Colors.white,
                    size: 32,
                  ),
                  const SizedBox(width: 16),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        appointment.status,
                        style: AppTextStyles.headlineMedium.copyWith(color: Colors.white),
                      ),
                      if (isTherapist && riskScore != null)
                      Text(
                        'Risk Score: ${(riskScore * 100).toInt()}%',
                        style: AppTextStyles.bodyMedium.copyWith(color: Colors.white70),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),

          _buildSection(
            title: 'Date & Time',
            icon: Icons.access_time_filled,
            content: Text(
              _formatDateTime(appointment.sessionDateTime),
              style: AppTextStyles.bodyLarge,
            ),
          ),

          _buildSection(
            title: 'Participants',
            icon: Icons.people_alt,
            content: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildParticipantRow('Patient', patientName, Icons.person),
                const SizedBox(height: 8),
                _buildParticipantRow('Therapist', therapistName, Icons.medical_services),
              ],
            ),
          ),

          _buildSection(
            title: 'Clinical Note',
            icon: Icons.note_alt,
            content: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (note == null)
                  Text(
                    'No notes added yet.',
                    style: AppTextStyles.bodyMedium.copyWith(fontStyle: FontStyle.italic),
                  )
                else ...[
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                       Expanded(child: Text(note.summary ?? 'No summary', style: AppTextStyles.bodyLarge)),
                       if (note.sentimentLabel != null)
                         Container(
                           padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                           decoration: BoxDecoration(
                             color: note.sentimentLabel == 'POSITIVE' ? AppColors.primaryLight 
                                  : note.sentimentLabel == 'NEGATIVE' ? AppColors.error.withOpacity(0.1) 
                                  : Colors.grey.shade100,
                             borderRadius: BorderRadius.circular(20),
                           ),
                           child: Row(
                             mainAxisSize: MainAxisSize.min,
                             children: [
                               Icon(
                                 note.sentimentLabel == 'POSITIVE' ? Icons.sentiment_satisfied_alt
                                 : note.sentimentLabel == 'NEGATIVE' ? Icons.sentiment_dissatisfied
                                 : Icons.sentiment_neutral,
                                 size: 16,
                                 color: note.sentimentLabel == 'POSITIVE' ? AppColors.primaryDark 
                                      : note.sentimentLabel == 'NEGATIVE' ? AppColors.error 
                                      : Colors.grey.shade700,
                               ),
                               const SizedBox(width: 6),
                               Text(
                                 note.sentimentLabel!,
                                 style: AppTextStyles.labelLarge.copyWith(
                                    fontSize: 10,
                                    color: note.sentimentLabel == 'POSITIVE' ? AppColors.primaryDark 
                                         : note.sentimentLabel == 'NEGATIVE' ? AppColors.error 
                                         : Colors.grey.shade700,
                                 ),
                               ),
                             ],
                           ),
                         ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  if (note.patientProgressScore != null) ...[
                    Row(
                      children: [
                        Text('Progress Score:', style: AppTextStyles.labelLarge),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: AppColors.primaryLight,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            '${note.patientProgressScore} / 10',
                            style: AppTextStyles.labelLarge.copyWith(color: AppColors.primaryDark),
                          ),
                        ),
                      ],
                    ),
                  ]
                ],
              ],
            ),
          ),
          
          if (isTherapist)
             _ChurnRiskCard(patientId: appointment.patient.id),
          
          const SizedBox(height: 16),

          const SizedBox(height: 24),

          if (isScheduled && isPatient)
            CustomButton(
              text: 'Cancel Appointment',
              icon: Icons.cancel_outlined,
              onPressed: () => _updateStatus(context, ref, 'cancel'),
             // TODO: Update CustomButton to support color override or add secondary style
             // For now, primary style is fine, maybe add red color in next iteration
            ),

          if (isScheduled && isTherapist)
            CustomButton(
              text: 'Mark as Complete',
              icon: Icons.check_circle_outline,
              onPressed: () => _updateStatus(context, ref, 'complete'),
            ),

          if (isTherapist) ...[
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _showRecommendationDialog(context, ref),
                    icon: const Icon(Icons.lightbulb_outline),
                    label: const Text('AI Insight'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                       shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _showNoteEditSheet(context, ref),
                    icon: const Icon(Icons.edit_note),
                    label: Text(note == null ? 'Add Note' : 'Edit Note'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                ),
              ],
            ),
          ],
          const SizedBox(height: 40),
        ],
      ),
    );
  }

  Widget _buildSection({required String title, required IconData icon, required Widget content}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: AppColors.primary, size: 24),
              const SizedBox(width: 12),
              Text(title, style: AppTextStyles.headlineMedium.copyWith(fontSize: 18)),
            ],
          ),
          const SizedBox(height: 16),
          content,
        ],
      ),
    );
  }

  Widget _buildParticipantRow(String role, String name, IconData icon) {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: Colors.grey.shade100,
            shape: BoxShape.circle,
          ),
          child: Icon(icon, size: 16, color: Colors.grey.shade700),
        ),
        const SizedBox(width: 12),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(role, style: AppTextStyles.bodyMedium.copyWith(fontSize: 12)),
            Text(name, style: AppTextStyles.labelLarge),
          ],
        ),
      ],
    );
  }
}

class EditNoteForm extends ConsumerStatefulWidget {
  final AppointmentDTO appointment;
  const EditNoteForm({super.key, required this.appointment});

  @override
  ConsumerState<EditNoteForm> createState() => _EditNoteFormState();
}

class _EditNoteFormState extends ConsumerState<EditNoteForm> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _summaryController;
  late TextEditingController _privateNotesController;
  late double _progressScore;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    final note = widget.appointment.note;
    _summaryController = TextEditingController(text: note?.summary ?? '');
    _privateNotesController = TextEditingController(text: '');
    _progressScore = note?.patientProgressScore?.toDouble() ?? 5.0;
  }

  Future<void> _submitNote() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final service = ref.read(appointmentServiceProvider);
      await service.addOrUpdateNote(
        appointmentId: widget.appointment.id,
        summary: _summaryController.text,
        privateNotes: _privateNotesController.text,
        patientProgressScore: _progressScore.toInt(),
      );

      ref.invalidate(therapistAppointmentsProvider);
      ref.invalidate(patientAppointmentsProvider);

      if (mounted) Navigator.of(context).pop();

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Note saved!'), backgroundColor: AppColors.success),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString()), backgroundColor: AppColors.error),
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
        left: 24,
        right: 24,
        top: 24,
      ),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Clinical Note', style: AppTextStyles.headlineMedium),
              const SizedBox(height: 24),

              TextFormField(
                controller: _summaryController,
                decoration: const InputDecoration(
                  labelText: 'Summary (Visible to Patient)',
                  alignLabelWithHint: true,
                ),
                maxLines: 4,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),

              TextFormField(
                controller: _privateNotesController,
                decoration: const InputDecoration(
                  labelText: 'Private Notes',
                  alignLabelWithHint: true,
                ),
                maxLines: 3,
              ),
              const SizedBox(height: 24),

              Text('Patient Progress: ${_progressScore.toInt()}/10', style: AppTextStyles.labelLarge),
              Slider(
                value: _progressScore,
                min: 1,
                max: 10,
                divisions: 9,
                label: _progressScore.toInt().toString(),
                activeColor: AppColors.primary,
                onChanged: (val) => setState(() => _progressScore = val),
              ),
              const SizedBox(height: 24),

              CustomButton(
                text: 'Save Note',
                isLoading: _isLoading,
                onPressed: _submitNote,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ChurnRiskCard extends ConsumerStatefulWidget {
  final int patientId;
  const _ChurnRiskCard({required this.patientId});

  @override
  ConsumerState<_ChurnRiskCard> createState() => _ChurnRiskCardState();
}

class _ChurnRiskCardState extends ConsumerState<_ChurnRiskCard> {
  bool _loading = true;
  double? _churnProb;
  bool? _isHighRisk;

  @override
  void initState() {
    super.initState();
    _fetchChurnRisk();
  }

  void _fetchChurnRisk() async {
    try {
      final service = ref.read(patientServiceProvider);
      final churnRisk = await service.getChurnRisk(widget.patientId);
      
      if (mounted) {
        setState(() {
          _loading = false;
          _churnProb = churnRisk?.churnProbability;
          _isHighRisk = churnRisk?.isChurnRisk;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
      debugPrint('Error fetching churn risk: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const SizedBox(
        height: 80,
        child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
      );
    }
    
    if (_churnProb == null) return const SizedBox.shrink();

    final isRisk = _isHighRisk ?? false;
    final prob = (_churnProb! * 100).toInt();

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.analytics_outlined, color: AppColors.primary, size: 24),
              const SizedBox(width: 12),
              Text('Churn Prediction', style: AppTextStyles.headlineMedium.copyWith(fontSize: 18)),
            ],
          ),
          const SizedBox(height: 16),
          Row(
             children: [
               Expanded(
                 child: Column(
                   crossAxisAlignment: CrossAxisAlignment.start,
                   children: [
                     Text('Churn Probability', style: AppTextStyles.bodyMedium),
                     const SizedBox(height: 4),
                     Text('$prob%', style: AppTextStyles.displayLarge.copyWith(fontSize: 24)),
                   ],
                 ),
               ),
               Container(
                 padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                 decoration: BoxDecoration(
                   color: isRisk ? AppColors.error.withOpacity(0.1) : AppColors.success.withOpacity(0.1),
                   borderRadius: BorderRadius.circular(20),
                 ),
                 child: Row(
                   children: [
                     Icon(
                       isRisk ? Icons.warning_amber_rounded : Icons.check_circle_outline,
                       color: isRisk ? AppColors.error : AppColors.success,
                       size: 20,
                     ),
                     const SizedBox(width: 8),
                     Text(
                       isRisk ? 'High Risk' : 'Low Risk',
                       style: AppTextStyles.labelLarge.copyWith(
                         color: isRisk ? AppColors.error : AppColors.success,
                       ),
                     ),
                   ],
                 ),
               ),
             ],
          ),
           if (isRisk) ...[
            const SizedBox(height: 12),
            Text(
              'Patient shows signs of potential disengagement. Consider reaching out or reviewing therapy plan.',
               style: AppTextStyles.bodyMedium.copyWith(color: Colors.grey.shade700, fontSize: 13),
            ),
           ],
        ],
      ),
    );
  }
}

