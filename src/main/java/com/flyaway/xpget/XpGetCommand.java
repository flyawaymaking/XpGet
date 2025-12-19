package com.flyaway.xpget;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XpGetCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private int EXP_PER_BOTTLE;

    public XpGetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    private void loadConfigValues() {
        this.EXP_PER_BOTTLE = plugin.getConfig().getInt("exp-per-bottle", 7);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player-only");
            return true;
        }

        if (!player.hasPermission("xpget.use")) {
            sendMessage(player, "messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            openXpGetGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            reloadConfigCommand(player);
            return true;
        } else if (subCommand.equals("max")) {
            convertMax(player);
        } else {
            try {
                int amount = Integer.parseInt(subCommand);
                convertSpecific(player, amount);
            } catch (NumberFormatException e) {
                sendMessage(player, "messages.invalid-amount");
                sendUsage(player);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("max");

            if (sender.hasPermission("xpget.reload")) {
                commands.add("reload");
            }

            String input = args[0].toLowerCase();
            for (String cmd : commands) {
                if (cmd.startsWith(input)) {
                    completions.add(cmd);
                }
            }

            if (input.isEmpty() || Character.isDigit(input.charAt(0))) {
                try {
                    if (input.isEmpty()) {
                        for (int i : Arrays.asList(1, 5, 10, 16, 32, 64, 128)) {
                            completions.add(String.valueOf(i));
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return completions;
    }

    private void reloadConfigCommand(Player player) {
        if (!player.hasPermission("xpget.reload")) {
            sendMessage(player, "messages.no-permission");
            return;
        }

        try {
            plugin.reloadConfig();
            loadConfigValues();
            sendMessage(player, "messages.config-reloaded");
        } catch (Exception e) {
            sendMessage(player, "messages.reload-error");
            plugin.getLogger().severe("Ошибка при перезагрузке конфига: " + e.getMessage());
        }
    }

    private void openXpGetGUI(Player player) {
        XpGetGUI gui = new XpGetGUI(player, plugin, this);
        gui.open();
    }

    private void sendUsage(Player player) {
        sendMessage(player, "messages.usage");
    }

    private void convertSpecific(Player player, int bottleAmount) {
        if (bottleAmount <= 0) {
            sendMessage(player, "messages.positive-amount");
            return;
        }

        int requiredExp = bottleAmount * EXP_PER_BOTTLE;
        int playerExp = getTotalPlayerExperience(player);

        if (playerExp < requiredExp) {
            sendMessage(player, "messages.not-enough-exp",
                    "required", String.valueOf(requiredExp),
                    "current", String.valueOf(playerExp));
            return;
        }

        int emptyBottles = countEmptyBottles(player.getInventory());

        if (emptyBottles < bottleAmount) {
            sendMessage(player, "messages.not-enough-empty",
                    "needed", String.valueOf(bottleAmount),
                    "current", String.valueOf(emptyBottles));
            return;
        }

        int availableSpace = getAvailableSpaceConsideringReplacement(player.getInventory());
        if (availableSpace < bottleAmount) {
            sendMessage(player, "messages.not-enough-space",
                    "available", String.valueOf(availableSpace));
            return;
        }

        if (performConversion(player, bottleAmount)) {
            sendMessage(player, "messages.success-specific",
                    "amount", String.valueOf(bottleAmount));
        } else {
            sendMessage(player, "messages.conversion-error");
        }
    }

    private void convertMax(Player player) {
        int playerExp = getTotalPlayerExperience(player);
        int emptyBottles = countEmptyBottles(player.getInventory());

        int maxByExp = playerExp / EXP_PER_BOTTLE;
        int maxBottles = Math.min(maxByExp, emptyBottles);

        if (maxBottles <= 0) {
            if (emptyBottles == 0) {
                sendMessage(player, "messages.no-empty-bottles");
            } else {
                sendMessage(player, "messages.not-enough-exp-for-one");
            }
            return;
        }

        int actualBottles = Math.min(maxBottles, getAvailableSpaceConsideringReplacement(player.getInventory()));

        if (actualBottles <= 0) {
            sendMessage(player, "messages.not-enough-space-even");
            return;
        }

        if (actualBottles < maxBottles) {
            sendMessage(player, "messages.limited-space",
                    "actual", String.valueOf(actualBottles),
                    "max", String.valueOf(maxBottles));
        }

        if (performConversion(player, actualBottles)) {
            sendMessage(player, "messages.success-max",
                    "actual", String.valueOf(actualBottles));

            if (actualBottles == maxByExp) {
                sendMessage(player, "messages.limited-by-exp");
            } else if (actualBottles == emptyBottles) {
                sendMessage(player, "messages.limited-by-empty");
            } else {
                sendMessage(player, "messages.limited-by-space");
            }
        } else {
            sendMessage(player, "messages.conversion-error");
        }
    }

    private boolean performConversion(Player player, int bottleAmount) {
        PlayerInventory inventory = player.getInventory();
        int requiredExp = bottleAmount * EXP_PER_BOTTLE;

        removeEmptyBottles(inventory, bottleAmount);
        takeExperience(player, requiredExp);
        addExperienceBottles(inventory, bottleAmount);
        return true;
    }

    private int getAvailableSpaceConsideringReplacement(PlayerInventory inventory) {
        int availableSpace = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                availableSpace += 64;
            } else if (item.getType() == Material.EXPERIENCE_BOTTLE && item.getAmount() < 64) {
                availableSpace += 64 - item.getAmount();
            } else if (item.getType() == Material.GLASS_BOTTLE) {
                availableSpace += item.getAmount();
            }
        }

        return availableSpace;
    }

    private void sendMessage(CommandSender sender, String configPath, String... placeholders) {
        String message = plugin.getConfig().getString(configPath);
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

    private void addExperienceBottles(PlayerInventory inventory, int totalAmount) {
        int remaining = totalAmount;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE && item.getAmount() < 64) {
                int canAdd = 64 - item.getAmount();
                int toAdd = Math.min(canAdd, remaining);
                item.setAmount(item.getAmount() + toAdd);
                remaining -= toAdd;
                if (remaining <= 0) return;
            }
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (remaining <= 0) break;

            ItemStack item = inventory.getItem(i);
            if (item == null) {
                int stackSize = Math.min(64, remaining);
                inventory.setItem(i, new ItemStack(Material.EXPERIENCE_BOTTLE, stackSize));
                remaining -= stackSize;
            }
        }

        if (remaining > 0) {
            while (remaining > 0) {
                int stackSize = Math.min(64, remaining);
                ItemStack newStack = new ItemStack(Material.EXPERIENCE_BOTTLE, stackSize);
                inventory.addItem(newStack);
                remaining -= stackSize;
            }
        }
    }

    private int countEmptyBottles(PlayerInventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeEmptyBottles(PlayerInventory inventory, int amountToRemove) {
        int remaining = amountToRemove;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                int amount = item.getAmount();

                if (amount <= remaining) {
                    inventory.setItem(i, null);
                    remaining -= amount;
                } else {
                    item.setAmount(amount - remaining);
                    remaining = 0;
                }

                if (remaining <= 0) break;
            }
        }
    }

    private int getTotalPlayerExperience(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        int totalExp = 0;

        totalExp += Math.round(progress * getExpToNextLevel(level));

        for (int i = 0; i < level; i++) {
            totalExp += getExpToNextLevel(i);
        }

        return totalExp;
    }

    private void takeExperience(Player player, int expToTake) {
        int currentTotalExp = getTotalPlayerExperience(player);
        int newTotalExp = Math.max(0, currentTotalExp - expToTake);
        setTotalExperience(player, newTotalExp);
    }

    private void setTotalExperience(Player player, int totalExp) {
        player.setExp(0);
        player.setLevel(0);

        int level = 0;
        int expForNextLevel = getExpToNextLevel(level);

        while (totalExp >= expForNextLevel) {
            totalExp -= expForNextLevel;
            level++;
            expForNextLevel = getExpToNextLevel(level);
        }

        float progress = (float) totalExp / expForNextLevel;
        player.setLevel(level);
        player.setExp(progress);
    }

    private int getExpToNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }
}
