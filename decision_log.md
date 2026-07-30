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

### 2024-05-20 - Screen 02: Call History Screen
- **Context/Goal:** Implementation of call history feed with permission-gated empty state and persistent bottom navigation.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Permission Management:* Should permission handling be in the ViewModel or UI? *Decision:* ViewModel tracks the permission state boolean, UI triggers the request and displays the empty state card based on the boolean to keep logic testable.
  - *Conflict 2: Bottom Navigation Implementation:* Using standard `NavigationBar` vs custom Stitch-rounded dock. *Decision:* Custom `BottomAppBar` with pill-shaped selection indicator matching Screen 02 visual specs.
  - *Conflict 3: List vs Card:* Displaying call records as a list. *Decision:* Each call record will be a row within a scrollable list, following the "Icon-Label pairing" pattern seen in profile images.
- **Final Decision:** Implement `CallHistoryScreen` with a `Scaffold` to host the `BottomAppBar`. Permission state determines whether to show the `EmptyCallHistoryCard` or the `CallLogList`.
- **Impact:** `CallHistoryScreen.kt`, `CallHistoryViewModel.kt`, `MainActivity.kt` nav graph update.
