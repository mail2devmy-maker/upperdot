# UpperDot - Master Software Requirements Specification (SRS)

## 1. Project Specifications

* **Package Name:** `com.mail2dev.upperdot`
* **Architecture:** Clean Architecture + MVVM (Data, Domain, Presentation layers)
* **UI System:** Jetpack Compose + Material 3 (Stitch Design System layout engine)
* **Local DB:** Room Database (Single Source of Truth). Attachments, receipt photos, and **voice recordings** are stored as separate files in the app's private storage, with file paths persisted in Room.
* **Cloud Storage:** Google Drive REST API (Uses `appDataFolder` hidden directory ONLY via `DriveScopes.DRIVE_APPDATA`). Local-first architecture: saves are committed to Room DB immediately; sync (including attachments and voice logs) occurs in background when internet is available. No multi-device conflict resolution needed as the app is single-instance mobile-only.
* **Monetization:** RevenueCat Android SDK (Freemium / Premium configuration).
    * **Free Tier:** Unlimited contacts and cloud sync. Limited to 20 Relationship Notes and 20 Cash Transactions total.
    * **Premium Tier:** Unlimited notes, transactions, and digital wallet cards.
* **Privacy Rules:** Local-First design. Zero custom backend servers or third-party analytics trackers. User data is backed up to their own Google account for maximum trust and security.

---

## 2. Design System Tokens (Stitch Dark Theme)

* **Theme Variant**: Pure Dark Mode (High Contrast)
* **Background Color**: `#121212` (Pure Black for contrast)
* **Surface Color**: `#1E1E1E` (Dark Grey for cards/containers)
* **Primary Accent Color**: `#FFD54F` (UpperDot Yellow)
* **Accent Highlight / Cyan**: `#00C8FF` (Used for icons, active states, and buttons)
* **Positive/Revenue Color**: `#4CAF50` (Green)
* **Negative/Expense Color**: `#F44336` (Red)
* **Layout Tokens**: Heavy corner rounding (24dp for large containers, 16dp for input fields). Icon-label pairing with cyan icons.

---

## 3. Screen Layout Specifications

### Screen 01: Authentication Launchpad (Splash / Login)

* **Visual Layout Structure**:
  * Centrally aligned branding container set against background color (`#121212`).
  * **App Title text**: `"UpperDot"` scaled using extra-large display font in Primary Yellow (`#FFD54F`).
  * **Subtitle text**: `"Secure Business Card CRM"` styled in muted font-body variant.
  * **Sign-In Control**: A large rounded white button featuring colored Google "G" logo asset and text: `"Sign in with Google"`.
  * **Guest Action Link**: Text link labeled `"Try as Guest"` in Primary Yellow color styling.
  * **Legal Disclaimers**: Fine-print text block located at bottom layout anchor referencing standard Terms of Service and Privacy Policy.

* **Functional Jetpack Compose Component Structure**:
  * `Column` container with full screen size constraints, centered horizontally via `Alignment.CenterHorizontally`.
  * `Spacer` modules managing vertical positioning ratios.
  * `Button` component configured with custom white surface color modifier.
  * `TextButton` for Guest Mode.

* **Navigation & State Flow Logic**:
  * **Action: Click "Sign in with Google"**
    * Triggers Google Sign-In intent flow using local client credentials.
    * Requests OAuth scope: `DriveScopes.DRIVE_APPDATA`.
    * On success ➔ Transition root navigation pointer to primary dashboard (`connections_list`).
  * **Action: Click "Try as Guest"**
    * Displays warning dialog: "This mode will not save to the cloud. No sync will occur. Data is local-only and will be lost if the app is closed."
    * Behaves like a **Premium User** for the duration of the session.
    * On click ➔ Transition root navigation pointer to primary dashboard (`connections_list`).

---

### Screen 02: Call History Screen (Folder: call_history)

* **Visual Layout Structure**:
  * **Screen Header**: Left-aligned headline `"Call History"` with history clock icon to its left.
  * **Empty State Card Layout**: Central dark rounded surface container (`#1E1E1E`) displaying:
    * Circular layout badge holding phone icon in Accent color (`#00C8FF`).
    * Main text: `"No Cellular Records"`.
    * Instruction text: `"Ensure Call Log permissions are enabled in your device settings to sync telephony data."` (muted helper text).
  * **Bottom Navigation Dock**: Persistent menu featuring 4 items (`Call Logs`, `Contact`, `Insight`, `My Profile`). The far-left item `"Call Logs"` is highlighted using a pill-shaped primary accent background capsule.

* **Functional Jetpack Compose Component Structure**:
  * `Scaffold` component managing persistent structural layers.
  * `BottomAppBar` containing 4 discrete `NavigationBarItem` targets.
  * `Card` variant matching `#1E1E1E` surface container guidelines.
  * Runtime permission state container managing empty view swapping dynamically.

* **Navigation & State Flow Logic**:
  * **System Action: On Screen Entry Lifecycle**
    * Checks `READ_CALL_LOG` permission.
    * *Permission missing* ➔ Displays centered placeholder card.
    * *Permission granted* ➔ Room DB pulls call log data into view and hides placeholder graphics.
  * **Action: Click Unknown Number Row** ➔ Displays `"Add to Connections"` shortcut ➔ Navigates to **Add Contact Wizard** with phone number pre-filled.
  * **Action: Click Known Contact Row** ➔ Navigates to the contact's **Client Profile** (`client_profile`).
  * **Action: Click "Contact" Tab Item** ➔ Navigates to **Contact List** (`connections_list`).
  * **Action: Click "Insight" Tab Item** ➔ Navigates to **Insights Dashboard** (`insights_notes`).
  * **Action: Click "My Profile" Tab Item** ➔ Navigates to **User Profile Settings** (`my_profile`).

---

### Screen 03: Connections List (Folder: connections_list)

* **Visual Layout Structure**:
  * **App Bar Layer**: Left-aligned headline `"Connections"` with multi-person icon.
  * **Search Bar**: Full-width dark rounded text field (`#1E1E1E`) + placeholder `"Search by name or number..."`.
  * **Filter Row**: Horizontal row of capsule buttons. Default `"All"` active in cyan (`#00C8FF`).
  * **Empty State Card**: Central dark rounded surface container (`#1E1E1E`) displaying:
    * Silhouette icon badge in Accent color (`#00C8FF`).
    * Main text: `"No All Contacts"`.
    * Instruction text: `"Your directory is currently empty. Start building your secure network by adding new profile keys."`
  * **Action FAB**: Cyan (`#00C8FF`) circular button with `+` icon at bottom right.
  * **Bottom Navigation Dock**: Dock item `"Contact"` active.

* **Functional Jetpack Compose Component Structure**:
  * `LazyColumn` for contact feed.
  * `TextField` with custom shape tokens mapping to `#1E1E1E`.
  * `FloatingActionButton` for adding new contacts.

* **Navigation & State Flow Logic**:
  * **Action: Click `+` FAB** ➔ Navigates to **Add Contact Wizard** (`add_contact_core_info`).
  * **Action: Click Contact Card** ➔ Navigates to **Client Profile** (`client_profile`).
  * **Action: Swipe Right on Contact Card** ➔ Triggers `Intent.ACTION_DIAL` for the contact's **primary phone number**. A **phone icon appears behind the card** during the swipe gesture. If no number exists, the swipe action is disabled/ignored.
  * **Action: Long Press (Hold Tap) on Contact Card** ➔ Expands the card in-place to reveal two quick-action buttons: `"Add New Note"` and `"Add New Transaction"`. Clicking these launches their respective bottom sheets with the contact pre-selected.
  * **Action: Type in Search Bar** ➔ Filters contacts in real-time via Room DB. **Search scope includes Name, Nicknames (all comma-separated values), and partial phone number matching.**

---

### Screen 04: Add Contact Form Wizard - Step 1: Core Info (Folder: add_contact_core_info)

* **Visual Layout Structure**:
  * **App Bar Layer**: Centered header `"ADD CONTACT"` in all-caps bold. Left-aligned `"X"` close icon asset.
  * **Tab Segment Row**: Horizontal scrollable tab selection system (`Core Info`, `Identity`, `Corporate`, `Financial`). The `"Core Info"` tab option is highlighted with filled primary accent color (`#00C8FF`) and black text.
  * **Avatar Image Picker**: Centered circle layout holding profile camera plus icon encircled by accent stroke border (`#00C8FF`).
  * **Form Input Cards**: Three rounded dark surface text boxes (`#1E1E1E`):
    * Field 1: Silhouette icon prefix + placeholder: `"Full Name (Required)"`.
    * Field 2: Tag icon prefix + placeholder: `"Nicknames (Comma Separated)"`.
    * Field 3: Handset icon prefix + placeholder: `"Primary Phone Number"`.
  * **Dynamic Command**: Clickable line text: `"[ + Add Another Number ]"` in cyan (`#00C8FF`).
  * **Persistent Action**: Floating Action Button (FAB) at bottom-right holding save floppy-disk storage icon bounded by circular accent outline.

* **Functional Jetpack Compose Component Structure**:
  * `ScrollableTabRow` with modular clickable `Button` capsule wrappers tracking active selection indexes.
  * `OutlinedTextField` / `BasicTextField` mapped to `#1E1E1E` background shape token.
  * `FloatingActionButton` handling validation and saving routines.

* **Navigation & State Flow Logic**:
  * **Action: Click "X" Close Icon** ➔ Displays `"Discard Changes?"` confirmation dialog ➔ On confirm, discards inputs ➔ Pops back to `connections_list`.
  * **Action: Tap Tab Targets** ➔ Persists active inputs to ViewModel state ➔ Transitions wizard position index (Step 1 -> 2 -> 3 -> 4).
  * **Action: Click Avatar Circle** ➔ Triggers `PickVisualMedia` contract.
  * **Action: Click Save Disk FAB**
    * Validates `Full Name` non-empty.
    * *If valid* ➔ Commits entity to Room DB ➔ Triggers background Drive sync ➔ Navigates to `connections_list`.
    * *If invalid* ➔ Highlights `Full Name` text field with error state.

---

### Screen 05: Add Contact Form Wizard - Step 2: Identity (Folder: add_contact_identity)

* **Visual Layout Structure**:
  * **App Bar Layer**: Centered header `"ADD CONTACT"`. Left-aligned `"X"` close icon asset.
  * **Tab Segment Row**: `"Identity"` tab highlighted in cyan (`#00C8FF`).
  * **Primary Input**: Envelope icon + placeholder: `"Email Address"`.
  * **Social Profiles Section**: Section header `"SOCIAL PROFILES"` in cyan lettering (`#00C8FF`). Split field row with dropdown paired with URL/Handle input text box.
    * **Platform Options**: Facebook, Instagram, X, TikTok, YouTube, Shopee, Lazada, Custom (manual text entry).
  * **Dynamic Command**: Clickable line: `"[ + Add Social Profile ]"` in cyan (`#00C8FF`).
  * **Relationship Group Section**: Section header `"RELATIONSHIP GROUP"`. Text box with `#` prefix + placeholder: `"Custom Sub-tag (Optional)"`.
  * **Persistent Action**: Bottom-right Save FAB.

* **Navigation & State Flow Logic**:
  * **Action: Click "X" Close Icon** ➔ Discards inputs ➔ Navigates back to `connections_list`.
  * **Action: Tap Tab Targets** ➔ Saves state ➔ Switches wizard view.
  * **Action: Click Save Disk FAB** ➔ Cross-validates Step 1 required fields ➔ Commits to Room DB ➔ Navigates to `connections_list`.

---

### Screen 06: Add Contact Form Wizard - Step 3: Corporate Info (Folder: add_contact_corporate)

* **Visual Layout Structure**:
  * **App Bar Layer**: Centered header `"ADD CONTACT"`. Left-aligned `"X"` close icon asset.
  * **Tab Segment Row**: `"Corporate"` tab highlighted in cyan (`#00C8FF`).
  * **Form Input Cards**: Three rounded dark surface text boxes (`#1E1E1E`):
    * Field 1: Building icon + placeholder: `"Company Name"`.
    * Field 2 (Dropdown): Floating label `"Business Category"`, shape icon prefix, active default `"General"`, right-aligned clear `"X"` icon.
    * Field 3: Location pin icon + placeholder: `"Physical Office Address"`.
  * **Persistent Action**: Bottom-right Save FAB.

* **Navigation & State Flow Logic**:
  * **Action: Click "X" Close Icon** ➔ Discards inputs ➔ Navigates back to `connections_list`.
  * **Action: Tap Tab Targets** ➔ Saves state ➔ Switches wizard view.
  * **Action: Click Save Disk FAB** ➔ Cross-validates Step 1 required fields ➔ Commits to Room DB ➔ Navigates to `connections_list`.

---

### Screen 07: Add Contact Form Wizard - Step 4: Financial Info (Folder: add_contact_financial)

* **Visual Layout Structure**:
  * **App Bar Layer**: Centered header `"ADD CONTACT"`. Left-aligned `"X"` close icon asset.
  * **Tab Segment Row**: `"Financial"` tab highlighted in cyan (`#00C8FF`).
  * **Section Header**: `"BANK VAULT"` in cyan lettering (`#00C8FF`).
  * **Secure Account Card**: Bordered surface container (`#1E1E1E`):
    * Tag label `"SECURE ACCOUNT"`.
    * Institution dropdown displaying `"Maybank"`.
    * Field 1: Person icon + placeholder: `"Account Holder Name"`.
    * Field 2: Hash symbol icon + placeholder: `"Account Number"`.
  * **Dynamic Command**: Clickable line: `"[ + Add Bank Account ]"` in cyan (`#00C8FF`).
  * **Persistent Action**: Bottom-right Save FAB.

* **Navigation & State Flow Logic**:
  * **Action: Click Save Disk FAB**
    * Cross-validates Step 1 (`Full Name` non-empty).
    * *If valid* ➔ Bundles all wizard steps into parent entity ➔ Commits to Room DB ➔ Navigates to `connections_list`.

---

### Screen 08: Client Profile Detail View (Folder: client_profile)

* **Visual Layout Structure**:
  * **App Bar Layer**: Left back navigation arrow + title `"Client Profile"`. Right-aligned action pair: **Red Trash Icon** (Delete) and **Cyan Pencil Icon** (Edit).
  * **Profile Identity Header**:
    * Large centered circle avatar holding initial encircled by cyan ring border (`#00C8FF`).
    * Full name text title and secondary nickname text in muted parenthesis.
  * **Relationship Group Subsection**: Label `"Assign Relationship Group:"` in muted text, acting as header for tag pills.
    * **Display**: Small pills showing `group_name` and `tag_name` associated with the contact.
  * **Information Cards (`#1E1E1E`)**:
    * **CONTACT INFO Card**: Phone row and Email row with cyan icons.
    * **BUSINESS INFO Card**: Category row and Address row with cyan icons.
    * **BANK INFO Card**: Bank row and Account Name row with cyan icons.
    * **SOCIAL PROFILES Card**: Linked chat platform rows.
  * **Dynamic Accordions**:
    * **RELATIONSHIP NOTES (Count)**: Collapsible section with cyan square icon.
    * **TRANSACTION LEDGER (Count)**: Collapsible section with cyan square icon.

* **Functional Jetpack Compose Component Structure**:
  * `LazyColumn` managing the entire scrollable profile content.
  * `TopAppBar` for navigation and edit triggers.
  * Collapsible headers using `remember { mutableStateOf(true) }`.

* **Navigation & State Flow Logic**:
  * **Action: Click Back Arrow** ➔ Returns to `connections_list`.
  * **Action: Click Red Trash Icon** ➔ Displays confirmation dialog `"Are you sure you want to delete this contact? This action cannot be undone."` ➔ On confirm, deletes contact and all associated notes/transactions from Room and Drive ➔ Returns to `connections_list`.
  * **Action: Click Edit Pencil** ➔ Navigates to **Add Contact Wizard** in editing mode.
* **Navigation & State Flow Logic**:
  * **Action: Tap Accordion Headers** ➔ Toggles section collapse/expansion.
  * **Action: Long Press / Click Empty Note Accordion** ➔ Opens `new_relationship_note` bottom sheet; **Contact is auto-selected** based on the current profile being viewed.
  * **Action: Long Press / Click Empty Ledger Accordion** ➔ Opens `new_cash_transaction` bottom sheet; **Contact is auto-selected** based on the current profile being viewed.

---

### Screen 09: Insights Tab - Notes Stream (Folder: insights_notes)

* **Visual Layout Structure**:
  * **Screen Header**: `"Insights"` headline with chart icon prefix in cyan (`#00C8FF`).
  * **Search Bar**: Dark rounded text field (`#1E1E1E`) + placeholder `"Search notes..."`.
  * **Filter Capsule**: Outline button displaying `"All Contacts"`.
  * **Sub-Tab Switcher**: Dark capsule with `"NOTES"` active (cyan fill `#00C8FF`, black text) and `"TRANSACTIONS"` inactive.
  * **Feed Container**: Scrollable cards displaying logged relationship notes with linked contact tag badge.
  * **Action Command**: Bottom-right `+` FAB with cyan outline.

* **Navigation & State Flow Logic**:
  * **Action: Type in Search Bar** ➔ Filters Room DB transaction queries in real time. **Search scope includes Transaction Title, Detail/Notes, and Linked Contact Name.**
  * **Action: Click Contact Name on Card** ➔ Sets filter to that specific contact; UI updates to show only transactions for that contact.
  * **Action: Click "NOTES" Sub-Tab** ➔ Switches view to `insights_notes`.
  * **Action: Click `+` FAB** ➔ Launches `new_cash_transaction` bottom sheet. **Sheet must open fully expanded to show all fields without scrolling.**
  * **Action: Click Transaction Card** ➔ Toggles expanded state to show full transaction content.
  * **Action: Click Date Capsules ("From", "To")** ➔ Triggers `DatePickerDialog`.
  * **Paywall Action**: If free user attempts to add > 20 transactions ➔ Automatically navigates to **Plans Screen** (`my_profile` tier section).
  * **Bottom Navigation Dock**: Dock item `"Insight"` active.

* **Navigation & State Flow Logic**:
  * **Action: Type in Search Bar** ➔ Filters Room DB note queries in real time. **Search scope includes Note Title, Content, and Linked Contact Name.**
  * **Action: Click Contact Name on Card** ➔ Sets filter to that specific contact; UI updates to show only notes for that contact.
  * **Action: Click "TRANSACTIONS" Sub-Tab** ➔ Switches view layer to `insights_transactions`.
  * **Action: Click `+` FAB** ➔ Displays `new_relationship_note` bottom sheet overlay. **Sheet must open fully expanded (skip partially expanded state) to show all fields without scrolling.**
  * **Action: Click Note Card** ➔ Toggles expanded state to show full note content.
  * **Action: Tap Attachment in expanded card** ➔ Opens full-screen media viewer.
  * **Paywall Action**: If free user attempts to add > 20 notes ➔ Automatically navigates to **Plans Screen** (`my_profile` tier section).

---

### Screen 10: Create Note Bottom Sheet Overlay (Folder: new_relationship_note)

* **Visual Layout Structure**:
  * **Bottom Sheet Container (`#1E1E1E`)**:
    * Drag handle + title `"New Relationship Note"`.
    * **Contact Selector Box**: Dropdown title `"Select Contact (Mandatory)"` in cyan (`#00C8FF`).
    * Input fields for Title, Content, and Attachment upload square.
    * **Voice Entry**: Microphone icon for voice recording.
  * **Execution Button**: Full-width button `"Save Relationship Note"`.

* **Functional Logic**:
  * **Action: Hold Microphone** ➔ Records audio.
  * **Action: Release/Tap Stop** ➔ Saves recording to local storage.
  * **Action: Tap Trash Icon on Recording** ➔ Deletes current voice file before saving note.

---

### Screen 11: Insights Tab - Transactions Ledger (Folder: insights_transactions)

* **Visual Layout Structure**:
  * **Screen Header**: `"Insights"` headline with chart icon prefix in cyan (`#00C8FF`).
  * **Search Bar**: Dark rounded text field (`#1E1E1E`) + placeholder `"Search transactions..."`.
  * **Filter Capsules**: `"All Contacts"`, `"From"`, `"To (Present)"` (cyan border `#00C8FF`).
  * **Metrics Grid**: Summary block (Revenue: Green, Expenses: Red, Net: Cyan).
  * **Sub-Tab Switcher**: `"TRANSACTIONS"` active (cyan fill `#00C8FF`).
  * **Action Command**: Bottom-right `+` FAB with cyan outline.

* **Navigation & State Flow Logic**:
  * **Action: Type in Search Bar** ➔ Filters Room DB transaction queries in real time. **Search scope includes Transaction Title, Detail/Notes, and Linked Contact Name.**
  * **Action: Click Contact Name on Card** ➔ Sets filter to that specific contact; UI updates to show only transactions for that contact.
  * **Action: Click "NOTES" Sub-Tab** ➔ Switches view to `insights_notes`.
  * **Action: Click `+` FAB** ➔ Launches `new_cash_transaction` bottom sheet. **Sheet must open fully expanded to show all fields without scrolling.**
  * **Action: Click Transaction Card** ➔ Toggles expanded state to show full transaction content.
  * **Action: Click Date Capsules ("From", "To")** ➔ Triggers `DatePickerDialog`.
  * **Paywall Action**: If free user attempts to add > 20 transactions ➔ Automatically navigates to **Plans Screen** (`my_profile` tier section).

---

### Screen 12: New Cash Transaction Bottom Sheet Overlay (Folder: new_cash_transaction)

* **Visual Layout Structure**:
  * **Bottom Sheet Container (`#1E1E1E`)**:
    * Drag handle + title `"New Cash Transaction"`.
    * **Transaction Type Switcher**: Split button (`REVENUE` green vs `EXPENSE` transparent).
    * Split Fields for Title (0.6) and Amount (0.4).
    * **Voice Entry**: Microphone icon for voice recording notes.
  * **Execution Button**: Full-width button `"Finalize Transaction"`.

* **Functional Logic**:
  * **Action: Hold Microphone** ➔ Records audio memo for transaction.
  * **Action: Tap Stop/Release** ➔ Finalizes audio file.
  * **Action: Delete Icon** ➔ Discards recording.

---

### Screen 13: My Profile Settings Tab (Folder: my_profile)

* **Visual Layout Structure**:
  * **Screen Header**: Left headline `"My Profile"` with silhouette icon in cyan (`#00C8FF`).
  * **User Account Summary (`#1E1E1E`)**:
    * Avatar, Name, Email, Premium tag.
    * Sync status banner: Cloud icon, `"Last Sync: ..."` text, and manual refresh icon.
    * **Sign Out Button**: Small red-outlined pill button labeled `"Sign Out"` positioned below the sync banner (matching the size of the Premium tag).
    * Metrics summary bar (Contacts, Notes, Trans).
  * **Section Header**: `"WORKSPACE CONTROLS DECK"`.
  * **Navigation Menu Deck (`#1E1E1E`)**:
    * Menu items for Wallet, Groups, Advanced Settings, and Plans.
  * **Action FAB**: Cyan (`#00C8FF`) circular button with **Wallet icon** at bottom right (above bottom nav) to trigger Quick Wallet.
  * **Bottom Navigation Dock**: Dock item `"My Profile"` active.

* **Navigation & State Flow Logic**:
  * **Action: Click Wallet FAB** ➔ Opens **Quick Wallet Overlay** (Screen 18).
  * **Action: Click "Sign Out" Button** ➔ Clears local session tokens ➔ Invalidates Drive session ➔ Routes to Screen 01 (`Authentication Launchpad`).
  * **Action: Click "Free vs Premium Plan"** ➔ Opens Upgrade/Purchase screen featuring a `"Purchase Premium"` call-to-action.
  * **Action: Click Menu Items** ➔ Navigates to respective management screens.

---

### Screen 14: Digital Wallet Management (Folder: digital_wallet_management)

* **Visual Layout Structure**:
  * **App Bar Layer**: Left back arrow + title `"Digital Wallet Management"`.
  * **Tier Notification Banner (`#1E1E1E`)**: Info badge in cyan (`#00C8FF`).
  * **Stored Card Records Deck**: Vertical list of linked cards with edit (cyan) and delete (red) icons.
  * **Action Command**: Extended FAB `"+ Add New Card"` in cyan (`#00C8FF`).

---

### Screen 15: Add New Card Bottom Sheet Overlay (Folder: new_bank_card)

* **Visual Layout Structure**:
  * **Bottom Sheet Container (`#1E1E1E`)**:
    * Headline `"New Bank Card"`.
    * **Theme Color Picker**: Row of 6 color circles (Cyan active `#00C8FF`, Blue, Teal, Purple, Green, Red).
    * Form fields for Bank details and QR attachment button.

---

### Screen 16: Relationship Hierarchy Manager (Folder: manage_custom_groups)

* **Visual Layout Structure**:
  * **App Bar Layer**: Left back arrow + title `"Relationship Hierarchy"`.
  * **Hierarchical Node List (`#1E1E1E`)**: Scrollable cards with expand chevron, group name, count pill, cyan `+` icon, edit pencil, and red trash icon.

* **Functional Jetpack Compose Component Structure**:
  * `LazyColumn` managing nested relational groups.
  * Tree node subcomponents with mutable expansion boolean states (`var isOpen by remember { mutableStateOf(false) }`).

* **Navigation & State Flow Logic**:
  * **Action: Click Back Arrow** ➔ Returns to `my_profile`.
  * **Action: Click Group Chevron** ➔ Smoothly expands/collapses nested tag items.
  * **Action: Click Cyan Plus Circle (+)** ➔ Displays `AlertDialog` to append a new sub-tag string to parent node.
  * **Action: Click Trash Can Icon** ➔ Displays confirmation dialog showing **how many contacts are related to that group** ➔ On confirm, removes tag mapping from Room DB (contacts move to "Unassigned").
  * **Action: Rename Group** ➔ Updates group name in Room DB; all associated contacts are automatically updated to reflect the new group name.

---

### Screen 17: Advanced App Settings (Folder: advanced_app_settings)

* **Visual Layout Structure**:
  * **App Bar Layer**: Left back arrow + title `"Advanced App Settings"`.
  * **Configuration Groups**:
    * **Storage & Data Management** (Purple header text):
      * **Clear Local Cache**: Triggers warning dialog. Only deletes temporary thumbnails. No primary data or offline attachments are affected.
      * **Export Database Backup**: Serialized JSON snapshot (Encrypted with Google account context).
      * **Import Database Restore**: Overwrite local data with backup.
      * **Import VCF Contacts**: Supports Google Contacts .vcf format.
        * **Conflict Resolution**: User choice (Overwrite, Skip, or Duplicate).
        * **Data Integrity**: During "Overwrite", all existing linked Notes and Transactions are preserved and remain attached to the updated contact record.
        * **Smart Number Matcher**: Compares numbers by stripping symbols (`+`, `-`, spaces) and country codes to accurately identify duplicates across different formats.
      * **Media Compression**: Locked to **Medium** to optimize storage.
    * **Sync & Connectivity**:
      * **Sync Over Wi-Fi Only**: Toggle switch.
      * **Sync Frequency**: User Choice (1h, 6h, 12h, 24h, or Manual).
    * **Localization & Preferences**:
      * **Currency Selection**: Global symbol picker (Impacts display only, no conversion).
    * **Database Diagnostics Info**:
      * Read-only metrics: Total Database Size (DB), Total Number of Attachments, Last Successful Sync Timestamp, and Sync Error Logs.

---

### Screen 18: Quick Wallet Overlay (Folder: quick_wallet_overlay)

* **Visual Layout Structure**:
  * **Bottom Sheet Container (`#1E1E1E`)**:
    * Header: Wallet icon + title `"Quick Wallet"`.
    * **Card Display (Horizontal Pager)**: Bank name, holder name, white QR card template, cyan share button.
      * **QR Interaction**: Tapping the QR image expands it to full screen and increases device brightness for easy scanning.
    * **Clipboard Box**: Capsule pill with copy icon + account number.

* **Navigation & State Flow Logic**:
  * **Action: Swipe Left / Right on Card Area** ➔ Updates pager index state to navigate saved payment cards. **For Free users, swipes to a "Premium Only" upgrade prompt after the first card which navigates to "Free vs Premium Plan" screen.**
  * **Action: Click Top Gear Icon** ➔ Dismisses overlay ➔ Navigates to `digital_wallet_management`.
  * **Action: Click Account Number Capsule** ➔ Copies to clipboard.

## 4. Automated Developer Workflow and Version Control Rules

You have permission to use the integrated terminal to manage version control and architectural logging. For every screen you build from Screen 01 to Screen 18, you must execute this strict sequence:

1. Before writing any code, open decision_log.md at the project root and append a new log entry tracking the UI layout conflicts, design decisions, and state alternatives considered for the target screen.
2. Develop the necessary Composable functions using Jetpack Compose and establish corresponding MVVM ViewModel architectures following your specific Stitch Dark Theme parameters.
3. Once the screen features are ready and fully compileable, execute an automated git commit from the terminal using the clear description format feat(ui): implement Screen XX - Screen Name layout.
4. Ensure git status remains entirely clean before requesting user confirmation to proceed onto the subsequent screen setup.

5. **Error Resolution Tracking Rule:** If your code introduces a compilation error or build failure, you must document it in `decision_log.md` under a subsection called "### ⚠️ Build Errors & Resolutions". State what caused the error (e.g., duplicate overloads, missing imports) and exactly how you refactored the code to fix it before proceeding.
---

## 5. Local Storage Engineering Rules (Room Database)

You are now entering the Data Layer implementation phase. You must build out our local-first single source of truth database framework using Android Room Components.

### Architectural Rules
1. **Entities & Schemas:** Create decoupled `@Entity` classes for Contacts, Notes, Transactions, and BankCards matching your Clean Architecture Data Layer parameters.
2. **Type Converters:** Implement `@TypeConverter` functions for complex data elements (e.g., custom lists, object mappings, or UUID/LocalDateTime parsing to strings).
3. **Data Access Objects (DAOs):** All DAO queries must return asynchronous streams using Kotlin Coroutines `Flow` for reactive, real-time UI updates across our screens.
4. **File Storage Paths:** For attachments, photos, and voice recordings, do not store raw binary blobs inside Room. Store the raw binaries inside the app's internal private storage cache directory, and save the absolute file path strings as text inside the database table columns.

### Execution Sequence per Entity
For every database entity module you construct, you must adhere to the execution loop:
1. Log table design conflicts, indexing optimization decisions, and cascade delete options inside `decision_log.md` first.
2. Develop the Entity, DAO interfaces, and testing schemas.
3. Hook the raw Repository layer implementations directly up into our pre-existing screen ViewModels.
4. Execute an automated git commit with the message format: `feat(data): implement Room [Module Name] database schema and DAO`.
