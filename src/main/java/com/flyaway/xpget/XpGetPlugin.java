package com.flyaway.xpget;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XpGetPlugin extends JavaPlugin {
    private XpBottleManager xpBottleManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadBottleManager();
        XpGetCommand xpGetCommand = new XpGetCommand(this);

        getServer().getPluginManager().registerEvents(new XpGetListener(), this);

        getCommand("xpget").setExecutor(xpGetCommand);
        getCommand("xpget").setTabCompleter(xpGetCommand);
        getLogger().info("XpGetPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XpGetPlugin выключен!");
    }

    private void loadBottleManager() {
        int expPerBottle = getConfig().getInt("exp-per-bottle", 7);
        xpBottleManager = new XpBottleManager(this, expPerBottle);
    }

    public void reload() {
        reloadConfig();
        loadBottleManager();
    }

    public void sendMessage(CommandSender sender, String configPath, String... placeholders) {
        String message = getConfig().getString("messages." + configPath);
        if (message == null) {
            message = "<red>Message not found: " + configPath;
        }

        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
                }
            }
        }

        sender.sendMessage(miniMessage.deserialize(message));
    }

    public XpBottleManager getXpBottleManager() {
        return xpBottleManager;
    }

    public @NotNull Component toComponent(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return miniMessage.deserialize("<!italic>" + text);
    }
}
