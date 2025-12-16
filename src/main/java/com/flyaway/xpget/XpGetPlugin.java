package com.flyaway.xpget;

import org.bukkit.plugin.java.JavaPlugin;

public class XpGetPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        XpGetCommand xpGetCommand = new XpGetCommand(this);

        getCommand("xpget").setExecutor(xpGetCommand);
        getCommand("xpget").setTabCompleter(xpGetCommand);
        getLogger().info("XpGetPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XpGetPlugin выключен!");
    }
}
