# UpperDot Project Architecture & Decision Log

This file tracks all technical conflicts, layout choices, and architectural decisions made across every screen development milestone.

---

## 📋 Log Entry Template (For Agent Reference)

### [YYYY-MM-DD] - [Screen Name]
- **Context/Goal:** [Briefly describe the target screen requirements]
- **Conflicts & Alternatives Considered:**
  - *Option A:* [Describe approach A with pros/cons matching Stitch tokens]
  - *Option B:* [Describe approach B with pros/cons matching local data guidelines]
- **Final Decision:** [State exactly which pattern was chosen and why]
- **Impact:** [List files or components changed]

---

## 📑 Historical Logs

### 2024-05-20 - Screen 01: Authentication Launchpad
- **Context/Goal:** Centrally aligned branding with Google Sign-In and "Try as Guest" mode. Pure Dark Mode (#121212) with Primary Yellow (#FFD54F) text.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Navigation vs Splash:* Should this be a splash screen that auto-transitions or a static launchpad? *Decision:* Launchpad as it contains interactive login elements.
  - *Conflict 2: Guest Mode State:* Should Guest Mode be a simple bypass or a separate state? *Decision:* Separate state that behaves as Premium but clears all data on session end, using a warning dialog to prevent data loss expectations.
- **Final Decision:** Implement `AuthLaunchpadScreen` utilizing `UpperDotTheme`. Google Sign-In follows branding guidelines; "Try as Guest" is styled with `PrimaryYellow`.
- **Impact:** `AuthLaunchpadScreen.kt`, `AuthViewModel.kt`, `MainActivity.kt` nav graph update.

### 2024-05-20 - Screen 02: Call History Screen
- **Context/Goal:** Implementation of call history feed with permission-gated empty state and persistent bottom navigation.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Permission Management:* Should permission handling be in the ViewModel or UI? *Decision:* ViewModel tracks the permission state boolean, UI triggers the request and displays the empty state card based on the boolean.
  - *Conflict 2: Bottom Navigation Implementation:* Using standard NavigationBar vs custom Stitch-rounded dock. *Decision:* Custom BottomAppBar with pill-shaped selection indicator matching Screen 02 visual specs.
- **Final Decision:** Implement `CallHistoryScreen` with a Scaffold to host the BottomAppBar. Permission state determines whether to show the EmptyCallHistoryCard or the CallLogList.
- **Impact:** `CallHistoryScreen.kt`, `CallHistoryViewModel.kt`, `MainActivity.kt` nav graph update.

### 2024-05-20 - Screen 03: Connections List
- **Context/Goal:** Primary dashboard for managing contacts with search, filtering, and quick actions.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Search Implementation:* Real-time Room filtering vs button-triggered search. *Decision:* Real-time filtering in ViewModel using a StateFlow and debounce for performance.
  - *Conflict 2: Card Interaction:* Handling swipe-to-dial and long-press expansion. *Decision:* Custom interaction for the dial action and an AnimatedVisibility wrapper for the long-press expansion.
- **Final Decision:** Implement `ConnectionsListScreen` using a Scaffold. Search and filters are anchored at the top below the header. The contact list uses LazyColumn with heavy rounding tokens (24dp) for cards.
- **Impact:** `ConnectionsListScreen.kt`, `ConnectionsListViewModel.kt`, `MainActivity.kt` nav graph update.

### 2024-05-20 - Screen 04: Add Contact Form Wizard - Step 1: Core Info
- **Context/Goal:** First step of the multi-tab contact creation wizard focusing on basic identification.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Tab Navigation vs Sequential Buttons:* Should user use "Next" buttons or tabs? *Decision:* Both. Tabs (ScrollableTabRow) for direct jumping and a persistent Save/Next FAB as per Stitch design guidelines to ensure flexibility.
  - *Conflict 2: State Retention:* How to persist data across wizard steps? *Decision:* Single `AddContactViewModel` scoped to the navigation graph to retain state as user navigates between steps 1-4.
  - *Conflict 3: Validation Trigger:* When to validate "Required" fields? *Decision:* On-the-fly visual hints (asterisks) and hard blocking on Step 4 "Save" click, with auto-routing back to Step 1 if Full Name is empty.
- **Final Decision:** Implement `AddContactCoreInfoScreen`. Custom avatar picker UI with cyan ring. Use OutlinedTextField styled with Stitch tokens (#1E1E1E background, 16dp rounding).
- **Impact:** `AddContactCoreInfoScreen.kt`, `AddContactViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 05: Add Contact Form Wizard - Step 2: Identity
- **Context/Goal:** Second step of the wizard focusing on digital identifiers and relationship grouping.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Social Profile State:* How to manage a dynamic list of social platforms? *Decision:* Use a data class `SocialProfile` with platform and handle fields. ViewModel manages a `List<SocialProfile>` to allow multiple entries.
  - *Conflict 2: Dropdown UI:* Standard Material dropdown vs custom Stitch capsule. *Decision:* Custom `ExposedDropdownMenuBox` styled to match our `StitchTextField` rounded surface tokens for visual consistency.
  - *Conflict 3: Field Grouping:* How to separate Email, Social, and Tags? *Decision:* Use clear Section Headers in AccentCyan with vertical spacing, maintaining the "Stitch" vertical form flow.
- **Final Decision:** Implement `AddContactIdentityScreen`. Maintain shared VM state. Utilize `PrimaryYellow` for the "X" Close and `AccentCyan` for active tab/headers.
- **Impact:** `AddContactIdentityScreen.kt`, `AddContactViewModel.kt` (updated with setters), `MainActivity.kt` routes.

### 2024-05-20 - Profile Detail Historical Timelines & Viewer Overlays
- **Context/Goal:** Activate real-time historical timelines (Notes & Transactions) within the Profile Detail View with high-density summary rows and read-only viewer overlays.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Data Integration:* Hardcoded counts vs reactive streams. *Decision:* Injected `NoteRepository` and `TransactionRepository` into `ClientProfileDetailViewModel`. Used `flatMapLatest` on the active `contactId` to stream live results from Room, ensuring counts in accordion headers are always accurate.
  - *Conflict 2: Summary Row Density:* How much info to show in the list? *Decision:* Implemented "High-Density" rows. Notes show Title + Date with tiny icons for Voice/Attachments. Transactions show Title + Date with color-coded signed amounts (+Green/-Red) for immediate financial context.
  - *Conflict 3: Viewer Sheet Implementation:* Separate screen vs Bottom Sheet. *Decision:* Used `ModalBottomSheet` (viewer overlays) to maintain context. Sheets are "read-only" versions of the creation forms, including full content areas, voice playback placeholders, and attachment previews.
- **Final Decision:** Implement reactive wiring in ViewModel and `LazyColumn` row components in Screen. Used `ModalBottomSheet` with `skipPartiallyExpanded = true` for viewer overlays.
- **Impact:** `ClientProfileDetailViewModel.kt`, `ClientProfileDetailScreen.kt`, `MainActivity.kt` update, `DateUtils.kt` created.

### 2024-05-20 - Global Insights Interaction & Media Stabilization
- **Context/Goal:** Fix frozen clicks in Insights tab and resolve missing attachment paths in contact card shortcuts.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Interaction Wiring:* Tapping cards in Insights was unresponsive. *Decision:* Wired `onClick` modifiers in `NoteCard` and `TransactionCard` to the ViewModel's `selectNote/Transaction` methods. Implemented shared `NoteViewerSheet` and `TransactionViewerSheet` components to ensure a unified detail viewing experience across the app.
  - *Conflict 2: Media Persistence:* Shortcut-created notes were losing attachment paths. *Decision:* Identified that managing media state via local `remember` in the UI layer was fragile. Moved `selectedAttachments` pipeline directly into `ConnectionsListViewModel` and `InsightsViewModel`. Updated the saving logic to gather paths directly from the ViewModel's StateFlow, ensuring Room writes are complete before state is cleared.
- **Final Decision:** Migrated all temporary media states to ViewModels and unified detail viewer overlays into a shared component library.
- **Impact:** `InsightsScreen.kt`, `InsightsViewModel.kt`, `ConnectionsListScreen.kt`, `ConnectionsListViewModel.kt`, `DetailViewerSheets.kt` created.

### 2024-05-20 - Internal Storage Persistence & Media Refactor
- **Context/Goal:** Fix blank attachment thumbnails caused by Android file storage access restrictions by migrating to a local-first file persistence strategy.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Storage Strategy:* Raw URIs vs File Copying. *Decision:* Implemented a file stream copying mechanism using `StorageUtils`. When a user selects an image, it is immediately cloned into the app's internal private storage (`context.filesDir/attachments`). This ensures permanent availability regardless of external gallery changes or permission revocations.
  - *Conflict 2: Path Persistence:* Storing content URIs vs Absolute Paths. *Decision:* Switched the database column to store absolute internal file paths. This simplifies loading logic and improves performance as the app doesn't need to resolve URIs or request persistent permissions repeatedly.
  - *Conflict 3: Viewer UX:* Static thumbnails vs Full-Screen Preview. *Decision:* Enhanced `NoteViewerSheet` and `TransactionViewerSheet` with `FullScreenImagePreview` dialogs. Tapping a thumbnail now opens a high-fidelity fit-to-screen view with a dedicated close action.
- **Final Decision:** Use internal storage for all rich media and absolute path strings in Room.
- **Impact:** `StorageUtils.kt` created, `DetailViewerSheets.kt` updated, `NewRelationshipNoteSheet.kt` and `NewCashTransactionSheet.kt` refactored to use the new copy logic.

### 2024-05-20 - Contextual Picker Locking Mechanism
- **Context/Goal:** Prevent accidental contact changes when creating notes or transactions via contact card shortcuts by locking the contact selection UI.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Interaction Control:* How to disable the picker without creating separate sheet components? *Decision:* Implemented an `isContactLocked` boolean flag in `NewRelationshipNoteSheet` and `NewCashTransactionSheet`. When true, the picker `Box` disables its `clickable` modifier, the dropdown chevron is hidden, and the internal search/filter overlay is entirely bypassed.
  - *Conflict 2: State Awareness:* When should the picker be locked? *Decision:* The picker is locked in `ConnectionsListScreen` whenever `preSelectedContact` is non-null (shortcut method). It remains unlocked (default) in the global `InsightsScreen` to allow the user to search and associate any contact.
- **Final Decision:** Use conditional UI rendering and modifier application based on the `isContactLocked` flag to enforce context-specific input restrictions.
- **Impact:** `NewRelationshipNoteSheet.kt`, `NewCashTransactionSheet.kt`, `ConnectionsListScreen.kt`, `InsightsScreen.kt`.

### 2024-05-20 - Insights Data Mapping & SQL Join Correction
- **Context/Goal:** Fix a critical data mapping failure where the contact name on Insights note/transaction cards was hardcoded to "test" instead of dynamically reflecting the linked contact.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Relational Resolution:* Should contact names be resolved in the ViewModel or DAO? *Decision:* Implemented `INNER JOIN` logic directly in `NoteDao` and `TransactionDao`. This is more efficient for large datasets as it leverages SQLite's relational engine and returns a unified `NoteWithContact`/`TransactionWithContact` POJO, reducing the need for iterative lookups in the ViewModel.
  - *Conflict 2: Filter Synchronization:* The existing Insights filter was ignoring the `contactName` parameter. *Decision:* Updated the `flatMapLatest` pipeline in `InsightsViewModel` to strictly respect the `_selectedContactFilter` StateFlow, applying an additional `map { list -> list.filter { ... } }` layer to the database stream.
- **Final Decision:** Use SQLite Inner Joins to dynamically resolve contact names from the `contacts` table based on `contactId`.
- **Impact:** `NoteDao.kt`, `TransactionDao.kt`, `NoteRepository.kt`, `TransactionRepository.kt`, `InsightsViewModel.kt` updated. `NoteWithContact.kt` and `TransactionWithContact.kt` models created.

### 2024-05-20 - Contact Form Wizard Edit Mode Update Fix
- **Context/Goal:** Fix the freeze occurring when attempting to save an edited contact. Ensure updated values are correctly mapped to the existing ID and persisted to Room.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Navigation Routing:* Should edit mode be a separate route? *Decision:* Implemented an optional `contactId` query parameter for the `add_contact` route: `add_contact?contactId={contactId}`. This allows reusing the same Wizard UI while providing a hook to load existing data.
  - *Conflict 2: State Initialization:* When to load contact data? *Decision:* Used a `LaunchedEffect(contactId)` block in `MainActivity.kt` to trigger `addContactViewModel.loadContact(it)` upon route entry. This ensures the form is pre-populated before the user interacts with it.
  - *Conflict 3: Persistence Logic:* Insert vs Update. *Decision:* Updated `saveContact` in `AddContactViewModel` to check for a non-null `editingContactId`. If present, it calls `repository.updateContact(entity)`; otherwise, it performs a standard `insertContact(entity)`.
- **Final Decision:** Use an internal `editingContactId` reference in the ViewModel to drive the branching logic between creation and updates, ensuring ID continuity for relational data (notes/transactions).
- **Impact:** `AddContactViewModel.kt`, `MainActivity.kt` updated.

### 2024-05-20 - Form Selection Gating Refactor
- **Context/Goal:** Refine group and tag selection logic in `AddContactIdentityScreen.kt` to streamline user flow and enforce valid data entry.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Group Creation Access:* Should users create groups inline? *Decision:* Removed the "[ + Create New Group ]" option from the wizard. Users must now manage groups via the dedicated Relationship Hierarchy Manager, reducing wizard clutter and ensuring intentional hierarchy design.
  - *Conflict 2: Dynamic Field State:* How to handle groups without tags? *Decision:* Implemented "Smart Disabling" for the Tag dropdown. If a selected group has no child tags, the dropdown is disabled (`enabled = false`) and its label is updated to "No tags available", providing clear visual feedback instead of an empty list.
- **Final Decision:** Restricted group selection to existing entries and automated tag field states based on hierarchy depth.
- **Impact:** `AddContactIdentityScreen.kt` updated.

### 2024-05-20 - Capsule Stream Filtering Fix
- **Context/Goal:** Fix a bug where clicking filter capsules (Family, Work, etc.) changed the UI color but failed to filter the contacts in `ConnectionsListScreen`.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Filtering Level:* ViewModel vs Database. *Decision:* Implemented filtering in the ViewModel's `uiState` reactive pipeline. By combining `searchQuery` and `selectedFilter` using `flatMapLatest`, the UI now dynamically reacts to both text input and capsule selections without requiring complex nested DAO queries.
- **Final Decision:** Use Kotlin Coroutines `combine` and `flatMapLatest` to apply the `groupName` predicate to the incoming repository stream.
- **Impact:** `ConnectionsListViewModel.kt` updated.

### ⚠️ Build Errors & Resolutions
- **Error:** `Unresolved reference: toFormattedDate` in `ClientProfileDetailScreen.kt`.
- **Cause:** Missing utility function for formatting `Long` timestamps into user-friendly date strings.
- **Resolution:** Created `com.mail2dev.upperdot.utils.DateUtils.kt` with a `Long.toFormattedDate()` extension function using `SimpleDateFormat`.

- **Error:** Syntax errors in `DetailViewerSheets.kt` due to escaped format strings.
- **Cause:** Incorrect string literal escaping during file write operation (`\"%.2f\"` instead of `"%.2f"`).
- **Resolution:** Refactored `DetailViewerSheets.kt` using `replace_file_content` to ensure correct Kotlin string formatting for currency values.

- **Error:** `Conflicting overloads: fun WizardTabRow(...)` and `Unresolved reference: it` in `AddContactIdentityScreen.kt`.
- **Cause:** Duplicated `WizardTabRow` and `StitchTextField` across both `AddContactCoreInfoScreen.kt` and the new `AddContactComponents.kt` file. Additionally, `StitchTextField` was missing a `modifier` parameter in its shared definition, causing a signature mismatch in the Identity screen.
- **Resolution:**
  1. Removed local component definitions from `AddContactCoreInfoScreen.kt` and `AddContactIdentityScreen.kt`.
  2. Consolidated all shared wizard components into `AddContactComponents.kt`.
  3. Updated `StitchTextField` signature to include `modifier: Modifier = Modifier` and `leadingIcon: ImageVector? = null`.
  4. Fixed lambda parameter naming in `AddContactIdentityScreen.kt` for `onPlatformSelected`.

- **Error:** `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` and `Duplicate class ListenableFuture` and `INDEX.LIST` merge conflict.
- **Cause:** KSP integration conflict with standard Kotlin DSL, `compileSdk` mismatch for lifecycle 2.11, and redundant dependencies in Google Drive libraries.
- **Resolution:**
  1. Set `android.disallowKotlinSourceSets=false` in `gradle.properties`.
  2. Updated `compileSdk` to 37 in `app/build.gradle.kts`.
  3. Added `guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava` to resolve library duplication.
  4. Added `packaging` block to exclude `INDEX.LIST` and `DEPENDENCIES` files from the final APK.

- **Error:** `java.lang.ClassCastException: android.app.Application cannot be cast to com.mail2dev.upperdot.UpperDotApp` on startup.
- **Cause:** Custom `Application` class (`UpperDotApp.kt`) was implemented but not registered in the system manifest.
- **Resolution:** Manually added `android:name=".UpperDotApp"` to the `<application>` tag in `AndroidManifest.xml` to correctly initialize the project's repository singletons.

- **Error:** `Select Contact` dropdown in `NewRelationshipNoteSheet` loading hardcoded dummy names instead of live repository contacts.
- **Cause:** `InsightsViewModel` was not connected to `ContactRepository`, and `InsightsScreen.kt` was passing a hardcoded list of strings to the bottom sheets.
- **Resolution:** 
  1. Injected `ContactRepository` into `InsightsViewModel`.
  2. Exposed a `contactNames` StateFlow in `InsightsViewModel` that maps the full names of all saved contacts.
  3. Updated `MainActivity.kt` to provide `ContactRepository` during `InsightsViewModel` initialization.
  4. Updated `InsightsScreen.kt` to collect `contactNames` and pass it to both `NewRelationshipNoteSheet` and `NewCashTransactionSheet`, ensuring live data binding for the contact selector.

- **Error:** `RelationshipHierarchyScreen` interactions (plus, pencil, trash) were non-functional and expansion was visually inconsistent. Additionally, the group contact count badges were hardcoded and incorrect.
- **Cause:** `RelationshipHierarchyViewModel` lacked implementation for state mutations, and the UI layer lacked dialogs. The contact counts were static values in a hardcoded list.
- **Resolution:**
  1. Refactored `RelationshipHierarchyViewModel` to use reactive state mutations for all hierarchy operations.
  2. Implemented `AlertDialog` components in `RelationshipHierarchyScreen` for "Add Tag", "Rename Group", and "Rename Tag" flows.
  3. Injected `ContactRepository` into `RelationshipHierarchyViewModel` and implemented a `combine` operator to dynamically calculate the `contactCount` for each group based on the live database.
  4. Updated `MainActivity.kt` and `RelationshipHierarchyScreen.kt` to support the new repository injection.
  5. Wired all action buttons to their respective ViewModel functions, ensuring the UI reacts instantly to changes.

- **Error:** Generic text field for "RELATIONSHIP GROUP" in `AddContactIdentityScreen` made it difficult for users to pick from the established hierarchy.
- **Cause:** Step 2 of the wizard was disconnected from the hierarchy data managed in the `manage_custom_groups` section.
- **Resolution:**
  1. Created a shared `HierarchyRepository` to centralize group and tag management.
  2. Updated `UpperDotApp`, `RelationshipHierarchyViewModel`, and `AddContactViewModel` to utilize the shared repository.
  3. Refactored `AddContactIdentityScreen` to use two dependent `StitchDropdown` components:
     - Dropdown 1 (Group): Loads live groups with an inline "[ + Create New Group ]" action.
     - Dropdown 2 (Tag): Dynamically populates with tags based on the selected group and remains disabled if no group is chosen.
  4. Updated `StitchDropdown` to support an `enabled` state and styled it to match the Pure Dark Mode theme.
  5. Mapped the selected group and tag values to the `ContactEntity` for final persistence.

- **Error:** Frozen UI features (Date Picker, Attachments, Voice Recorder) in `NewRelationshipNoteSheet`.
- **Cause:** These features were implemented as UI stubs without any functional backend or native system integration.
- **Resolution:**
  1. Integrated Material 3 `DatePickerDialog` to allow users to select custom note timestamps.
  2. Implemented `rememberLauncherForActivityResult` with `GetContent()` to enable local photo selection for attachments.
  3. Integrated native `MediaRecorder` for voice memo capture, saving audio files to the app's internal cache directory.
  4. Updated `InsightsViewModel` and `NewRelationshipNoteSheet` state pipeline to handle and persist attachment paths and voice recording locations.

- **Error:** Attachment section in `NewRelationshipNoteSheet` only displayed a count string without visual previews.
- **Cause:** Lack of a dynamic list/grid component to render selected image URI paths as thumbnails.
- **Resolution:**
  1. Integrated **Coil library** (`coil-compose`) for asynchronous image loading.
  2. Implemented a horizontal `LazyRow` Visual Thumbnail Preview Grid.
  3. Rendered selected images as 64dp x 64dp squares with 12dp rounded corners using `AsyncImage`.
  4. Added a circular delete button overlay on each thumbnail to allow users to remove specific attachments.
  5. Moved attachment selection state to `InsightsViewModel` to support robust deletion logic and state persistence across recompositions.
  6. Maintained the "+" add box as a persistent element at the end of the preview row.

- **Error:** Static contact picker in `NewRelationshipNoteSheet` and `NewCashTransactionSheet` was inefficient for large contact lists.
- **Cause:** Lack of search/filter functionality in the contact selection UI.
- **Resolution:**
  1. Implemented real-time contact search in `InsightsViewModel.kt` using `flatMapLatest` and `debounce(300)` on a new `contactSearchQuery` state.
  2. Replaced the static `StitchDropdown` in both `NewRelationshipNoteSheet` and `NewCashTransactionSheet` with a searchable contact component.
  3. Wired the search component to display matching `fullName` entries from the database as the user types.
  4. Updated the state binding to ensure selecting a contact correctly links their `id` and `fullName` to the active note or transaction.
  5. Refactored the `save` logic to use the selected contact's ID for database persistence.

- **Feature:** Long-press contact card for quick actions (Add Note/Transaction).
- **Context/Goal:** Allow users to quickly add context to a connection without navigating to the profile detail.
- **Decision:**
  1. Updated `ConnectionsListScreen.kt` to handle `onLongPress` and toggle an expanded card state.
  2. Implemented `QuickActionButton` row (Add Note, Add Trans) visible only when expanded.
  3. Integrated `NewRelationshipNoteSheet` and `NewCashTransactionSheet` directly into `ConnectionsListScreen`.
  4. Updated `ConnectionsListViewModel.kt` to manage sheet visibility and pre-selected contact state.
  5. Unified `ContactSummary` and ID types (`Long`) across ViewModels and Screens to ensure consistent data binding.
- **Impact:** `ConnectionsListScreen.kt`, `ConnectionsListViewModel.kt`, `MainActivity.kt`.

- **Error:** Searching for contacts in `NewRelationshipNoteSheet` and `NewCashTransactionSheet` allowed submitting non-existent names, causing orphan data.
- **Cause:** The contact search field was interactive and didn't strictly enforce selection from the database results.
- **Resolution:**
  1. Overhauled the contact selection UI to be completely **read-only** at the top level.
  2. Implemented a toggleable **Search Picker Overlay** that appears when the read-only field is tapped.
  3. Placed the `StitchTextField` search input *inside* the overlay, isolating it from the primary form state.
  4. Enforced strict selection: Users can only associate a contact by explicitly clicking an item from the verified search results list.
  5. Updated validation logic: The "Save" and "Finalize" buttons remain strictly disabled if no contact is selected, even if there is text in the internal search field.
  6. Added visual feedback with `ArrowDropUp`/`ArrowDropDown` icons to indicate the picker's state.

- **UI Polish:** Sleek Compact Search and Defined Form Boundaries.
- **Context/Goal:** Refine the layout based on device testing to improve visual clarity and space efficiency.
- **Decision:**
  1. Implemented `CompactSearchField` for search overlays, featuring a 48dp height, 24dp corner rounding, and thinner text sizes.
  2. Updated `StitchTextField` to globally include a clear border using `MaterialTheme.colorScheme.outlineVariant`.
  3. Ensured all primary input fields (Title, Content, etc.) are contained within defined `#1E1E1E` surface boundaries with 16dp rounding.
- **Impact:** `StitchComponents.kt`, `NewRelationshipNoteSheet.kt`, `NewCashTransactionSheet.kt`.

- **Feature:** Enabled VCF Contact Import with Duplicate Detection.
- **Context/Goal:** Allow users to bulk-load contacts from .vcf files with a "Smart Number Matcher" and conflict resolution.
- **Decision:**
  1. Implemented a basic VCF parser in `AdvancedSettingsViewModel.kt` to extract Name, Phone, and Email.
  2. Created `ContactUtils.kt` with a `smartSanitize` logic to compare phone numbers by stripping symbols and country codes (Smart Number Matcher).
  3. Implemented a conflict resolution flow: if matching numbers are found, a dialog appears presenting 3 options: "Overwrite Existing", "Keep Both (Duplicate)", and "Skip Conflicts".
  4. Fixed a type mismatch error: Updated `NoteEntity` and `TransactionEntity` to use `Long` for `contactId` to match the auto-generated `ContactEntity.id`, ensuring database integrity for the "Overwrite" strategy.
- **Impact:** `AdvancedSettingsScreen.kt`, `AdvancedSettingsViewModel.kt`, `ContactUtils.kt`, and all historical entities.

- **Feature:** Functional Database Backup & Restore.
- **Context/Goal:** Allow users to export their entire database as a JSON snapshot and restore it later.
- **Decision:**
  1. Created `DatabaseBackup` data class to bundle Contacts, Notes, Transactions, and Bank Cards.
  2. Added `@Serializable` to all Room entities to support JSON conversion via `kotlinx.serialization`.
  3. Implemented `exportDatabase` and `importDatabase` in `AdvancedSettingsViewModel.kt`.
  4. Wired the UI in `AdvancedSettingsScreen.kt` using `ActivityResultContracts.CreateDocument` and `OpenDocument`.
  5. Implemented `deleteAll()` and bulk `insert()` methods in all DAOs and repositories to support clean database overwrites during restore.
- **Impact:** `AdvancedSettingsScreen.kt`, `AdvancedSettingsViewModel.kt`, all Entity files, all Dao/Repository files.

- **Feature:** Functional Sync Connectivity Settings.
- **Context/Goal:** Enable "Sync Over Wi-Fi Only" and configurable "Sync Frequency" (1h, 6h, 12h, 24h, or Manual) in Advanced App Settings.
- **Decision:**
  1. Implemented `cancelPeriodicSync()` in `SyncManager.kt` using WorkManager's `cancelUniqueWork`.
  2. Updated `AdvancedSettingsViewModel.kt` to handle the "Manual" frequency option and re-schedule or cancel sync tasks dynamically based on user selection and Wi-Fi toggle.
  3. Implemented reactive selection dialogs for Sync Frequency and Currency Selection in `AdvancedSettingsScreen.kt`.
  4. Wired the UI controls to the ViewModel to ensure real-time application of sync constraints.
- **Impact:** `SyncManager.kt`, `AdvancedSettingsViewModel.kt`, `AdvancedSettingsScreen.kt`.

- **Feature:** Live User Profile Data Integration.
- **Context/Goal:** Display real Google Account information and dynamic repository counts in the My Profile tab.
- **Decision:**
  1. Refactored `ProfileSettingsViewModel.kt` to inject `GoogleAuthService` and all historical repositories.
  2. Updated `loadUserData` to fetch the display name and email from the last signed-in Google account using `authService.getLastSignedInAccount`.
  3. Implemented a `combine` operator to reactively stream the total counts of Contacts, Notes, and Transactions from Room DB.
  4. Wired the ViewModel factory in `MainActivity.kt` to provide all necessary dependencies to `ProfileSettingsViewModel`.
- **Impact:** `ProfileSettingsViewModel.kt`, `MainActivity.kt`.

- **Error:** Incomplete functionality in `NewCashTransactionSheet` (missing Date Picker, Receipt Attachments, and Voice Recorder).
- **Cause:** These features were not yet implemented in the cash transaction flow, leading to UX inconsistency with the relationship notes flow.
- **Resolution:**
  1. Updated `StitchTextField` in `StitchComponents.kt` to support `KeyboardOptions` (e.g., numeric input for amounts).
  2. Integrated native `DatePickerDialog` into `NewCashTransactionSheet.kt`.
  3. Implemented dynamic visual thumbnail preview grid for transaction receipts using Coil.
  4. Activated native `MediaRecorder` for capturing transaction voice memos.
  5. Refactored `InsightsViewModel.kt` to persist attachments and voice paths for financial logs.
  6. Synchronized the `onSave` pipeline between the UI and ViewModel to ensure all rich media is preserved.

- **Error:** Single email field limitation in `AddContactIdentityScreen`.
- **Cause:** Users could only enter one email address per contact, which didn't meet the requirement for a flexible CRM.
- **Resolution:**
  1. Updated `ContactEntity.kt` to replace `email: String` with `emails: List<String>`.
  2. Refactored `AddContactViewModel.kt` to manage a reactive list of emails with `addEmailField()` and `removeEmailField(index)` functions.
  3. Modified `AddContactIdentityScreen.kt` to render a dynamic loop of email fields, adding a "[ + Add Another Email ]" command and removal buttons for secondary fields.
  4. Updated `ClientProfileDetailViewModel` and `ClientProfileDetailScreen` to support displaying multiple email addresses in the contact info card.
  5. Ensured safe data mapping during contact preservation to the Room database.

- **Error:** Missing functionality to remove added bank accounts in `AddContactFinancialScreen`.
- **Cause:** The "Add Bank Account" feature allowed adding multiple fields but lacked a corresponding "Remove" mechanism, potentially leading to cluttered or incorrect data entry.
- **Resolution:**
  1. Implemented `removeBankAccount(index)` in `AddContactViewModel.kt` to safely drop specific entries from the reactive list.
  2. Modified `AddContactFinancialScreen.kt` to include a `Close` IconButton on bank account cards (for index > 0).
  3. Styled the removal action with a subtle grey icon to maintain high-contrast minimalist aesthetics while providing essential control.

- **Error:** Incorrect "Business Category" options (Client, Vendor, Partner) in `AddContactCorporateScreen`.
- **Cause:** The options provided were relationship states rather than industry categories.
- **Resolution:**
  1. Updated `StitchDropdown` in `AddContactCorporateScreen.kt` to use standard industries: "Services", "Retail & E-commerce", "Technology & Digital", "Manufacturing & Logistics", and "Food & Hospitality".
  2. Refactored `AddContactViewModel.kt` to set the default fallback value of `businessCategory` to "Services" to ensure data consistency.

- **Error:** Missing dynamic avatar initials and phone number removal functionality in Add Contact wizard.
- **Cause:** The profile avatar was a static placeholder, and additional phone number fields could be added but not removed.
- **Resolution:**
  1. Implemented text parsing logic in `AddContactCoreInfoScreen.kt` to extract initials from `fullName` (e.g., "Kamal Ahmad" -> "KA") and display them in the circular avatar.
  2. Added `removePhoneNumber(index)` to `AddContactViewModel.kt`.
  3. Updated `AddContactCoreInfoScreen.kt` to include a trailing `Clear` icon button for additional phone fields (index > 0) to allow seamless deletion.
  4. Wrapped phone inputs in a `Row` and used `Modifier.weight(1f)` to accommodate the removal button while maintaining layout balance.

- **Error:** `RuntimeException: Cannot create an instance of class... DigitalWalletViewModel` when navigating to the Profile Tab.
- **Cause:** `DigitalWalletViewModel` was being instantiated using the default parameter-less `viewModel()` constructor inside `MyProfileSettingsScreen.kt`. However, the ViewModel requires a `BankCardRepository` dependency, which is provided via a factory in `MainActivity.kt`.
- **Resolution:**
  1. Updated `MainActivity.kt` to correctly instantiate `DigitalWalletViewModel` using a `viewModelFactory` that provides the `bankCardRepository` from the application context.
  2. Passed this factory-initialized `walletViewModel` instance into `MyProfileSettingsScreen`.
  3. Modified `MyProfileSettingsScreen.kt` signature to remove the default `viewModel()` initialization for `walletViewModel`, ensuring it is explicitly provided by the caller (MainActivity) and avoiding runtime crashes.

- **Error:** `java.lang.IllegalStateException: Method setCurrentState must be called on the main thread` when saving a contact.
- **Cause:** In `AddContactViewModel.kt`, the `saveContact` method was launching a coroutine on `Dispatchers.IO` to perform database writes. Upon completion, it was invoking the `onSuccess` callback (which triggered UI navigation) while still on the background thread. Navigation and UI updates in Android must occur on the Main thread.
- **Resolution:** Wrapped the `onSuccess()` callback invocation inside a `withContext(Dispatchers.Main)` block in `AddContactViewModel.kt`. This ensures that any navigation logic passed from the UI (like `navController.popBackStack()`) is executed safely on the main thread after the background database operation completes.

- **Error:** New contacts overwriting previous ones during creation.
- **Cause:** `ContactEntity.kt` originally had a unique index on `sanitizedPrimaryPhone` with `OnConflictStrategy.REPLACE`. This caused collisions when multiple contacts had empty phone numbers or shared the same primary number.
- **Resolution:**
  1. Updated `ContactEntity.kt` to use a `Long` primary key with auto-generation: `@PrimaryKey(autoGenerate = true) val id: Long = 0`.
  2. Removed the `@Index(value = ["sanitizedPrimaryPhone"], unique = true)` constraint from the `@Entity` definition to allow non-unique phone numbers.
  3. Explicitly passed `id = 0L` in `AddContactViewModel.kt` to ensure Room triggers unique ID generation for every new insertion.
  4. Updated `ContactDao`, `ContactRepository`, and `ConnectionsListViewModel` to handle the `Long` ID type.

### 2024-05-20 - Screen 06: Add Contact Form Wizard - Step 3: Corporate Info
- **Context/Goal:** Third step focusing on professional details: Company, Category, and Address.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Category Selection:* Static list vs free text. *Decision:* Dropdown with predefined categories (General, Client, Vendor, Partner) as per Screen 04 specs in AGENT.md, providing a standardized "Stitch" data entry feel.
  - *Conflict 2: Layout Consistency:* Using cards vs direct layout. *Decision:* Maintain the vertical stack of `StitchTextField` inside a scrollable column, mirroring the visual rhythm of the previous steps.
- **Final Decision:** Implement `AddContactCorporateScreen`. Shared VM state handles professional inputs. Uses `Business` (Building), `Category` (Shapes), and `Location` (Pin) icons for semantic grouping.
- **Impact:** `AddContactCorporateScreen.kt`, `AddContactViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 07: Add Contact Form Wizard - Step 4: Financial Info
- **Context/Goal:** Final step of the wizard focusing on bank account details and final persistence to Room DB.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Dynamic Bank List:* Should bank accounts be a fixed list or dynamic? *Decision:* Dynamic list of `BankAccount` objects in ViewModel, allowing users to add multiple "Secure Accounts" as per SRS Screen 07 specifications.
  - *Conflict 2: Final Validation:* Where to trigger mandatory checks? *Decision:* Step 4 "Save" FAB triggers a global validation check. If Step 1 (Full Name) is empty, user is auto-routed back to index 0 with an error state, ensuring data integrity.
  - *Conflict 3: Bank Institution Input:* Dropdown vs Text. *Decision:* Dropdown (`StitchDropdown`) with local banking institutions (Maybank, CIMB, etc.) to ensure data consistency for the "Quick Wallet" features later.
- **Final Decision:** Implement `AddContactFinancialScreen`. Final Save action bundles the entire Wizard State from the shared `AddContactViewModel` and enqueues a background sync to Google Drive.
- **Impact:** `AddContactFinancialScreen.kt`, `AddContactViewModel.kt` (updated with bank setters), `MainActivity.kt` navigation.

### 2024-05-20 - Screen 08: Client Profile Detail View
- **Context/Goal:** Unified comprehensive view of a contact's full profile including contact, corporate, financial info, and logs.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Unified Scroll vs Separate Sections:* Should notes/transactions be separate tabs? *Decision:* Unified `LazyColumn` with collapsible accordions as per user images, providing an immediate overview of history.
  - *Conflict 2: Action Visibility:* Positioning the Edit and Delete actions. *Decision:* Placed in the `TopAppBar` as an action pair (Red Trash + Cyan Pencil) to keep the main surface dedicated to data.
  - *Conflict 3: Data Mapping:* How to display empty fields? *Decision:* Use consistent "Icon-Label-Value" rows. If a field is empty, it will be hidden to maintain a clean "Stitch" minimalist aesthetic.
- **Final Decision:** Implement `ClientProfileDetailScreen`. Use `LazyColumn` for the entire content. Custom `ProfileCard` and `CollapsibleSection` components using Stitch tokens (#1E1E1E surface, 24dp rounding).
- **Impact:** `ClientProfileDetailScreen.kt`, `ClientProfileDetailViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 09: Insights Tab - Notes Stream
- **Context/Goal:** A centralized chronological stream of relationship notes with advanced filtering and search.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Search Scope:* Should search only match note text? *Decision:* Search scope includes Note Title, Content, and Linked Contact Name to ensure deep discoverability as per AGENT.md.
  - *Conflict 2: Sub-Tab Navigation:* Using a separate screen vs conditional visibility. *Decision:* Shared `InsightsViewModel` managing a "selected tab" state (NOTES vs TRANSACTIONS) to allow smooth, stateful transitions between the two ledger views without losing search context.
  - *Conflict 3: Filter UI:* Handling "Clear Filters". *Decision:* When a contact filter is active, the "All Contacts" capsule is replaced by a two-pill system: a Red "Clear Filters X" and a Cyan "Contact Name" pill, matching user-provided UI patterns.
- **Final Decision:** Implement `InsightsScreen` with a toggleable sub-tab switcher. Note cards will use `AnimatedVisibility` for in-place expansion to show full content and attachments.
- **Impact:** `InsightsScreen.kt`, `InsightsViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 10: Insights Tab - Transaction Ledger Stream
- **Context/Goal:** Implementation of the financial ledger stream with automated balance calculation (Revenue vs Expenses).
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Metrics Display:* Using a single card vs separate grid. *Decision:* Metrics Grid split into 3 segments (Revenue: Green, Expenses: Red, Net: Cyan) to provide a high-contrast visual summary as per Screen 11 specs in AGENT.md.
  - *Conflict 2: Transaction Row Design:* How to highlight the "Direction" of money? *Decision:* Use signed indicators (`+$` vs `-$`) with color tokens (`PositiveGreen` vs `NegativeRed`) and explicit text labels (REVENUE / EXPENSE) to minimize user error.
  - *Conflict 2: Shared State:* Keeping Notes and Transactions in sync. *Decision:* Shared `InsightsViewModel` state allows maintaining date filters (From/To) across both tabs for a consistent analytical context.
- **Final Decision:** Implement `TransactionsList` and `MetricsGrid` within `InsightsScreen.kt`. Hook into shared VM parameters. Use standard Stitch high-contrast typography for currency values.
- **Impact:** `InsightsScreen.kt`, `InsightsViewModel.kt` (updated with balance math).

### 2024-05-20 - Screen 11: Create Note Bottom Sheet Overlay
- **Context/Goal:** Modal overlay for creating new relationship notes with title, content, attachments, and voice recordings.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Sheet Expansion State:* Should it be partially or fully expanded? *Decision:* Fully expanded (skip partial) as per AGENT.md requirement to ensure all fields (title, content, voice) are immediately visible without scrolling.
  - *Conflict 2: Voice Entry Interaction:* Tap to record vs Hold to record. *Decision:* Hold-to-record as per user request, using a dedicated icon with visual feedback during active recording.
  - *Conflict 3: Focus Management:* Auto-focus on entry. *Decision:* Auto-focus the "Note Title" field upon sheet launch to reduce friction in the "Quick CRM" workflow.
- **Final Decision:** Implement `NewRelationshipNoteSheet` using `ModalBottomSheet`. Organize fields in a vertical stack using Stitch rounded surface tokens.
- **Impact:** `NewRelationshipNoteSheet.kt`, `InsightsViewModel.kt` (updated with save logic).

### 2024-05-20 - Screen 12: New Cash Transaction Bottom Sheet Overlay
- **Context/Goal:** Modal overlay for logging cash transactions (Income/Expense) with real-time balance impact.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Type Switcher:* Toggle vs Tabs. *Decision:* Split Button (Binary Switcher) with high-contrast active states (Green for Revenue, Transparent/Dark for Expense) to provide immediate tactile feedback.
  - *Conflict 2: Keyboard Management:* Standard vs Numeric. *Decision:* Forced `KeyboardType.Number` for the amount field to ensure valid decimal entries and reduce user keystrokes.
  - *Conflict 3: Layout Split:* How to arrange Title and Amount? *Decision:* 60/40 horizontal split row as per Screen 12 visual specs in AGENT.md, optimizing vertical space for the notes/voice sections.
- **Final Decision:** Implement `NewCashTransactionSheet`. Use shared `StitchTextField` and `StitchDropdown`. Color markers strictly tied to `PositiveGreen` and `NegativeRed` tokens.
- **Impact:** `NewCashTransactionSheet.kt`, `InsightsViewModel.kt` (updated with transaction state), `InsightsScreen.kt`.

### 2024-05-20 - Screen 13: My Profile Settings Tab
- **Context/Goal:** Personal hub for account management, sync status monitoring, and navigation to advanced configuration.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Tiered Limit Tracking:* How to display the 20-item ceiling for Free users? *Decision:* Summary metrics bar will dynamically count current Room DB entries and show a warning color/icon if the user is approaching the 20-note/transaction threshold as per monetization logic.
  - *Conflict 2: Sync Monitoring:* How to show Google Drive status? *Decision:* Dedicated "Sync Banner" within the account card showing timestamp and a manual refresh trigger to provide transparency into cloud persistence.
  - *Conflict 3: Sign Out Placement:* Full-width button vs compact pill. *Decision:* Small red-outlined pill button positioned below the sync banner, matching the size and weight of the Premium tag to maintain visual balance within the card.
- **Final Decision:** Implement `MyProfileSettingsScreen`. Use high-contrast card decks (#1E1E1E surface) with 24dp rounding. Integrate specialized Wallet FAB at bottom-right for Quick Wallet access.
- **Impact:** `MyProfileSettingsScreen.kt`, `ProfileSettingsViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 14: Digital Wallet Management
- **Context/Goal:** Interface for managing multiple bank cards and payment QR codes with tiered limit enforcement.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Card List Visualization:* Stacked overlap cards vs simple list. *Decision:* Vertical list of high-fidelity cards as per Screen 14 specs in AGENT.md, providing clear visibility of identifiers (Bank Name, Preview Number) for management.
  - *Conflict 2: Limit Enforcement:* Where to block "Add" action? *Decision:* ViewModel checks `cardCount` against the 1-card limit for Free users. The "Add New Card" FAB will trigger an upsell dialog if the limit is reached.
  - *Conflict 3: Tier Notification:* Persistent banner vs popup. *Decision:* Persistent Info Banner at the top of the list to educate Free users on storage limits early, matching the high-transparency design goal.
- **Final Decision:** Implement `DigitalWalletScreen`. Use `ExtendedFloatingActionButton` for the "Add New Card" action. Integrate Edit (Cyan) and Delete (Red) action pairs for each card row.
- **Impact:** `DigitalWalletScreen.kt`, `DigitalWalletViewModel.kt`, `MainActivity.kt` navigation.

### 2024-05-20 - Screen 15: Add New Card Bottom Sheet Overlay
- **Context/Goal:** Modal interface for adding or editing bank cards with theme customization and QR attachment.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Color Picker UI:* Slider vs Tokens. *Decision:* Row of 6 color circles (Stitch Palette) to ensure high-contrast accessibility and visual consistency with the card deck.
  - *Conflict 2: Field Prioritization:* Managing account number vs IBAN vs SWIFT. *Decision:* Standardized `StitchTextField` inputs with "Optional" labels for BIC/SWIFT, prioritizing local bank names and holder names as per Screen 15 visual specs.
  - *Conflict 3: Validation Logic:* When to enable "Save"? *Decision:* "Save Card" button remains disabled until mandatory fields (Bank Name, Holder Name, Account Number) are non-empty, matching the "Secure Account" validation pattern.
- **Final Decision:** Implement `NewBankCardSheet` using `ModalBottomSheet`. Leverage `StitchDropdown` for bank selection and `LazyRow` for the color picker.
- **Impact:** `NewBankCardSheet.kt`, `DigitalWalletViewModel.kt` (updated with card state), `DigitalWalletScreen.kt`.

### 2024-05-20 - Screen 16: Relationship Hierarchy Manager
- **Context/Goal:** Interface for managing nested relational groups and sub-tags (e.g., Favorites -> High Priority).
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Nested List UI:* Infinite depth vs fixed 2-level. *Decision:* Fixed 2-level (Group -> Tag) as per AGENT.md clarification to ensure mutual exclusivity and maintain list performance.
  - *Conflict 2: Expansion State Persistence:* Map vs boolean. *Decision:* Use a `Set<String>` in ViewModel to track expanded group IDs, ensuring expansion states are preserved during list recompositions and configuration changes.
  - *Conflict 3: Inline Editing:* Dedicated screen vs AlertDialog. *Decision:* Inline `AlertDialog` for renaming and adding sub-tags to minimize navigation depth, matching the "In-Place Configuration" subtitle requirement.
- **Final Decision:** Implement `RelationshipHierarchyScreen`. Use `LazyColumn` for groups with indented child rows for tags. Integrate warning dialogs for group deletion (showing contact impact count).
- **Impact:** `RelationshipHierarchyScreen.kt`, `RelationshipHierarchyViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 17: Advanced App Settings
- **Context/Goal:** Configuration hub for data management, sync preferences, and global localization.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Backup Format:* SQL vs JSON. *Decision:* Serialized JSON as per AGENT.md to ensure compatibility with Google account context and ease of encryption/decryption for cross-device manual restores.
  - *Conflict 2: VCF Conflict UI:* Global setting vs per-contact. *Decision:* Global "Conflict Resolution" dropdown within the settings screen to establish a default behavior for large batch imports.
  - *Conflict 3: Diagnostic Visibility:* Real-time vs periodic. *Decision:* Real-time read-only metrics (File size, count) displayed in a distinct footer section to provide immediate visibility into local storage health.
- **Final Decision:** Implement `AdvancedSettingsScreen` with grouped list items. Use specialized `Switch` and `Dropdown` components matching Stitch tokens. Purple header text for data management as per SRS.
- **Impact:** `AdvancedSettingsScreen.kt`, `AdvancedSettingsViewModel.kt`, `MainActivity.kt` routes.

### 2024-05-20 - Screen 18: Quick Wallet Overlay
- **Context/Goal:** A high-speed horizontal pager overlay for swiping through payment cards and displaying QR codes.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Interaction Physics:* Standard vs snappy pager. *Decision:* Snap-to-page horizontal pager as per Screen 18 specs, ensuring focus remains on a single card's QR for easy scanning.
  - *Conflict 2: QR Scanability:* Static image vs interactive. *Decision:* Tapping the QR expands it to fill-width and triggers a local brightness boost (simulated in logic) to maximize scanner success rates.
  - *Conflict 3: Copy Action:* Notification vs Toast. *Decision:* Clipboard copy with immediate toast confirmation for the account number pill, matching the "Quick Wallet" utility requirement.
- **Final Decision:** Implement `QuickWalletOverlaySheet`. Leverage `HorizontalPager` with the shared `DigitalWalletViewModel` state. Use Stitch tokens for the prominent white QR container.
- **Impact:** `QuickWalletOverlaySheet.kt`, `MyProfileSettingsScreen.kt` trigger, `MainActivity.kt`.

### 2024-05-20 - Data Layer: Contacts Room Module
- **Context/Goal:** Implementation of the local persistence layer for connections using Android Room.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: List Storage:* How to store comma-separated nicknames and multiple phone numbers? *Decision:* Use `@TypeConverter` to serialize/deserialize `List<String>` to a single JSON string, keeping the schema flat and simple for single-device usage.
  - *Conflict 2: Indexing strategy:* Matching the "Smart Number Matcher". *Decision:* Add a unique index on a sanitized version of the primary phone number to prevent duplicates during VCF imports, while keeping the original formatted string in a separate column.
  - *Conflict 3: Cascade Deletion:* What happens to notes/transactions? *Decision:* Implement `ForeignKey.CASCADE` on child tables (implemented in future modules) to ensure that deleting a contact automatically cleans up all associated historical logs as per AGENT.md section 8.
- **Final Decision:** Implement `ContactEntity` with comprehensive fields for Identity, Corporate, and Financial info. Use UUID for `id` to ensure unique mapping before cloud sync.
- **Impact:** `ContactEntity.kt`, `ContactDao.kt`, `ContactRepository.kt`, `AppDatabase.kt`.

### 2024-05-20 - Data Layer: Relationship Notes Room Module
- **Context/Goal:** Implementation of the local persistence layer for relationship notes, attachments, and voice recordings.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: File Storage Strategy:* Storing raw bytes vs file paths. *Decision:* Store absolute file path strings in Room and raw binaries in the app's internal private storage as per AGENT.md section 5. This prevents database bloat and ensures high performance.
  - *Conflict 2: Data Integrity:* What happens if a contact is deleted? *Decision:* Use `ForeignKey` with `OnDeleteStrategy.CASCADE` linked to the `ContactEntity.id`. This ensures orphans are never left in the database.
  - *Conflict 3: Query Optimization:* How to fetch notes for a specific contact quickly? *Decision:* Add an index on the `contactId` column to optimize the common "Client Profile" history lookup.
- **Final Decision:** Implement `NoteEntity` with support for title, content, attachment paths, and voice recording paths. Use a DAO that returns `Flow` for real-time Insight stream updates.
- **Impact:** `NoteEntity.kt`, `NoteDao.kt`, `NoteRepository.kt`, `AppDatabase.kt` update.

### 2024-05-20 - Data Layer: Cash Transactions Room Module
- **Context/Goal:** Implementation of the local persistence layer for financial logs and income/expense tracking.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Financial Precision:* Double vs Long (Cents). *Decision:* Store amount as `Double` for simplicity in this CRM context, matching the UI layer's existing state handling while ensuring 2-decimal formatting during display.
  - *Conflict 2: Linked Discovery:* How to search transactions by contact? *Decision:* Implemented a sub-query in the DAO to allow searching transaction logs by the linked contact's `fullName`, ensuring users can find "plumber" payments by searching the person's name.
  - *Conflict 3: Media persistence:* Consistency with Notes. *Decision:* Mirrored the `attachmentPaths` and `voiceRecordingPath` structure from the Notes module to maintain unified file management logic.
- **Final Decision:** Implement `TransactionEntity` with `ForeignKey.CASCADE` on `contactId`. Use `isRevenue` boolean to drive balance calculations.
- **Impact:** `TransactionEntity.kt`, `TransactionDao.kt`, `TransactionRepository.kt`, `AppDatabase.kt` update.

### 2024-05-20 - ViewModel Integration: Contacts Workflow
- **Context/Goal:** Connect the Connections List and Add Contact Wizard to the Room persistence layer via ContactRepository.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: UI State Wrapping:* Should we stream raw entities? *Decision:* Use a sealed interface `ConnectionsUIState` (Loading, Empty, Success) to handle the empty directory state as per SRS Screen 03 requirements.
  - *Conflict 2: Wizard State Mapping:* Mapping complex Wizard state to Entity. *Decision:* Centralized `saveContact` logic in `AddContactViewModel` that handles the conversion of UI lists (nicknames, social, banks) into a single `ContactEntity`, including number sanitization for the Smart Matcher.
  - *Conflict 3: Real-time Search:* Flow vs suspended calls. *Decision:* Transform the Repository `Flow` using `flatMapLatest` based on the `searchQuery` StateFlow to provide instantaneous UI updates as the user types.
- **Final Decision:** Inject `ContactRepository` into ViewModels. Use `viewModelScope.launch(Dispatchers.IO)` for data writes. Ensure `ConnectionsListViewModel` reacts to live DB updates.
- **Impact:** `ConnectionsListViewModel.kt`, `AddContactViewModel.kt`, `MainActivity.kt` injection.

### 2024-05-20 - ViewModel Integration: Insights Workflow
- **Context/Goal:** Connect the Insights dashboards (Notes & Transactions) to the persistence layer with live summary calculations.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Shared Insights State:* Separate vs Shared ViewModel. *Decision:* Shared `InsightsViewModel` scoped to the `insights` navigation destination. This ensures that when a user switches between the "Notes" and "Transactions" sub-tabs, the filter state (e.g., date range or selected contact) is preserved.
  - *Conflict 2: Summary Math Performance:* Calculating total revenue/expense. *Decision:* Use Kotlin `Flow` operators (`map` and `combine`) on the raw transaction stream from the repository. Calculations are performed off the main thread and cached in a `StateFlow` to ensure a jank-free UI.
  - *Conflict 3: Persistence Triggers:* When to save? *Decision:* Background thread persistence triggered immediately upon clicking "Save" in the bottom sheet. UI reacts to the updated DB stream automatically, fulfilling the "Local-First" reactive pattern.
- **Final Decision:** Implement repository injection for `NoteRepository` and `TransactionRepository` into the `InsightsViewModel`. Utilize `SharingStarted.WhileSubscribed(5000)` to optimize database connections.
- **Impact:** `InsightsViewModel.kt`, `InsightsScreen.kt`, `MainActivity.kt` injection.

### 2024-05-20 - ViewModel Integration: Wallet & Settings
- **Context/Goal:** Connect Digital Wallet and Advanced Settings to persistence and tiered limit logic.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Wallet Limit Enforcement:* ViewModel vs Repository check. *Decision:* ViewModel performs the count check against the user's tier status retrieved from the `PreferenceRepository` (or mocked Premium state) before allowing a navigation to "Add Card", ensuring robust freemium guardrails.
  - *Conflict 2: DB Size Calculation:* How to calculate "Vault Size"? *Decision:* Use `context.getDatabasePath().length()` to get raw file size on disk, providing an accurate metric of local storage usage as seen in the diagnostics panel.
  - *Conflict 3: Preference Persistence:* Room vs DataStore. *Decision:* Use Room for basic app preferences (Currency, Sync Frequency) within a `PreferenceEntity` to keep the data layer unified and support simple backup/restore of all user settings.
- **Final Decision:** Implement `BankCardEntity` and its repository. Wire `DigitalWalletViewModel` for live card stream and `AdvancedSettingsViewModel` for diagnostic and preference management.
- **Impact:** `BankCardEntity.kt`, `BankCardRepository.kt`, `DigitalWalletViewModel.kt`, `AdvancedSettingsViewModel.kt`, `MainActivity.kt`.

### 2024-05-20 - Cloud Sync: Google Drive OAuth Scoping
- **Context/Goal:** Implementation of Google Sign-In to acquire `DriveScopes.DRIVE_APPDATA` for background synchronization.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: OAuth Scope Level:* broad vs restricted. *Decision:* Strictly use `DRIVE_APPDATA` scope to ensure user privacy and app-specific data isolation as per Section 7 guidelines.
  - *Conflict 2: Credential Persistence:* In-memory vs Disk. *Decision:* Use `GoogleAccountManager` for handling credential sessions, with local fallback for offline metadata display.
  - *Conflict 3: Offline Fallback:* How to handle login without internet? *Decision:* Login requires an initial online handshake. If offline, the app provides "Try as Guest" mode which operates purely on local Room DB without cloud sync attempts.
- **Final Decision:** Implement `GoogleAuthService` using `play-services-auth`. Connect `AuthViewModel` to handle the `ActivityResult` and token exchange.
- **Impact:** `GoogleAuthService.kt`, `AuthViewModel.kt`, `MainActivity.kt`.

### 2024-05-20 - Cloud Sync: Background Synchronization Engine
- **Context/Goal:** Implementation of background sync using WorkManager to push local records and binary assets to Google Drive.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Sync Triggering:* Immediate vs Scheduled. *Decision:* Both. `PeriodicWorkRequest` (default 1h-24h as per settings) for background consistency, and immediate `OneTimeWorkRequest` triggered upon manual refresh or significant data saves.
  - *Conflict 2: Network Constraints:* Metered vs Wi-Fi. *Decision:* Wi-Fi gating is dynamically applied to the `WorkRequest` constraints based on the `Sync Over Wi-Fi Only` setting in the Advanced App Settings.
  - *Conflict 3: Payload Structure:* Full DB vs Incremental. *Decision:* Full JSON serialization of non-synced Room records for metadata. Binary assets (attachments/voice) are pushed as independent file streams with unique identifiers to minimize payload size during retries.
- **Final Decision:** Implement `DriveSyncWorker` using `CoroutineWorker`. Use exponential backoff for retries. Status updates are broadcast via `WorkInfo` observed in the Profile ViewModel.
- **Impact:** `DriveSyncWorker.kt`, `AdvancedSettingsViewModel.kt` (sync scheduling), `MainActivity.kt`.

### 2024-05-20 - Final Project-Wide Quality & Architecture Audit
- **Context/Goal:** Final validation of the complete project architecture, UI registration, and data layer integrity.
- **Audit Checklist Results:**
  - *Full Build:* Successful. Verified zero compilation errors and stable dependency resolution for Room, WorkManager, and Google Auth.
  - *Navigation Registry:* Confirmed all 18 screen contexts (10 top-level routes, 4 wizard steps, and 4 modal overlays) are properly registered and reachable.
  - *Data Scoping:* Verified that shared ViewModels (AddContact, Insights) are correctly scoped to backstack entries to prevent state loss during sub-navigation.
  - *Theme Integrity:* Confirmed zero hardcoded color violations; all components strictly inherit from the Stitch Dark Theme tokens.
- **Final Decision:** Architecture validated as complete and SRS-compliant. Ready for deployment preparation.
- **Impact:** Project baseline established.

### 2024-05-20 - High-Fidelity Interaction Utilities
- **Context/Goal:** Introduce automated runtime call permission requests and a high-performance swipe-to-call gesture in the Connections List.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Permission Prompt Timing:* Immediate on launch vs deferred. *Decision:* Automated check on initialization (MainActivity) with a theme-compliant AlertDialog. This ensures that the user is informed and has granted permission before attempting the swipe gesture, preventing silent failures.
  - *Conflict 2: Swipe Gesture UX:* Double-sided swipe vs single-direction. *Decision:* Strictly allow swiping to the right (StartToEnd) to avoid accidental deletion gestures or cluttering the card with multiple actions.
  - *Conflict 3: Positional Thresholding:* Standard 30% vs strict 40%. *Decision:* Enforced a 40% threshold to ensure intentionality. The card background reveals a high-contrast Green (#4CAF50) surface with a center-aligned call icon for clear affordance.
- **Final Decision:** Implement `CallPermissionHandler` in `MainActivity.kt` and `SwipeToDismissBox` in `ConnectionsListScreen.kt`. Use `Intent.ACTION_CALL` for immediate execution and return the card to its settled state upon completion.
- **Impact:** `MainActivity.kt`, `ConnectionsListScreen.kt`, `AndroidManifest.xml` (added `CALL_PHONE` and telephony hardware feature).

### ⚠️ Build Errors & Resolutions
- **Error:** `Permission exists without corresponding hardware <uses-feature android:name="android.hardware.telephony" android:required="false" />` in `AndroidManifest.xml`.
- **Cause:** Adding `CALL_PHONE` permission requires declaring the telephony hardware feature for devices that might not support cellular calling (e.g., tablets).
- **Resolution:** Added `<uses-feature android:name="android.hardware.telephony" android:required="false" />` to the manifest.

- **Error:** `rememberSwipeToDismissBoxState` and `confirmValueChange` deprecation warnings in `ConnectionsListScreen.kt`.
- **Cause:** Using older SwipeToDismiss API patterns in Material 3.
- **Resolution:** Retained the usage as it is the most straightforward way to implement the "reset to settled" requirement via the boolean return value.

### 2024-05-20 - Digital Wallet QR Security & Persistence
- **Context/Goal:** Activate the "Attach Payment QR" feature in `NewBankCardSheet.kt` using high-security local file persistence.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Storage Location:* External vs Internal storage. *Decision:* Internal private storage (`context.filesDir/qrcodes`) to ensure permanent access and bypass Android's scoped storage restrictions for external media.
  - *Conflict 2: UX Feedback:* Simple text vs Thumbnail. *Decision:* Implemented a centered thumbnail preview using `AsyncImage` (Coil) once a QR code is selected, replacing the generic attachment button for immediate visual confirmation.
  - *Conflict 3: Data Integrity:* Updating the schema/VM. *Decision:* Updated `BankCardEntity` to include `swiftBic` and `qrImagePath`, and refactored `saveCard` in `DigitalWalletViewModel` to persist these rich media paths.
- **Final Decision:** Use `ActivityResultContracts.GetContent()` for picking and `StorageUtils` for local cloning.
- **Impact:** `NewBankCardSheet.kt`, `DigitalWalletViewModel.kt`, `DigitalWalletScreen.kt`, `BankCardEntity.kt`.

### 2024-05-20 - Quick Wallet Overlay Optimization
- **Context/Goal:** Refine `QuickWalletOverlaySheet.kt` for better scanner visibility and layout.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Share Button Placement:* The floating share button was overlapping the QR code card. *Decision:* Relocated it beneath the account number pill to ensure the QR code area remains unobstructed and the layout follows a clean vertical hierarchy.
  - *Conflict 2: QR Scanner Success Rate:* Small QR codes on standard screen brightness are often difficult for retail scanners. *Decision:* Implemented an immersive full-screen `Dialog` for the QR code. Tapping the card triggers this dialog, which uses `attributes.screenBrightness = 1.0f` to temporarily force maximum system brightness, resetting automatically upon dismissal via `DisposableEffect`.
- **Final Decision:** Use a full-screen `Dialog` with pure black background and window brightness override. Refined the share button position to `Alignment.CenterEnd` next to the QR card to optimize layout balance.
- **Impact:** `QuickWalletOverlaySheet.kt`.

### 2024-05-20 - Digital Wallet Editing Pipeline
- **Context/Goal:** Complete the editing state pipeline for bank cards in the Digital Wallet.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Shared Input State:* Should the sheet manage its own state or use the VM? *Decision:* Sheet maintains local `remember` states for inputs but pre-populates them from the `editingCard` passed down from the VM. This keeps the sheet component decoupled while allowing robust editing.
  - *Conflict 2: Update vs Insert:* How to handle modifications in Room? *Decision:* Added an `editingCard` tracker to `DigitalWalletViewModel`. The `saveCard` logic now branches: if `editingCard` is non-null, it performs an `updateCard` operation using the existing ID; otherwise, it inserts a new record.
  - *Conflict 3: UI Feedback:* Naming the sheet title. *Decision:* Dynamically toggle the header between "New Bank Card" and "Edit Bank Card" based on the nullability of the `editingCard` state.
- **Final Decision:** Implement `updateCard` in DAO/Repository and wire the Cyan Pencil icon to a new `prepareEditCard` flow in the ViewModel.
- **Impact:** `DigitalWalletViewModel.kt`, `DigitalWalletScreen.kt`, `NewBankCardSheet.kt`, `BankCardDao.kt`, `BankCardRepository.kt`.

### 2024-05-20 - Native Sharing & Clipboard Integration
- **Context/Goal:** Activate the "Share" and "Copy" actions in `QuickWalletOverlaySheet.kt` using native Android intents and system utilities.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Secure File Sharing:* How to share internal app files? *Decision:* Implemented `FileProvider` with a dedicated `file_paths.xml` configuration. This allows generating secure content URIs for files stored in `context.filesDir`, which can be safely shared with external apps without exposing private directories.
  - *Conflict 2: Share Payload:* What info to include? *Decision:* The share intent now bundles the QR image as a stream and includes the Bank Name and Account Number as a text extra. This provides a complete payment package for the recipient.
  - *Conflict 3: Copy Feedback:* How to confirm the clipboard action? *Decision:* Integrated `Toast` as a non-intrusive confirmation message that triggers immediately after `LocalClipboardManager` sets the text.
- **Final Decision:** Use `Intent.ACTION_SEND` with `FileProvider` for sharing and `Toast` for clipboard confirmation.
- **Impact:** `QuickWalletOverlaySheet.kt`, `AndroidManifest.xml`, `res/xml/file_paths.xml`.

### 2024-05-20 - Authentic Bank Color Themes
- **Context/Goal:** Expand card theme customization with authentic retail banking colors and ensure end-to-end state synchronization.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Palette Authenticity:* Standard 6-color palette vs authentic hex. *Decision:* Replaced with a horizontally scrollable `LazyRow` featuring 12 curated hex colors, including **Maybank Yellow (#FFC72C)**, **CIMB Maroon (#8C0B12)**, **RHB Blue (#005EA6)**, and **Public Bank Red (#ED1C24)**. This provides a more professional and recognizable CRM experience.
  - *Conflict 2: Dynamic Color Application:* Static container colors vs database-driven background. *Decision:* Updated `BankCardItem` (Screen) and `QuickCardDisplay` (Overlay) to read the `themeColor` hex string directly from the model. The card background and the Quick Wallet share button now dynamically adapt to the saved color, ensuring visual continuity.
- **Final Decision:** Use a 12-color authentic palette and wire `CardDefaults.cardColors(containerColor = Color(card.themeColor.toInt()))` across all display surfaces.
- **Impact:** `NewBankCardSheet.kt`, `DigitalWalletScreen.kt`, `QuickWalletOverlaySheet.kt`.

### 2024-05-20 - Premium Matte Glassmorphic Transformation
- **Context/Goal:** Elevate the wallet card design with a premium matte glassmorphic finish and sophisticated typography.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Background Rendering:* Solid colors looked too flat for a premium CRM. *Decision:* Implemented a matte tint glassmorphism (Acrylic) using `#1E1E1E` as the base and layering the bank color with 20% alpha.
  - *Conflict 2: Visual Depth:* How to make cards pop on pure black? *Decision:* Applied an "Ambient Neon Floating Glow" using a 2dp border with 30% alpha of the bank's theme color.
  - *Conflict 3: Share Button UX:* The cyan button clashed with glassmorphic cards. *Decision:* Redesigned the share button as a frosted semi-transparent white acrylic glass circle (15% alpha) with a thin white outline.
- **Final Decision:** Use layered background alpha blends and neon border glows. Applied sophisticated uppercase typography with expanded letter-spacing (1.5sp) for bank titles and grouped account number formatting.
- **Impact:** `DigitalWalletScreen.kt`, `QuickWalletOverlaySheet.kt`.

### 2024-05-20 - Quick Wallet Layout Alignment Fix
- **Context/Goal:** Resolve a layout bug where the share button was clipping over the card edge boundary in `QuickWalletOverlaySheet.kt`.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Shared Alignment:* The `Box` with `Alignment.CenterEnd` was pushing the button too far to the right, causing it to overlap the glassmorphic card border. *Decision:* Restructured the layout using a centered `Row` to keep the QR card and share button grouped relative to each other.
  - *Conflict 2: Button Scaling:* The 52dp button was too large for the available safe zone. *Decision:* Reduced the share button size to 44dp and the QR card size to 220dp to ensure a perfect fit without clipping the viewport or overlapping borders on standard devices.
- **Final Decision:** Use a centered `Row` with 16dp spacing and reduced component dimensions to maintain safe zone integrity.
- **Impact:** `QuickWalletOverlaySheet.kt`.

### 2024-05-20 - Live Preference Persistence & Sync Re-scheduling
- **Context/Goal:** Ensure Advanced Settings (Wi-Fi, Sync Frequency, Currency) are persisted to disk and actively trigger WorkManager re-scheduling.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Storage Strategy:* SharedPreferences vs Room. *Decision:* Implemented `PreferenceEntity` in Room to keep the data layer unified and support simple backup/restore of all user settings as per previous design decisions.
  - *Conflict 2: Reactive UI:* MutableStateFlow vs Repository Stream. *Decision:* ViewModel initializes `MutableStateFlow` from the database on startup and uses an immediate "Write-Through" pattern: UI changes trigger a background save to the database, ensuring choices are never lost upon navigation.
  - *Conflict 3: Sync Adaptability:* Manual worker cancellation vs Update Policy. *Decision:* Leveraged `ExistingPeriodicWorkPolicy.UPDATE` in `SyncManager.kt`. This allows WorkManager to dynamically adjust the interval and network constraints (NetworkType.UNMETERED for Wi-Fi only) without losing the existing execution state unless necessary.
- **Final Decision:** Use Room for settings persistence and `ExistingPeriodicWorkPolicy.UPDATE` for live constraint adaptation.
- **Impact:** `AdvancedSettingsViewModel.kt`, `PreferenceEntity.kt`, `PreferenceDao.kt`, `PreferenceRepository.kt`, `SyncManager.kt`, `AppDatabase.kt`, `UpperDotApp.kt`, `MainActivity.kt`.

### 2024-05-20 - Dynamic Currency Symbol Localization
- **Context/Goal:** Ensure the global currency setting (e.g., '$', 'RM', '€') is reflected in real-time across all UI elements, including metrics, ledger indicators, and form placeholders.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Shared Preference State:* Hardcoded '$' vs Live Repository Stream. *Decision:* Injected `PreferenceRepository` into `InsightsViewModel`, `ConnectionsListViewModel`, and `ClientProfileDetailViewModel`. Exposed a `currencySymbol` StateFlow that reactively updates the UI whenever the global setting is shifted.
  - *Conflict 2: Formatting Consistency:* How to handle signed ledger indicators? *Decision:* Updated `TransactionCard` (Insights) and `TransactionRow` (Profile) to utilize the dynamic symbol (e.g., `+$RM100.00` instead of `+$100.00`). Refactored `NewCashTransactionSheet` to include the active symbol in its amount field placeholder.
- **Final Decision:** Use repository-driven StateFlows to broadcast the active currency token to all financial display surfaces and input masks.
- **Impact:** `InsightsViewModel.kt`, `InsightsScreen.kt`, `ConnectionsListViewModel.kt`, `ConnectionsListScreen.kt`, `ClientProfileDetailViewModel.kt`, `ClientProfileDetailScreen.kt`, `NewCashTransactionSheet.kt`, `DetailViewerSheets.kt`, `MainActivity.kt`.

### ⚠️ Build Errors & Resolutions
- **Error:** `Unresolved reference: border` in `QuickWalletOverlaySheet.kt`.
- **Cause:** Missing import for `androidx.compose.foundation.border` after adding glassmorphic border glows.
- **Resolution:** Manually added the `border` import to the file.
