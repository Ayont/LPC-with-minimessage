package de.ayont.lpc;

import de.ayont.lpc.chat.ChatFormatService;
import de.ayont.lpc.chat.EmojiReplacer;
import de.ayont.lpc.chat.MentionService;
import de.ayont.lpc.chat.UrlLinkifier;
import de.ayont.lpc.commands.LPCCommand;
import de.ayont.lpc.listener.AsyncChatListener;
import de.ayont.lpc.listener.ConnectionListener;
import de.ayont.lpc.listener.SpigotChatListener;
import de.ayont.lpc.moderation.ModerationService;
import de.ayont.lpc.moderation.MuteService;
import de.ayont.lpc.scheduler.Scheduler;
import de.ayont.lpc.scheduler.Schedulers;
import de.ayont.lpc.update.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LPC extends JavaPlugin {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private boolean paper;
    private boolean folia;
    private Scheduler scheduler;
    private ChatFormatService chatFormatService;
    private MuteService muteService;
    private ModerationService moderationService;
    private EmojiReplacer emojiReplacer;
    private UrlLinkifier urlLinkifier;
    private MentionService mentionService;

    public static LegacyComponentSerializer getLegacySerializer() {
        return LEGACY_SERIALIZER;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.paper = detectPaper();
        this.folia = detectFolia();
        this.scheduler = Schedulers.create(this);
        this.chatFormatService = new ChatFormatService(this);
        this.muteService = new MuteService(this);
        this.moderationService = new ModerationService(this, muteService);
        this.emojiReplacer = new EmojiReplacer(this);
        this.urlLinkifier = new UrlLinkifier(this);
        this.mentionService = new MentionService(this);
        registerCommand();
        registerListeners();
        startUpdateChecker();
        logRuntimePlatform();
    }

    /** Logs the detected server + Java version — the single universal jar runs on many, so make
     *  the actual runtime platform visible for support. */
    private void logRuntimePlatform() {
        getLogger().info("Running on " + getServer().getName() + " (API " + getServer().getBukkitVersion()
                + ") on Java " + System.getProperty("java.version")
                + (paper ? " [Paper/Adventure chat]" : " [Spigot/legacy chat]")
                + (folia ? " [Folia]" : ""));
    }

    public boolean isPaper() {
        return paper;
    }

    public boolean isFolia() {
        return folia;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.cancelAll();
        }
    }

    public ChatFormatService getChatFormatService() {
        return chatFormatService;
    }

    public ModerationService getModerationService() {
        return moderationService;
    }

    public MuteService getMuteService() {
        return muteService;
    }

    public EmojiReplacer getEmojiReplacer() {
        return emojiReplacer;
    }

    public UrlLinkifier getUrlLinkifier() {
        return urlLinkifier;
    }

    public MentionService getMentionService() {
        return mentionService;
    }

    /** Re-reads config-derived state for every service. Call after {@code reloadConfig()}. */
    public void reloadServices() {
        chatFormatService.reload();
        muteService.reload();
        moderationService.reload();
        emojiReplacer.reload();
        urlLinkifier.reload();
        mentionService.reload();
    }

    /** @return whether chat formatting is disabled in the given world. */
    public boolean isDisabledWorld(String worldName) {
        for (String world : getConfig().getStringList("disabled-worlds")) {
            if (world.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    /** Resolves a player's display name as a component on either platform. */
    @SuppressWarnings("deprecation") // getDisplayName() is the Spigot fallback
    public Component displayNameOf(Player player) {
        return paper ? player.displayName() : LEGACY_SERIALIZER.deserialize(player.getDisplayName());
    }

    /** Sends a component to a sender, falling back to legacy text on Spigot. */
    public void send(CommandSender target, Component component) {
        if (paper) {
            target.sendMessage(component);
        } else {
            target.sendMessage(LEGACY_SERIALIZER.serialize(component));
        }
    }

    private void registerCommand() {
        PluginCommand command = getCommand("lpc");
        if (command == null) {
            getLogger().warning("Command 'lpc' is missing from plugin.yml; commands are unavailable.");
            return;
        }
        LPCCommand executor = new LPCCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        if (paper) {
            pluginManager.registerEvents(new AsyncChatListener(this), this);
        } else {
            pluginManager.registerEvents(new SpigotChatListener(this), this);
        }
        pluginManager.registerEvents(new ConnectionListener(this), this);
    }

    private void startUpdateChecker() {
        if (!getConfig().getBoolean("update-checker", true)) {
            return;
        }
        UpdateChecker updateChecker = new UpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        updateChecker.checkAsync();
    }

    private boolean detectPaper() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            getLogger().info("Paper API detected — using Adventure chat rendering.");
            return true;
        } catch (ClassNotFoundException notPaper) {
            getLogger().info("Spigot API detected — using legacy chat rendering.");
            return false;
        }
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            getLogger().info("Folia detected — using regionized scheduling.");
            return true;
        } catch (ClassNotFoundException notFolia) {
            return false;
        }
    }
}
