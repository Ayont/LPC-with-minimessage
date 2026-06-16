# Changelog

## 4.1.0

**Released:** 2026-06-16  
**Minecraft:** 26.1.2 · **Java:** 25 · **API version:** 26.1.2

Native Folia support with region-aware scheduling while retaining full Paper/Spigot compatibility.

### Added

- `folia-supported: true` in `plugin.yml`.
- Cross-platform scheduler abstraction in `de.ayont.lpc.scheduler`:
  - `Scheduler` interface with `run`, `runAsync`, `runDelayed` and `runOnEntity`.
  - `PaperSchedulerImpl` using `GlobalRegionScheduler`, `AsyncScheduler` and `EntityScheduler`.
  - `BukkitSchedulerImpl` fallback for plain Spigot/Bukkit.
  - Runtime platform detection via `Schedulers.create(...)`.
- Folia detection in `LPC` with dedicated log output.
- `onDisable` cancels all scheduled tasks cleanly.

### Changed

- `UpdateChecker` now schedules its network request via `plugin.getScheduler().runAsync(...)`.
- `MentionService.pingAll` now schedules per-target pings via `plugin.getScheduler().runOnEntity(...)`.

### Compatibility

- Verified on Folia 26.1.2 build 8.
- Paper/Spigot fallback remains intact.

## 4.0.0

**Released:** 2026-06-15  
**Minecraft:** 26.1.2 · **Java:** 25 · **API version:** 26.1.2

Major rewrite release with a new MiniMessage chat formatter, an integrated moderation toolkit, and a much more flexible configuration.

### Added

- **New MiniMessage chat formatter** with a clear separation between trusted format strings and player messages.
- **Group & track formats** per LuckPerms group or track (`group-formats`, `track-formats`).
- **`[item]` placeholder** – shows the held item in chat (hover tooltip on Paper).
- **Placeholder support:** `{prefix}`, `{suffix}`, `{prefixes}`, `{suffixes}`, `{world}`, `{displayname}`, `{username-color}`, `{message-color}`, and `{gradient-name}`.
- **Optional PlaceholderAPI integration** for formats.
- **@Mentions** with highlighting, sound + action-bar ping on Paper (Spigot gets highlighting only).
- **Emoji replacement** via configurable shortcuts like `:heart:` → ❤.
- **Clickable links** in chat (`openUrl`, clickable on Paper, colored/underlined on Spigot).
- **Per-rank message styles** – groups/tracks can get their own message coloring without needing MiniMessage permissions.
- **Gradient names** via LuckPerms meta or per group (`{gradient-name}`).
- **Join / quit / first-join / death messages** in MiniMessage format, individually toggleable.
- **Integrated moderation toolkit** (all toggleable):
  - Anti-spam cooldown
  - Repeat-message filter
  - Caps filter (BLOCK or LOWERCASE)
  - Profanity filter (BLOCK or MASK)
  - Anti-advertising / anti-IP (BLOCK or REDACT)
- **Mute system** with `/lpc mute <player> [duration]` and `/lpc unmute <player>`, plus support for external mute permission nodes.
- **Modrinth update checker** with in-game notification for players with `lpc.update`.
- **Paper, Folia & Spigot compatibility** with automatic fallback to legacy rendering and region-aware scheduling on Folia.
- **Unit tests** for emoji replacement, legacy colors, mentions, player messages, URL linkification, moderation text checks, and version comparison.

### Changed

- Complete rewrite of chat rendering: player messages are inserted as pre-built components and are never re-parsed.
- Interactive tags (`click`, `hover`, `insertion`, …) in player messages are always stripped.
- Default config bumped to `config-version: 3`.

### Security

- Player messages cannot inject MiniMessage tags or PlaceholderAPI placeholders.
- Prefix/suffix values and format strings are parsed in a trusted context.
- Gradient specifications are validated against an allowed character set.

### Permissions

- `lpc.reload`, `lpc.chatcolor`, `lpc.itemplaceholder`, `lpc.update`
- `lpc.emoji`, `lpc.chatlinks`, `lpc.mention.exempt`
- `lpc.mute`, `lpc.muted`
- `lpc.bypass.spam`, `lpc.bypass.repeat`, `lpc.bypass.caps`, `lpc.bypass.profanity`, `lpc.bypass.advert`

### Known Limitations

- Legacy color codes (`&a`, `§b`) in prefixes/suffixes are not supported – use MiniMessage.
