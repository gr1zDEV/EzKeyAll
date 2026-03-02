# EzKeyAll

EzKeyAll is a Paper/Folia plugin for **Minecraft 1.21.11** that awards one weighted reward command on a timer.

## Features
- Supports latest Paper + latest Folia for 1.21.11.
- Server-wide timer mode or per-player timer mode.
- Weighted random rewards from `config.yml`.
- Message output types: ACTIONBAR, CHAT (with optional clickable command), TITLE.
- Color formatting parser supports both `&#RRGGBB` and legacy `&` codes.
- Persists timer runtime state to `data.yml` every 60 seconds and on disable.
- Folia-safe timer execution via `GlobalRegionScheduler`.

## Installation
1. Build with Maven: `mvn -B clean package`.
2. Place the generated jar from `target/` into your server `plugins/` folder.
3. Start/restart server.

## Commands
- `/ezkeyall reload` - Reloads `config.yml` and reloads timer state from `data.yml` (does not force a fresh timer reset).
- `/ezkeyall reset` - Resets active timer(s) to `timer` in config.
- `/ezkeyall time` - Shows remaining time (server-wide or player-specific mode).

## Permissions
- `ezkeyall.admin` (default: op): access to `/ezkeyall` admin commands.
- `ezkeyall.bypass` (default: false): bypasses rewards and timer participation.

## Configuration
`src/main/resources/config.yml` is shipped exactly as requested, including:
- `timer`, `server-wide`
- `messages`, `title-settings`, `chat-settings`, `actionbar-settings`
- `click-command`, `click-command-message`
- weighted `rewards`

### Actionbar behavior
If multiple actionbar lines are configured, they are joined as `line1 | line2` and sent as one actionbar message.

## Data persistence
Runtime state is stored in `plugins/EzKeyAll/data.yml`:
- `serverTimer: <int>`
- `players.<uuid>.timerRemaining` when per-player mode is used.

## Build target
- Java 21
- Maven
- Paper API: `io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`

## GitHub release workflow
Workflow file: `.github/workflows/release.yml`

Triggers:
- `release` event (`types: [created]`)
- tag push matching `v*`

Behavior:
- checks out code
- sets up Java 21 with Maven cache
- runs `mvn -B clean package`
- uploads jar as workflow artifact
- uploads jar to GitHub Release assets with `softprops/action-gh-release`
  - on tag pushes without an existing release, workflow creates/updates a release (`draft: false`, `prerelease: false`) and attaches jar.
