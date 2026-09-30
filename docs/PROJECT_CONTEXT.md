# UpperDot — Product Overview & Local Context

## 1. Product Description
**UpperDot** (`com.mail2dev.upperdot`) is an offline-first Android client relationship management, telephony security, and digital vault application designed to track high-value contacts, relationship notes, hierarchy connections, call logs/whitelists, financial transactions, and secure Google Drive data synchronization.

## 2. Core Feature Pillars
* **Client Relationship & Connections Management:** Manage client contacts, relationship notes, connection hierarchies, and profile details.
* **Telecom Integration & Call Security:** Native Android Telecom integration featuring `InCallService` overlay screens, `CallScreeningService` whitelist management, and call history logging.
* **Digital Wallet & Data Vault:** Vault management for bank cards, transaction logs, saved banks, and cash transactions.
* **Insights & Analytics:** Visualization and analytics dashboard for relationship activities and transaction metrics.
* **Offline-First Storage & Google Drive Sync:** Local Room database with cloud backup and sync via `GoogleDriveService` and WorkManager (`DriveSyncWorker`).

## 3. Local Environment & Sandbox
* **Active Working Directory:** `C:\DockerShared\Project\UpperDot\`
* **Master Source Backup:** `C:\109Backup\Project\UpperDot\`
* **Local Homelab Server:** `192.168.0.10` (Gitea / Syncthing / FileBrowser / SMB)
* **Execution Stack:** Android Studio AI Agent / Gemini CLI / Antigravity CLI (`agy`) + Wireless ADB debugging on a physical Android device.

## 4. Documentation Mapping
* `docs/SYSTEM_PROMPT.md`: Core AI execution rules and handoff instructions.
* `docs/PROJECT_CONTEXT.md`: High-level app purpose and local setup.
* `docs/ARCHITECTURE.md`: Tech stack specs, package map, and coding standards.
* `docs/CURRENT_TASK.md`: Active task sprint details and immediate goals.
* `docs/CHANGELOG.md`: Historical record of completed milestones.
