import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/core/widgets/custom_button.dart';
import 'package:clinique_app/core/widgets/custom_text_field.dart';
import 'package:clinique_app/models/profile_dto.dart';
import 'package:clinique_app/providers/profile_provider.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

class SettingsView extends ConsumerWidget {
  const SettingsView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(myProfileProvider);
    final updateState = ref.watch(profileUpdateProvider);

    ref.listen(profileUpdateProvider, (_, next) {
      if (next.hasError && !next.isLoading) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Update Failed: ${next.error.toString()}'),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
      if (!next.hasError && !next.isLoading && next.hasValue) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Profile updated successfully!'),
            backgroundColor: AppColors.success,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    });

    return Scaffold(
      body: profileAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(
          child: Text('Error loading profile', style: AppTextStyles.bodyLarge),
        ),
        data: (profile) {
          return ProfileForm(
            profile: profile,
            isSaving: updateState.isLoading,
            onSave: (Map<String, dynamic> data) {
              ref.read(profileUpdateProvider.notifier).updateProfile(data: data);
            },
          );
        },
      ),
    );
  }
}

class ProfileForm extends ConsumerStatefulWidget {
  final ProfileDTO profile;
  final bool isSaving;
  final Function(Map<String, dynamic> data) onSave;

  const ProfileForm({
    super.key,
    required this.profile,
    required this.isSaving,
    required this.onSave,
  });

  @override
  ConsumerState<ProfileForm> createState() => _ProfileFormState();
}

class _ProfileFormState extends ConsumerState<ProfileForm> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _firstNameController;
  late TextEditingController _lastNameController;
  late TextEditingController _phoneController;
  late TextEditingController _specialtyController;
  late TextEditingController _credentialsController;
  DateTime? _dateOfBirth;

  @override
  void initState() {
    super.initState();
    _firstNameController = TextEditingController(text: widget.profile.firstName);
    _lastNameController = TextEditingController(text: widget.profile.lastName);
    _phoneController = TextEditingController(text: widget.profile.phoneNumber);
    if (widget.profile.dateOfBirth != null) {
      _dateOfBirth = DateTime.tryParse(widget.profile.dateOfBirth!);
    }
    _specialtyController = TextEditingController(text: widget.profile.specialty);
    _credentialsController = TextEditingController(text: widget.profile.credentials);
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _phoneController.dispose();
    _specialtyController.dispose();
    _credentialsController.dispose();
    super.dispose();
  }

  void _submit() {
    if (_formKey.currentState!.validate()) {
      final Map<String, dynamic> data = {
        'firstName': _firstNameController.text,
        'lastName': _lastNameController.text,
      };

      if (widget.profile.isPatient) {
        data['phoneNumber'] = _phoneController.text;
        data['dateOfBirth'] = _dateOfBirth != null
            ? DateFormat('yyyy-MM-dd').format(_dateOfBirth!)
            : null;
      } else {
        data['specialty'] = _specialtyController.text;
        data['credentials'] = _credentialsController.text;
      }

      widget.onSave(data);
    }
  }

  Future<void> _selectDateOfBirth(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _dateOfBirth ?? DateTime(2000),
      firstDate: DateTime(1900),
      lastDate: DateTime.now(),
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
    if (picked != null && picked != _dateOfBirth) {
      setState(() {
        _dateOfBirth = picked;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final bool isPatient = widget.profile.isPatient;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header
            Text('Settings', style: AppTextStyles.headlineMedium),
            const SizedBox(height: 8),
            Text('Manage your profile information', style: AppTextStyles.bodyMedium),
            const SizedBox(height: 32),

            // Email Read-only
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.grey.shade100,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey.shade300),
              ),
              child: Row(
                children: [
                  const Icon(Icons.email_outlined, color: Colors.grey),
                  const SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Email Address', style: AppTextStyles.labelLarge.copyWith(fontSize: 12, color: Colors.grey)),
                      Text(widget.profile.email ?? '', style: AppTextStyles.bodyLarge),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            Row(
              children: [
                Expanded(
                  child: CustomTextField(
                    label: 'First Name',
                    controller: _firstNameController,
                    prefixIcon: const Icon(Icons.person_outline),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: CustomTextField(
                    label: 'Last Name',
                    controller: _lastNameController,
                    prefixIcon: const Icon(Icons.person_outline),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            if (isPatient) ...[
              CustomTextField(
                label: 'Phone Number',
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                prefixIcon: const Icon(Icons.phone_outlined),
              ),
              const SizedBox(height: 24),
              
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                   Text('Date of Birth', style: AppTextStyles.labelLarge.copyWith(color: AppColors.textSecondary)),
                   const SizedBox(height: 8),
                   InkWell(
                    onTap: () => _selectDateOfBirth(context),
                    borderRadius: BorderRadius.circular(12),
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.grey.shade300),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.calendar_month_outlined, color: Colors.grey),
                          const SizedBox(width: 12),
                          Text(
                             _dateOfBirth == null ? 'Select Date' : DateFormat('yyyy-MM-dd').format(_dateOfBirth!),
                             style: AppTextStyles.bodyLarge,
                          ),
                        ],
                      ),
                    ),
                   ),
                ],
              ),
              const SizedBox(height: 30),
            ] else ...[
              CustomTextField(
                label: 'Specialty',
                controller: _specialtyController,
                prefixIcon: const Icon(Icons.work_outline),
              ),
              const SizedBox(height: 24),
              CustomTextField(
                label: 'Credentials',
                controller: _credentialsController,
                prefixIcon: const Icon(Icons.school_outlined),
              ),
              const SizedBox(height: 30),
            ],

            CustomButton(
              text: 'Save Changes',
              isLoading: widget.isSaving,
              onPressed: _submit,
            ),

            const SizedBox(height: 48),
            const Divider(),
            const SizedBox(height: 16),
            
            OutlinedButton.icon(
              onPressed: () {
                ref.read(authProvider.notifier).logout();
              },
              icon: const Icon(Icons.logout, color: AppColors.error),
              label: const Text('Logout', style: TextStyle(color: AppColors.error)),
              style: OutlinedButton.styleFrom(
                side: const BorderSide(color: AppColors.error),
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }
}
