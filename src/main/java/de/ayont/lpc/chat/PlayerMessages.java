package de.ayont.lpc.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

/**
 * Builds safe {@link Component}s from raw player chat input.
 *
 * <p>The key security property: a player's message is never deserialized with the full MiniMessage
 * tag set. Interactive tags ({@code click}, {@code hover}, {@code insertion}, {@code selector},
 * ...) are excluded, so a player can never inject a component that runs a command or shows
 * arbitrary tooltips on click.
 */
public final class PlayerMessages {

    private PlayerMessages() {
    }

    /**
     * Creates a MiniMessage instance that only understands cosmetic tags: colors, decorations,
     * gradients, rainbow and reset. Everything else is treated as literal text.
     *
     * @param allowGradients whether {@code <gradient>} and {@code <rainbow>} are permitted
     * @return a restricted MiniMessage instance suitable for player-supplied input
     */
    public static MiniMessage colorParser(boolean allowGradients) {
        TagResolver resolver = allowGradients
                ? TagResolver.resolver(
                        StandardTags.color(),
                        StandardTags.decorations(),
                        StandardTags.gradient(),
                        StandardTags.rainbow(),
                        StandardTags.reset())
                : TagResolver.resolver(
                        StandardTags.color(),
                        StandardTags.decorations(),
                        StandardTags.reset());
        return MiniMessage.builder().tags(resolver).build();
    }

    /**
     * Renders a raw player chat message into a safe component.
     *
     * @param colorParser the restricted parser from {@link #colorParser(boolean)}
     * @param raw         the raw chat input (may be {@code null})
     * @param allowColor  whether the player is permitted to use color codes / cosmetic tags
     * @return a component; literal text when {@code allowColor} is false, otherwise cosmetically
     *         formatted with legacy codes converted and only safe MiniMessage tags honoured
     */
    public static Component render(MiniMessage colorParser, String raw, boolean allowColor) {
        String text = raw == null ? "" : raw;
        if (!allowColor) {
            return Component.text(text);
        }
        return colorParser.deserialize(LegacyColors.toMiniMessage(text));
    }
}
