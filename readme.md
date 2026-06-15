![LPC Banner](https://cdn.varilx.de/raw/fwtRZS.png)

<p align="center">
  <a href="https://modrinth.com/plugin/lpc-chat">
    <img src="https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg" alt="Available on Modrinth" />
  </a>
</p>

<p align="center">
  <a href="https://discord.gg/ZPyb9g6Gs4">
    <img src="https://img.shields.io/discord/1322873747535040512" alt="Discord">
  </a>
  <a href="https://github.com/Ayont/LPC-with-minimessage/actions/workflows/publish.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Ayont/LPC-with-minimessage/publish.yml" alt="Build Status">
  </a>
  <a href="https://github.com/Ayont/LPC-with-minimessage/releases">
    <img src="https://img.shields.io/github/v/release/Ayont/LPC-with-minimessage" alt="Latest Release">
  </a>
</p>

# LPC – LuckPerms Chat Formatter ✨
**A flexible chat formatting plugin with MiniMessage support for LuckPerms**

> Modern chat formatting powered by [MiniMessage](https://docs.advntr.dev/minimessage/format.html), full LuckPerms metadata support, group/track formats, and PlaceholderAPI.

---

## 🧩 Compatibility

| | |
|---|---|
| **Minecraft** | 26.1.2 |
| **Server** | Paper (recommended) or Spigot |
| **Java** | 25+ |

---

## 🔧 Requirements

- [LuckPerms](https://luckperms.net/) *(Required)* – Permissions plugin
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) *(Optional)* – Additional placeholders

---

## ✅ Features

**Formatting**
- Full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) support, with group- and track-specific formats
- Optional PlaceholderAPI integration and `[item]` placeholder (hover tooltip on Paper)
- Per-rank message styling and per-rank **gradient names** (`{gradient-name}`)
- Per-world toggle via `disabled-worlds`

**Social** *(on by default)*
- **@Mention pings** – highlight online names + sound/action-bar ping (Paper)
- **Emoji shortcuts** – e.g. `:heart:` → ❤ (fully configurable)
- **Clickable links** – URLs become `openUrl` links on Paper, coloured on Spigot

**Moderation** *(off by default)*
- Anti-spam cooldown, repeated-message blocker, caps filter, profanity mask, anti-advertising
- Per-player **mute** (`/lpc mute`) + LuckPerms mute node for punishment plugins

**Server messages** *(off by default)*
- MiniMessage join / quit / first-join / death messages

**Quality of life**
- `/lpc reload · version · help · mute · unmute` with tab completion
- Built-in Modrinth update checker
- **Safe by design** – player messages can never inject `click`/`hover`/command components
- Works on both Paper (Adventure rendering) and Spigot (legacy fallback)

---

## ⌨️ Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/lpc reload` | `lpc.reload` | Reload the configuration |
| `/lpc version` | – | Show the installed version |
| `/lpc help` | – | List available commands |
| `/lpc mute <player> [duration]` | `lpc.mute` | Mute a player (e.g. `10m`, `2h`; omit for permanent) |
| `/lpc unmute <player>` | `lpc.mute` | Unmute a player |

---

## 🧑‍💼 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `lpc.reload` | op | Reload the configuration |
| `lpc.chatcolor` | false | Use colour codes & cosmetic MiniMessage tags in chat |
| `lpc.itemplaceholder` | false | Use the `[item]` placeholder in chat |
| `lpc.update` | op | Receive an update notification on join |
| `lpc.emoji` | true | Use emoji shortcuts (only enforced if `emoji.require-permission`) |
| `lpc.chatlinks` | true | Have URLs turned into clickable links |
| `lpc.mention.exempt` | false | Opt out of receiving mention pings |
| `lpc.mute` | op | Use `/lpc mute` and `/lpc unmute` |
| `lpc.muted` | false | Marks a player as muted (usually set by a punishment plugin) |
| `lpc.bypass.spam` / `.repeat` / `.caps` / `.profanity` / `.advert` | op | Bypass the matching moderation filter |

> ℹ️ Even with `lpc.chatcolor`, only **cosmetic** tags (colours, decorations, and optionally gradients/rainbow) are honoured in player messages. Interactive tags (`click`, `hover`, `insertion`, …) are always stripped.

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Main chat format (MiniMessage!)
chat-format: "{prefix}{name}<dark_gray> »<reset> {message}"

# Per-group formats (optional)
group-formats:
#  default: "<gray>[User]</gray> {name}<dark_gray> »<reset> {message}"
#  admin: "<red>[Admin]</red> {name}<dark_gray> »<reset> {message}"

# Per-track formats (optional) – groups take priority over tracks
track-formats:
#  staff_track: "<gold>[Staff]</gold> {name}<dark_gray> »<reset> {message}"

# Enable the [item] placeholder
use-item-placeholder: true

# Allow <gradient> / <rainbow> for players with lpc.chatcolor
allow-gradient-tags: true

# Worlds where LPC does NOT format chat
disabled-worlds: []

# Check Modrinth for updates on startup
update-checker: true

# Reload message
reload-message: "<green>Reloaded LPC configuration!"
```

---

## 🪄 Available Placeholders

| Placeholder | Description |
|-------------|-------------|
| `{message}` | The chat message (inserted safely, never re-parsed) |
| `{name}` | Player's name |
| `{displayname}` | Display name / nickname |
| `{world}` | Player's current world |
| `{prefix}` / `{suffix}` | Highest priority prefix / suffix |
| `{prefixes}` / `{suffixes}` | All prefixes / suffixes, highest priority first |
| `{username-color}` / `{message-color}` | Colour values from LuckPerms meta |

> ℹ️ All colour values (prefix, suffix, etc.) must be in **MiniMessage** format – no legacy codes (`&a`, `§b`).

---

## 🚀 Installation

1. Stop your server
2. Drop `LPC-<version>.jar` into your `/plugins` folder
3. Start the server to generate `config.yml`
4. Edit the config to your liking
5. Run `/lpc reload` to apply changes ✅

---

## 🛠️ Building

```bash
./gradlew shadowJar
# output: build/libs/LPC-<version>.jar
```

Requires JDK 25.

---

## 📌 Notes

- **Not affiliated with LuckPerms** – please do not contact the LuckPerms author for support.
- Legacy version available at: [GitHub Legacy LPC](https://github.com/wikmor/LPC)
