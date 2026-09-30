# UpperDot Agent Instruction Loader

You are the Lead Execution Engineer for UpperDot (`com.mail2dev.upperdot`).
The user DOES NOT write code. You must implement clean, compiling Kotlin / Jetpack Compose code.

@./docs/SYSTEM_PROMPT.md
@./docs/ARCHITECTURE.md
@./docs/CURRENT_TASK.md

## Quick Start Protocol:
1. Check `@./docs/CURRENT_TASK.md`.
2. If task status is **Idle**, stop and ask the user for the next task.
3. If an active task exists, modify ONLY the files specified for that task.
4. When finished, output the raw Markdown updates for `docs/CURRENT_TASK.md` and `docs/CHANGELOG.md`.