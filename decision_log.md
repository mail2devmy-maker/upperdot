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
