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
The plugin is configured through `plugins/EzKeyAll/config.yml` (default template in `src/main/resources/config.yml`).

### Top-level settings

#### `timer`
- Type: integer (seconds)
- Default: `3600`
- Purpose: defines how long each cycle lasts before a reward is rolled.
- Safety: values below `1` are clamped to `1` internally.

#### `server-wide`
- Type: boolean
- Default: `true`
- `true`: one global timer for the entire server; when it finishes, all eligible online players can receive a reward roll.
- `false`: each player has their own timer tracked independently.

#### `sound-on-reward`
- Type: Bukkit `Sound` enum name
- Default: `ENTITY_PLAYER_LEVELUP`
- Set to a valid sound constant to play a sound when reward feedback is sent.
- If blank/invalid, no sound is played.

### Message channel configuration

#### `messages`
- Type: list of strings
- Supported values: `ACTIONBAR`, `CHAT`, `TITLE`
- The plugin loops through this list in order and sends each enabled message format.
- Unknown entries are ignored safely.

Example:
```yml
messages:
  - ACTIONBAR
  - CHAT
```

#### `actionbar-settings.message`
- Type: list of strings
- Each line supports placeholders and color codes.
- If you provide multiple lines, they are joined as `line1 | line2` and sent as a single actionbar message.

#### `chat-settings.message`
- Type: list of strings
- Each line is sent as its own chat message.
- Supports placeholders, colors, and clickable token insertion via `[click-command-message]`.

#### `title-settings`
- `fade-in`, `stay`, `fade-out`: integer ticks (20 ticks = 1 second)
- `title-message`, `subtitle-message`: strings with placeholders/colors

### Click command configuration

#### `click-command`
- Type: string command (example: `/spawn`)
- If non-empty, the clickable chat token will run this command when clicked.

#### `click-command-message`
- Type: display text string
- Inserted where `[click-command-message]` appears in chat lines.
- If `click-command` is set, this inserted text is clickable.

### Rewards block

`rewards` contains numbered (or otherwise unique) entries. Each reward entry has:

- `command`: console command to execute for that winner
- `chance`: numeric weight used for weighted random selection
- `key-name`: display name used in message placeholders

Example:
```yml
rewards:
  '1':
    command: dc givekey %player% common 1
    chance: 80.0
    key-name: '&#B9F2FFCommon Key'
```

Notes:
- `chance` is a weight, not a strict percent requirement. Higher values are picked more often relative to other entries.
- `%player%` is passed through to your command as written; this is commonly consumed by the command/plugin executing that command.

## Placeholder reference

EzKeyAll supports a small, explicit placeholder/token set in its own message pipeline.

### `{key-name}`
- Replaced with the selected reward's `key-name`.
- Available in actionbar, chat, title, and subtitle message strings.

### `[click-command-message]`
- Chat-only token.
- Replaced with the configured `click-command-message` text.
- When `click-command` is configured, the replaced text becomes clickable and runs that command.

### `%player%`
- Not replaced by EzKeyAll message code.
- Intended for reward commands (inside `rewards.*.command`) so downstream command handlers can target the player.

## Color formatting

All message strings support:
- Legacy color/format codes like `&7`, `&l`, etc.
- Hex format using `&#RRGGBB`.

Hex codes are converted internally to Adventure-compatible format before being sent.

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
