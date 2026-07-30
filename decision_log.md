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

### 2024-05-20 - Screen 04: Add Contact Form Wizard - Step 1: Core Info
- **Context/Goal:** First step of the multi-tab contact creation wizard focusing on basic identification.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Tab Navigation vs Sequential Buttons:* Should user use "Next" buttons or tabs? *Decision:* Both. Tabs (ScrollableTabRow) for direct jumping and a persistent Save/Next FAB as per Stitch design guidelines to ensure flexibility.
  - *Conflict 2: State Retention:* How to persist data across wizard steps? *Decision:* Single `AddContactViewModel` scoped to the navigation graph (using `hiltViewModel` or shared backstack entry in production, here simple shared VM instance) to retain state as user navigates between steps 1-4.
  - *Conflict 3: Validation Trigger:* When to validate "Required" fields? *Decision:* On-the-fly visual hints (asterisks) and hard blocking on Step 4 "Save" click, with auto-routing back to Step 1 if Full Name is empty.
- **Final Decision:** Implement `AddContactCoreInfoScreen`. Custom avatar picker UI with cyan ring. Use `OutlinedTextField` styled with Stitch tokens (#1E1E1E background, 16dp rounding).
- **Impact:** `AddContactCoreInfoScreen.kt`, `AddContactViewModel.kt`, `MainActivity.kt` routes.
