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

### 2024-05-20 - Screen 03: Connections List
- **Context/Goal:** Primary dashboard for managing contacts with search, filtering, and quick actions.
- **Conflicts & Alternatives Considered:**
  - *Conflict 1: Search Implementation:* Real-time Room filtering vs button-triggered search. *Decision:* Real-time filtering in ViewModel using a `StateFlow` and `debounce` for performance, matching the responsive feel of a modern CRM.
  - *Conflict 2: Card Interaction:* Handling swipe-to-dial and long-press expansion. *Decision:* Custom `SwipeToDismissBox` for the dial action (with phone icon background) and an `AnimatedVisibility` wrapper within each list item for the long-press expansion.
  - *Conflict 3: List State Logic:* Displaying "No Contacts" vs "No Search Results". *Decision:* ViewModel will provide a sealed `UIState` (Loading, Empty, SearchEmpty, Success) to handle dynamic list overlays cleanly.
- **Final Decision:** Implement `ConnectionsListScreen` using a `Scaffold`. Search and filters are anchored at the top below the header. The contact list uses `LazyColumn` with heavy rounding tokens (24dp) for cards.
- **Impact:** `ConnectionsListScreen.kt`, `ConnectionsListViewModel.kt`, `MainActivity.kt` nav graph update.
