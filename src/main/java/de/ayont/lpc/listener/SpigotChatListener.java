package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import de.ayont.lpc.renderer.SpigotChatRenderer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;


public class SpigotChatListener implements Listener {
    private final LPC plugin;
    private final SpigotChatRenderer chatRenderer;
    private final MiniMessage miniMessage;
    private final Map<String, String> legacyToMiniMessageColors;

    public SpigotChatListener(LPC plugin) {
        this.plugin = plugin;
        this.chatRenderer = new SpigotChatRenderer(plugin);
        this.miniMessage = MiniMessage.miniMessage();
        this.legacyToMiniMessageColors = new HashMap<>();
        initColorMappings();
    }

    private void initColorMappings() {
        legacyToMiniMessageColors.put("&0", "<black>");
        legacyToMiniMessageColors.put("&1", "<dark_blue>");
        legacyToMiniMessageColors.put("&2", "<dark_green>");
        legacyToMiniMessageColors.put("&3", "<dark_aqua>");
        legacyToMiniMessageColors.put("&4", "<dark_red>");
        legacyToMiniMessageColors.put("&5", "<dark_purple>");
        legacyToMiniMessageColors.put("&6", "<gold>");
        legacyToMiniMessageColors.put("&7", "<gray>");
        legacyToMiniMessageColors.put("&8", "<dark_gray>");
        legacyToMiniMessageColors.put("&9", "<blue>");
        legacyToMiniMessageColors.put("&a", "<green>");
        legacyToMiniMessageColors.put("&b", "<aqua>");
        legacyToMiniMessageColors.put("&c", "<red>");
        legacyToMiniMessageColors.put("&d", "<light_purple>");
        legacyToMiniMessageColors.put("&e", "<yellow>");
        legacyToMiniMessageColors.put("&f", "<white>");
        legacyToMiniMessageColors.put("&l", "<bold>");
        legacyToMiniMessageColors.put("&o", "<italic>");
        legacyToMiniMessageColors.put("&n", "<underlined>");
        legacyToMiniMessageColors.put("&m", "<strikethrough>");
        legacyToMiniMessageColors.put("&k", "<obfuscated>");
        legacyToMiniMessageColors.put("&r", "<reset>");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        if (event.getPlayer().hasPermission("lpc.chatcolor")) {
            message = message.replaceAll("§", "&");
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getValue(), entry.getKey());
            }
        }

        if (plugin.getConfig().getBoolean("use-item-placeholder", false) && event.getPlayer().hasPermission("lpc.itemplaceholder")) {
            final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (!item.getType().equals(Material.AIR)) {
                String itemName = item.getType().toString().toLowerCase().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    StringBuilder hoverText = new StringBuilder();

                    if (meta.hasDisplayName()) {
                        try {
                            Component displayName = meta.displayName();
                            if (displayName != null) {
                                itemName = MiniMessage.miniMessage().serialize(displayName);
                            }
                        } catch (NoSuchMethodError e) {
                            String displayName = meta.getDisplayName();
                            itemName = MiniMessage.miniMessage().serialize(
                                    LegacyComponentSerializer.builder()
                                            .useUnusualXRepeatedCharacterHexFormat()
                                            .hexColors()
                                            .character('§')
                                            .build()
                                            .deserialize(displayName)
                            );
                        }
                    }

                    if (meta.hasLore()) {
                        try {
                            java.util.List<Component> lore = meta.lore();
                            if (lore != null) {
                                for (Component line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(line));
                                }
                            }
                        } catch (NoSuchMethodError e) {
                            java.util.List<String> lore = meta.getLore();
                            if (lore != null) {
                                for (String line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(
                                            LegacyComponentSerializer.builder()
                                                    .useUnusualXRepeatedCharacterHexFormat()
                                                    .hexColors()
                                                    .character('§')
                                                    .build()
                                                    .deserialize(line)
                                    ));
                                }
                            }
                        }
                    }

                    itemName = "<hover:show_text:'" + itemName + hoverText.toString() + "'>" + itemName + "</hover>";
                }
                message = message.replaceFirst("(?i)\\[item]", itemName);
            }
        }

        event.setFormat(LegacyComponentSerializer.builder()
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build()
                .serialize(chatRenderer.render(event.getPlayer(), message)));
    }
}