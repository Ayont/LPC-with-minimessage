package de.ayont.lpc.chat;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Pattern;

/**
 * Replaces the {@code [item]} token in a rendered chat line with the item the player is holding.
 * On Paper the item is shown with a hover tooltip; the {@code withHover} flag lets the legacy
 * Spigot path skip the hover (which cannot survive legacy serialization).
 */
public final class ItemPlaceholder {

    private static final Pattern ITEM_PATTERN = Pattern.compile("\\[item]", Pattern.CASE_INSENSITIVE);

    private ItemPlaceholder() {
    }

    /**
     * Applies the {@code [item]} replacement when enabled in config and permitted, otherwise
     * returns the rendered component unchanged.
     *
     * @param plugin    the plugin (for config + platform detection + legacy serializer)
     * @param player    the chatting player
     * @param rendered  the already-rendered chat component
     * @param withHover whether to attach the item hover tooltip (Paper only)
     * @return the component with {@code [item]} resolved, or the original component
     */
    public static Component apply(LPC plugin, Player player, Component rendered, boolean withHover) {
        if (!plugin.getConfig().getBoolean("use-item-placeholder", false)
                || !player.hasPermission("lpc.itemplaceholder")) {
            return rendered;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            return rendered;
        }

        Component display = displayName(plugin, item);
        if (withHover) {
            display = display.hoverEvent(item);
        }
        Component replacement = display;

        return rendered.replaceText(TextReplacementConfig.builder()
                .match(ITEM_PATTERN)
                .replacement(replacement)
                .build());
    }

    @SuppressWarnings("deprecation") // getDisplayName() is the only option on legacy Spigot
    private static Component displayName(LPC plugin, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            if (plugin.isPaper()) {
                Component name = meta.displayName();
                if (name != null) {
                    // An item renamed via an anvil (or another plugin) is player-influenced input —
                    // harden it so a crafted item name can never carry a click/hover/insertion event
                    // into the [item] tooltip.
                    return ComponentSanitizer.stripInteractive(name);
                }
            } else {
                return LPC.getLegacySerializer().deserialize(meta.getDisplayName());
            }
        }
        return Component.text(item.getType().toString().toLowerCase().replace("_", " "));
    }
}
