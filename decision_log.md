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

### 2024-05-20 - Screen 05: Add Contact Form Wizard - Step 2: Identity
- **Context/Goal:** Second step of the wizard focusing on digital identifiers and relationship grouping.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Social Profile State:* How to manage a dynamic list of social platforms? *Decision:* Use a data class `SocialProfile` with platform and handle fields. ViewModel manages a `List<SocialProfile>` to allow multiple entries.
  - *Conflict 2: Dropdown UI:* Standard Material dropdown vs custom Stitch capsule. *Decision:* Custom `ExposedDropdownMenuBox` styled to match our `StitchTextField` rounded surface tokens for visual consistency.
  - *Conflict 3: Field Grouping:* How to separate Email, Social, and Tags? *Decision:* Use clear Section Headers in AccentCyan with vertical spacing, maintaining the "Stitch" vertical form flow.
- **Final Decision:** Implement `AddContactIdentityScreen`. Maintain shared VM state. Utilize `PrimaryYellow` for the "X" Close and `AccentCyan` for active tab/headers.
- **Impact:** `AddContactIdentityScreen.kt`, `AddContactViewModel.kt` (updated with setters), `MainActivity.kt` routes.

### ⚠️ Build Errors & Resolutions
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

### 2024-05-20 - Cloud Sync: Background Synchronization Engine
- **Context/Goal:** Implementation of background sync using WorkManager to push local records and binary assets to Google Drive.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Sync Triggering:* Immediate vs Scheduled. *Decision:* Both. `PeriodicWorkRequest` (default 1h-24h as per settings) for background consistency, and immediate `OneTimeWorkRequest` triggered upon manual refresh or significant data saves.
  - *Conflict 2: Network Constraints:* Metered vs Wi-Fi. *Decision:* Wi-Fi gating is dynamically applied to the `WorkRequest` constraints based on the `Sync Over Wi-Fi Only` setting in the Advanced App Settings.
  - *Conflict 3: Payload Structure:* Full DB vs Incremental. *Decision:* Full JSON serialization of non-synced Room records for metadata. Binary assets (attachments/voice) are pushed as independent file streams with unique identifiers to minimize payload size during retries.
- **Final Decision:** Implement `DriveSyncWorker` using `CoroutineWorker`. Use exponential backoff for retries. Status updates are broadcast via `WorkInfo` observed in the Profile ViewModel.
- **Impact:** `DriveSyncWorker.kt`, `AdvancedSettingsViewModel.kt` (sync scheduling), `MainActivity.kt`.

---
