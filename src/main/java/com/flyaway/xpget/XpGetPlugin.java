package com.flyaway.xpget;

import org.bukkit.plugin.java.JavaPlugin;

public class XpGetPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Регистрируем команду с проверкой прав
        getCommand("xpget").setExecutor(new XpGetCommand(this));
        getLogger().info("XpGetPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XpGetPlugin выключен!");
    }
}
