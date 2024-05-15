![Varilx](https://discordapp.com/api/guilds/886262410489520168/widget.png?style=shield) ![LatestCommit](https://img.shields.io/github/last-commit/Ayont/LPC-with-minimessage) ![Alldownloads](https://img.shields.io/github/downloads/ayont/LPC-with-minimessage/total
)


![LPC Chat Formatter Minimessage](https://github.com/Ayont/LPC-with-minimessage/assets/107298409/cbfc847a-a201-4092-9e52-a26c0ba6d2c3)


# LPC with Mini-message Support!

A chat formatting plugin for LuckPerms with Minimessage and other Features!
### The Legacy Version » https://github.com/wikmor/LPC (@wikmor)


## Config.yml
````yml
# LPC Configuration
# Please read the https://luckperms.net/wiki/Prefixes,-Suffixes-&-Meta before you set up.
#
# Placeholders:
# {message} - the chat message
# {name} - the player's name
# {displayname} - the player's display name / nickname
# {world} - the world name of the player's current world
# {prefix} - the player's highest priority prefix
# {suffix} - the player's highest priority suffix
# {prefixes} - the player's prefixes sorted by the highest priority
# {suffixes} - the player's suffixes sorted by the highest priority
# {username-color} - the player's or the group's username color
# {message-color} - the player's or the group's message color

# WARNING: Prefixes, Suffixes, message-color etc. needs to be Minimessage Colorcodes and not Legacy!

# To reload the configuration, run '/lpc reload' command. Make sure you have the 'lpc.reload' permission assigned.
# More information can be found at the Github Wiki.
chat-format: "{prefix}{name}<dark_gray> »<reset> {message}"

# Set the format per group.
# Note: Option for more advanced users. Remove comments to run.
group-formats:
#  default: "[default] {name}<dark_gray> »<reset>  {message}"
#  admin: "[admin] {name}<dark_gray> »<reset>  {message}"
````

## Wiki to the Placeholders ->
> {message}: The chat message

> {name}: The player's name

> {displayname}: The player's display name / nickname
> {world}: The world name of the player's current world

> {prefix}: The player's highest priority prefix

> {suffix}: The player's highest priority suffix

> {prefixes}: The player's prefixes sorted by the highest priority

> {suffixes}: The player's suffixes sorted by the highest priority

> {username-color}: The player's or the group's username color

> {message-color}: The player's or the group's message color

# Preview:

### Chat Format
![grafik](https://github.com/Ayont/LPC-with-minimessage/assets/107298409/f8394ef3-286a-41f9-a86a-a88874ad1f76)

### [ITEM] Placeholder
![grafik](https://github.com/Ayont/LPC-with-minimessage/assets/107298409/ab779f59-f2d1-4b41-8996-5d6df52f4ee0)


This plugin is not affiliated with LuckPerms. Please do not ask its author for help with this plugin.​
