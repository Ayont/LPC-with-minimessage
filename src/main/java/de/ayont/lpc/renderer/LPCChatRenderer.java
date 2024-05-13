package de.ayont.lpc.renderer;

import de.ayont.lpc.LPC;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LPCChatRenderer implements ChatRenderer {

    private final LuckPerms luckPerms;
    private final LPC plugin;
    private final MiniMessage miniMessage;


    public LPCChatRenderer(LPC plugin) {
        this.luckPerms = LuckPermsProvider.get();
        this.plugin = plugin;
        this.miniMessage = MiniMessage.builder().build();

    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(source);
        final String group = metaData.getPrimaryGroup();


        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);

        String formatKey = plugin.getConfig().getString("group-formats." + group);
        String format = plugin.getConfig().getString(formatKey != null ? formatKey : "chat-format");
        if (format != null) {
            format = format.replace("&", "§")
                    .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                    .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                    .replace("{prefixes}", String.join(" ", metaData.getPrefixes().values()))
                    .replace("{suffixes}", String.join(" ", metaData.getSuffixes().values()))
                    .replace("{world}", source.getWorld().getName())
                    .replace("{name}", source.getName())
                    .replace("{displayname}", source.getDisplayName())
                    .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                    .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "")
                    .replace("{message}", plainMessage);

        }


        if (containsPlayerName(plainMessage)) {
            List<String> playerNames = getPlayerNamesFromMessage(plainMessage);
            for (String playerName : playerNames) {
                if (!playerName.contentEquals(source.getName()))
                    format = format.replace(playerName, "%playertag_" + playerName + "%");
            }
        }

        return miniMessage.deserialize(format);
    }

    public List<String> getPlayerNamesFromMessage(String message) {
        List<String> names = new ArrayList<>();
        String[] splitted = message.split(" ");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(Arrays.stream(splitted).anyMatch(string -> player.getName().contentEquals(string)))
                names.add(player.getName());
        }
        return names;
    }

    private boolean containsPlayerName(String message) {
        String[] splitted = message.split(" ");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(Arrays.stream(splitted).anyMatch(string -> player.getName().contentEquals(string)))
                return true;
        }
        return false;
    }
}
