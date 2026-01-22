package com.flyaway.xpget;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class XpGetCommand implements CommandExecutor, TabCompleter {

    private final XpGetPlugin plugin;

    public XpGetCommand(XpGetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            plugin.sendMessage(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("xpget.use")) {
            plugin.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            openXpGetGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            reloadConfigCommand(player);
        } else if (subCommand.equals("max")) {
            plugin.getXpBottleManager().convertMax(player);
        } else {
            try {
                int amount = Integer.parseInt(subCommand);
                plugin.getXpBottleManager().convertSpecific(player, amount);
            } catch (NumberFormatException e) {
                plugin.sendMessage(player, "invalid-amount");
                plugin.sendMessage(player, "usage");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = sender.hasPermission("xpget.reload")
                    ? List.of("max", "reload")
                    : List.of("max");

            String input = args[0].toLowerCase();
            for (String cmd : commands) {
                if (cmd.startsWith(input)) {
                    completions.add(cmd);
                }
            }

            if (input.isEmpty() || Character.isDigit(input.charAt(0))) {
                if (input.isEmpty()) {
                    completions.addAll(List.of("1", "5", "10", "16", "32", "64", "128"));
                }
            }
        }

        return completions;
    }

    private void reloadConfigCommand(Player player) {
        if (!player.hasPermission("xpget.reload")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        plugin.reload();
        plugin.sendMessage(player, "config-reloaded");
    }

    private void openXpGetGUI(Player player) {
        XpGetGUI gui = new XpGetGUI(player, plugin);
        gui.open();
    }
}
