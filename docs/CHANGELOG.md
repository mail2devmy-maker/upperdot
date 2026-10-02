# UpperDot Project Changelog

## [Milestone 1.10] - Visual Hierarchy & Component Affordance Polish
* **Status:** Completed & Verified
* **Goal:** De-clutter the contact list by muting badges and hiding default "Unassigned" tags, and redesign the BottomSheet tag picker into interactive selection chips to eliminate TextField confusion.

### Changes Summary:
* `ConnectionsListScreen.kt`: Muted contact card group/tag badge styling (11.sp, normal weight, `Color(0xFF1F2937)` background, `TextSecondary` text) and hid badges when group/tag is "Unassigned" or ID is "una".
* `AddContactScreen.kt`: Redesigned `GroupTagPickerBottomSheetContent` child tag items from rectangular full-width boxes into a horizontal `FlowRow` of compact selection chips/pills with checkmark icons (`✓ #tagname`) for selected items.

## [Milestone 1.09] - Two-Tier Filter Navigation & Bottom-Sheet Metadata Selection
* **Status:** Completed & Verified
* **Goal:** Enhance contact discovery on the main list with a two-tier filter bar and tag badges, and replace rigid dropdowns with a unified bottom-sheet picker on the Add/Edit Contact screen.

### Changes Summary:
* `ConnectionsListViewModel.kt`: Updated filtering pipeline to manage dual `selectedGroupId` and `selectedTagId` state and expose dynamic child tag flows.
* `ConnectionsListScreen.kt`: Implemented two-tier scrollable filter bar (Group pills on top, Tag pills on secondary row) and added inline group (`[group]`) and tag (`#tag`) badges to contact cards.
* `AddContactViewModel.kt` & `ClientProfileDetailViewModel.kt`: Added atomic `onGroupAndTagSelected` handler to update group and tag assignments in one step.
* `AddContactScreen.kt`: Replaced standard dropdowns with a "Relationship Group & Tag" selection tile and built a `ModalBottomSheet` displaying an expandable group tree with indented child tags for single-tap assignment.

## [Milestone 1.08] - Database Normalization for Groups & Tags (ID-Based Schema Migration)
* **Status:** Completed & Verified
* **Goal:** Refactor `ContactEntity` to store normalized foreign key references (`groupId: String?`, `tagId: String?`) instead of raw string names, ensuring instant, bug-free group and tag renames across all contacts.

### Changes Summary:
* `ContactEntity.kt`: Replaced string fields `groupName` and `tagName` with `groupId: String? = "una"` and `tagId: String? = null`.
* `AppDatabase.kt`: Incremented Room database version to `2` and defined `MIGRATION_1_2` to migrate existing contact data from group/tag names to `GroupEntity` and `TagEntity` IDs.
* `UpperDotApp.kt`: Registered `MIGRATION_1_2` in Room database builder.
* `ContactDao.kt`: Updated `getFavoriteContacts()` to query `groupId = 'fav'`, removed manual string replace queries, added `resetGroupIdForContacts` / `resetTagIdForContacts`, and updated `searchContacts` with `LEFT JOIN` on `contact_groups` and `contact_tags`.
* `HierarchyRepository.kt`: Simplified group/tag renames (no contact updates needed) and updated delete methods to call ID-based contact resets.
* `SearchUtils.kt`: Updated `extractSearchMatches` signature to accept `groupName` and `tagName` strings for search snippet highlighting.
* `ConnectionsListViewModel.kt`: Dynamically resolved group and tag display names from `HierarchyRepository` and updated search/filter pipelines.
* `ClientProfileDetailViewModel.kt`: Dynamically resolved group/tag display names and updated `toggleFavorite()` to switch `groupId` between `"fav"` and `"una"`.
* `MainActivity.kt`: Provided `hierarchyRepository` to `ClientProfileDetailViewModel` factory.
* `RelationshipHierarchyViewModel.kt`: Updated contact count calculation to match `it.groupId == group.id`.
* `AddContactViewModel.kt` & `AddContactScreen.kt`: Bound form selection state to `groupId` and `tagId` while resolving display names for dropdown UI.
* `DatabaseBackup.kt`: Updated backup schema version to `2`.

## [Milestone 1.07] - Persistent Custom Contact Groups & Tags Architecture
* **Status:** Completed & Verified
* **Goal:** Replace in-memory/hardcoded contact group and tag defaults with persistent Room DB storage (`GroupEntity`, `TagEntity`), dynamic seeding, cascading contact updates, and Drive backup integration.

### Changes Summary:
* `GroupEntity.kt`: Created Room `@Entity(tableName = "contact_groups")` for persistent group storage.
* `TagEntity.kt`: Created Room `@Entity(tableName = "contact_tags")` for persistent tag storage.
* `GroupWithTags.kt`: Created relational model mapping `GroupEntity` to `@Relation` child `TagEntity` list.
* `GroupDao.kt`: Implemented DAO with `@Transaction` reactive flow queries, CRUD functions, and seed operations.
* `AppDatabase.kt`: Registered `GroupEntity` and `TagEntity` in database schema and exposed `groupDao()`.
* `ContactDao.kt`: Added batch update queries `updateGroupName`, `resetGroupForContacts`, and `resetTagForContacts`.
* `DatabaseBackup.kt`: Included `groups` and `tags` lists in backup model for local and Google Drive sync.
* `HierarchyRepository.kt`: Wired with `GroupDao` and `ContactDao` to perform dynamic database seeding on empty tables and execute cascading updates for contact group/tag changes.
* `UpperDotApp.kt`: Initialized `HierarchyRepository` lazy singleton with `database.groupDao()` and `database.contactDao()`.
* `DriveSyncWorker.kt` & `DataVaultViewModel.kt`: Extended backup export/import to serialize and restore groups and tags.
* `ConnectionsListViewModel.kt` & `ConnectionsListScreen.kt`: Replaced hardcoded filter tabs with dynamic flow `availableFilters` from `HierarchyRepository`.
* `RelationshipHierarchyViewModel.kt`: Wrapped group/tag mutations in `viewModelScope.launch(Dispatchers.IO)`.

## [Milestone 1.06] - Obsidian-Style Search Highlights & Text Snippets
* **Status:** Completed & Verified
* **Goal:** Implement field-level search result highlights and text snippets in ConnectionsScreen, mirroring Obsidian's search UX.

### Changes Summary:
* `SearchMatch.kt`: Created domain models `SearchMatch` and `ContactSearchResult`.
* `SearchUtils.kt`: Implemented `extractSearchMatches` helper to scan contact fields with a ~30-character text window and index ranges.
* `SearchUtilsTest.kt`: Added automated unit tests covering keyword extraction, snippet truncation, and range calculation.
* `ConnectionsListViewModel.kt`: Updated `ContactSummary` and `uiState` search pipeline to map matches when search query is active.
* `ConnectionsListScreen.kt`: Rendered child snippet sub-card beneath contact name with `AnnotatedString` yellow keyword highlights and `AccentCyan` field labels.

## [Milestone 1.05] - Deep Contact Search Expansion
* **Status:** Completed & Verified
* **Goal:** Expand contact search capabilities to look deeply into remarks, emails, company info, bank accounts, social profiles, and associated notes/transactions.

### Changes Summary:
* `ContactDao.kt`: Updated `searchContacts` with `LEFT JOIN` on `notes` and `transactions` tables to search across all contact fields (remark, emails, corporate info, bank accounts, social profiles) and associated notes/transactions.
* `NoteDao.kt`: Updated `searchNotesWithContact` and `searchNotes` queries to match contact remark and company name.
* `TransactionDao.kt`: Updated `searchTransactionsWithContact` and `searchTransactions` queries to match contact remark and company name.

## [Milestone 1.00] - Adapt to New AI System
* **Status:** Completed & Verified
* **Goal:** Adapted project documentation, architecture baseline, and AI agent protocol for UpperDot.

### Changes Summary:
* `docs/ARCHITECTURE.md`: Updated project metadata, package mapping, dependencies, telecom integration, and theme tokens to match UpperDot (`com.mail2dev.upperdot`).
* `docs/SYSTEM_PROMPT.md`: Updated master AI rules and namespace references to `com.mail2dev.upperdot`.
* `docs/CHANGELOG.md`: Initialized UpperDot project changelog at Milestone 1.00.
