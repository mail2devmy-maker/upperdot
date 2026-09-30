# UpperDot — Technical Architecture Baseline

## 1. Project Metadata & Environment
* App Name: UpperDot
* Namespace: com.mail2dev.upperdot
* Compile SDK: 37 | Target SDK: 36 | Min SDK: 24 (Android 7.0)
* Build Setup: KSP Code Generation, Core Library Desugaring, Kotlin Serialization.
* Architecture: Offline-first MVVM / Repository Pattern with Google Drive Sync & Telecom Services.

## 2. Core Libraries & Frameworks
* UI: Jetpack Compose (BOM 2026.02.01), Material 3 (androidx.compose.material3), Extended Icons.
* Database & Storage: Room Database (androidx.room v2.8.4), Local & Drive Backup Engine (DatabaseBackup.kt).
* Navigation: Navigation Compose (androidx.navigation:navigation-compose:2.9.8).
* Media & Telecom: Coil Compose (2.7.0), Android Telecom Framework (InCallService, CallScreeningService), Audio Handler (AudioHandler.kt).
* Async & Sync: WorkManager (androidx.work:work-runtime-ktx:2.11.2), Google Drive API (v3-rev20260712-2.0.0), Google Play Services Auth (21.6.0).
* Serialization: kotlinx-serialization-json (1.11.0).
* Monetization: RevenueCat Purchases (com.revenuecat.purchases:10.15.1).

## 3. Package & Directory Map (`com.mail2dev.upperdot`)
* Root:
  * MainActivity.kt
  * UpperDotApp.kt
* data/local/:
  * AppDatabase.kt (Room Database instance)
  * DatabaseBackup.kt (Database backup & restore engine)
  * converter/: ComplexTypeConverters.kt, ListConverter.kt
  * dao/: BankCardDao.kt, ContactDao.kt, NoteDao.kt, PreferenceDao.kt, SavedBankDao.kt, TransactionDao.kt
  * entity/: BankCardEntity.kt, ContactEntity.kt, NoteEntity.kt, PreferenceEntity.kt, SavedBankEntity.kt, TransactionEntity.kt
  * model/: NoteWithContact.kt, TransactionWithContact.kt
* data/network/:
  * GoogleAuthService.kt, GoogleDriveService.kt
* data/repository/:
  * BankCardRepository.kt, BankSuggestionRepository.kt, BillingRepository.kt, ContactRepository.kt, HierarchyRepository.kt, NoteRepository.kt, PreferenceRepository.kt, TransactionRepository.kt
  * telephony/: CallLogRepository.kt
* data/sync/:
  * SyncManager.kt
* data/worker/:
  * DriveSyncWorker.kt
* telecom/:
  * InCallActivity.kt, InCallScreen.kt, UpperDotCallScreeningService.kt, UpperDotInCallService.kt
* ui/:
  * add_contact/: AddContactScreen.kt, AddContactViewModel.kt
  * app_settings/: AdvancedSettingsScreen.kt, AdvancedSettingsViewModel.kt
  * auth_launchpad/: AuthLaunchpadScreen.kt, AuthViewModel.kt
  * call_history/: CallHistoryScreen.kt, CallHistoryViewModel.kt
  * call_security/: CallWhitelistScreen.kt, CallWhitelistViewModel.kt
  * components/: DetailViewerSheets.kt, StitchComponents.kt, TelephonyKeypad.kt, UpperDotBottomNavigation.kt, VoiceMemoComponents.kt
  * connections_list/: ConnectionsListScreen.kt, ConnectionsListViewModel.kt
  * data_vault/: DataVaultManagementScreen.kt, DataVaultViewModel.kt
  * dialer/: DialerScreen.kt
  * digital_wallet/: DigitalWalletScreen.kt, DigitalWalletViewModel.kt
  * insights/: InsightsScreen.kt, InsightsViewModel.kt
  * new_cash_transaction/: TransactionSheet.kt
  * new_relationship_note/: RelationshipNoteSheet.kt
  * onboarding/: OnboardingScreen.kt
  * profile_detail/: ClientProfileDetailScreen.kt, ClientProfileDetailViewModel.kt
  * profile_settings/: MyProfileSettingsScreen.kt, ProfileSettingsViewModel.kt
  * relationship_hierarchy/: RelationshipHierarchyScreen.kt, RelationshipHierarchyViewModel.kt
  * theme/: Color.kt, StitchDesignSystem.kt, Theme.kt, Type.kt
  * wallet_overlay/: NewBankCardSheet.kt, QuickWalletOverlaySheet.kt
* util/ & utils/:
  * AudioHandler.kt, AudioHandlerImpl.kt, ContactUtils.kt, ImageCompressor.kt, MiuiPermissionUtils.kt, TelephonyUtils.kt
  * BackupUtils.kt, DateUtils.kt, StorageUtils.kt

## 4. Coding & Styling Conventions
* UI Theme: UpperDotTheme (StitchDesignSystem) located in ui/theme/. Palette tokens defined in Color.kt, typography in Type.kt.
* UI State: Collect state safely with collectAsStateWithLifecycle() in composables.
* Telecom Integration: InCallService and CallScreeningService manage native telephony features with foreground UI overlays.
* Database Rules: All Room migrations must preserve user offline data.
