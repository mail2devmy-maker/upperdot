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

### 2024-05-20 - ViewModel Integration: Wallet & Settings
- **Context/Goal:** Connect Digital Wallet and Advanced Settings to persistence and tiered limit logic.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Wallet Limit Enforcement:* ViewModel vs Repository check. *Decision:* ViewModel performs the count check against the user's tier status retrieved from the `PreferenceRepository` (or mocked Premium state) before allowing a navigation to "Add Card", ensuring robust freemium guardrails.
  - *Conflict 2: DB Size Calculation:* How to calculate "Vault Size"? *Decision:* Use `context.getDatabasePath().length()` to get raw file size on disk, providing an accurate metric of local storage usage as seen in the diagnostics panel.
  - *Conflict 3: Preference Persistence:* Room vs DataStore. *Decision:* Use Room for basic app preferences (Currency, Sync Frequency) within a `PreferenceEntity` to keep the data layer unified and support simple backup/restore of all user settings.
- **Final Decision:** Implement `BankCardEntity` and its repository. Wire `DigitalWalletViewModel` for live card stream and `AdvancedSettingsViewModel` for diagnostic and preference management.
- **Impact:** `BankCardEntity.kt`, `BankCardRepository.kt`, `DigitalWalletViewModel.kt`, `AdvancedSettingsViewModel.kt`, `MainActivity.kt`.

---
