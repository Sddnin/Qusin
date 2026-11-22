# Qusin development roadmap

The development order is reliability first, then better task execution, then wider integrations. A feature is considered complete only after its behavior, recovery path, and user-visible status have been checked.

## Current foundations

- Android app control through a dedicated background display, Wireless debugging, and Accessibility.
- Built-in browser with persistent site sessions and real input events.
- Task execution, parallel sessions, interruption, progress records, and recovery notices.
- Scheduled and condition-based tasks with previews and execution history.
- Encrypted local storage for provider credentials, memories, plans, and workspaces.
- Model protocols for OpenAI-compatible Chat Completions, Anthropic Messages, Gemini `generateContent`, and OpenAI Responses.
- API key pools with manual key management, round-robin selection, `401/403` rejection handling, and `429` cooldown handling across supported chat, image, and speech adapters.
- Computer access through CLI/Skill and MCP.

## Next milestones

### 1. Verify reliability on real devices

- Run representative tasks on Android 12, 13, 14, and newer devices, including different vendors' background-display and Accessibility implementations.
- Record failures by category: screen observation, coordinate targeting, input delivery, app handoff, model response, and task completion detection.
- Add regression tests for each reproducible failure before changing task policy.
- Validate API key pools with multiple real provider keys, including mixed `401`, `403`, `429`, cooldown expiry, and the all-keys-unavailable case. Keep OAuth credentials separate from user-added API keys.

### 2. Mobile agent execution v2

- Add wait conditions for multi-step tasks so the agent waits for a meaningful screen or UI state instead of relying only on fixed delays.
- Prefer screen differences and targeted observations over repeatedly sending full-screen observations.
- Load mobile tools only when needed for the current task.
- Benchmark first-feedback time, model round trips, screen reads, actions, and task completion rate before and after each change.

### 3. Scheduled-task follow-up

- Expand condition monitoring for signed-in apps and account-specific pages, with explicit permission and recovery behavior.
- Support more robust multi-step scheduled workflows and clearly report partial completion.
- Improve quiet hours, upcoming-run previews, and handling of missed executions after device downtime.
- Suggest reusable plans only from persistent user intent and require the user to adopt or edit a suggestion before it becomes active.

### 4. More integrations

- Add communication channels beyond the current computer and Feishu integrations where there is a stable, permissioned interface.
- Expand MCP server/client transports and make connection status, credential scope, and disconnect behavior easier to inspect.
- Keep external tool identifiers and the native runner interface backward-compatible when internal implementation changes.

## Known boundaries

- Android secure windows and some DRM or custom-rendered screens cannot be observed through ordinary screenshots.
- Login, CAPTCHA, device unlock, and certain sensitive steps must remain user-controlled when the operating system or service requires it.
- The Linux sandbox runtime is currently ARM64-only.
- Model output and completion detection are probabilistic. A completed task record is not a guarantee that an external service performed an action exactly once.
- The final review/approval flow remains disabled in the current development configuration; this roadmap does not silently re-enable it.
