package com.flyaway.xpget;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class XpGetCommand implements CommandExecutor {

    private static final int EXP_PER_BOTTLE = 7; // Опыт за одну бутылочку
    private final JavaPlugin plugin;

    public XpGetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        // Проверка прав
        if (!player.hasPermission("xpget.use")) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String amountArg = args[0].toLowerCase();

        if (amountArg.equals("max")) {
            convertMax(player);
        } else {
            try {
                int amount = Integer.parseInt(amountArg);
                convertSpecific(player, amount);
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверное количество! Используйте число или 'max'");
                sendUsage(player);
            }
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6Использование: §e/xpget [количество|max]");
        player.sendMessage("§6Примеры:");
        player.sendMessage("§e/xpget 10 §7- конвертировать опыт в 10 бутылочек");
        player.sendMessage("§e/xpget max §7- конвертировать максимально возможное количество");
        player.sendMessage("§6Примечание: Можно конвертировать любое количество бутылочек");
    }

    private void convertSpecific(Player player, int bottleAmount) {
        if (bottleAmount <= 0) {
            player.sendMessage("§cКоличество должно быть положительным!");
            return;
        }

        // Проверяем, достаточно ли у игрока опыта
        int requiredExp = bottleAmount * EXP_PER_BOTTLE;
        int playerExp = getTotalPlayerExperience(player);

        if (playerExp < requiredExp) {
            player.sendMessage("§cНедостаточно опыта! Нужно: " + requiredExp + " опыта, у вас: " + playerExp);
            return;
        }

        // Проверяем, есть ли пустые бутылочки
        int emptyBottles = countEmptyBottles(player.getInventory());

        if (emptyBottles < bottleAmount) {
            player.sendMessage("§cНедостаточно пустых бутылочек! Нужно: " + bottleAmount + ", у вас: " + emptyBottles);
            return;
        }

        // ВАЖНОЕ ИЗМЕНЕНИЕ: Проверяем место с учетом того, что пустые бутылочки освободят место
        int availableSpace = getAvailableSpaceConsideringReplacement(player.getInventory(), bottleAmount);
        if (availableSpace < bottleAmount) {
            player.sendMessage("§cНедостаточно места в инвентаре! Доступно места для: " + availableSpace + " бутылочек");
            return;
        }

        // Выполняем конвертацию
        if (performConversion(player, bottleAmount)) {
            player.sendMessage("§aУспешно конвертировано " + bottleAmount + " бутылочек опыта!");
        } else {
            player.sendMessage("§cПроизошла ошибка при конвертации!");
        }
    }

    private void convertMax(Player player) {
        int playerExp = getTotalPlayerExperience(player);
        int emptyBottles = countEmptyBottles(player.getInventory());

        // Максимальное количество бутылочек, которое можно создать
        int maxByExp = playerExp / EXP_PER_BOTTLE;

        int maxBottles = Math.min(maxByExp, emptyBottles);

        if (maxBottles <= 0) {
            if (emptyBottles == 0) {
                player.sendMessage("§cУ вас нет пустых бутылочек!");
            } else {
                player.sendMessage("§cУ вас недостаточно опыта для создания хотя бы одной бутылочки!");
            }
            return;
        }

        // ВАЖНОЕ ИЗМЕНЕНИЕ: Учитываем доступное место с учетом замены
        int actualBottles = Math.min(maxBottles, getAvailableSpaceConsideringReplacement(player.getInventory(), maxBottles));

        if (actualBottles <= 0) {
            player.sendMessage("§cНедостаточно места в инвентаре даже с учетом замены бутылочек!");
            return;
        }

        if (actualBottles < maxBottles) {
            player.sendMessage("§eВнимание: Конвертировано " + actualBottles + " вместо " + maxBottles + " из-за нехватки места в инвентаре");
        }

        if (performConversion(player, actualBottles)) {
            player.sendMessage("§aУспешно конвертировано " + actualBottles + " бутылочек опыта!");

            // Показываем информацию об ограничивающем факторе
            if (actualBottles == maxByExp) {
                player.sendMessage("§7Ограничено количеством опыта");
            } else if (actualBottles == emptyBottles) {
            } else {
                player.sendMessage("§7Ограничено местом в инвентаре");
            }
        } else {
            player.sendMessage("§cПроизошла ошибка при конвертации!");
        }
    }

    private boolean performConversion(Player player, int bottleAmount) {
        PlayerInventory inventory = player.getInventory();
        int requiredExp = bottleAmount * EXP_PER_BOTTLE;

        // Удаляем пустые бутылочки
        removeEmptyBottles(inventory, bottleAmount);

        // Забираем опыт
        takeExperience(player, requiredExp);

        // Добавляем бутылочки опыта (правильно обрабатываем большое количество)
        addExperienceBottles(inventory, bottleAmount);

        // Логируем действие для отладки
        plugin.getLogger().info("Игрок " + player.getName() + " конвертировал " +
                bottleAmount + " бутылочек (" + requiredExp + " опыта)");

        return true;
    }

    /**
     * ВАЖНЫЙ МЕТОД: Рассчитывает доступное место с учетом того, что пустые бутылочки будут заменены на бутылочки опыта
     */
    private int getAvailableSpaceConsideringReplacement(PlayerInventory inventory, int bottlesToConvert) {
        int availableSpace = 0;

        // Считаем текущее доступное место для бутылочек опыта
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                // Пустой слот может вместить 64 бутылочки
                availableSpace += 64;
            } else if (item.getType() == Material.EXPERIENCE_BOTTLE && item.getAmount() < 64) {
                // Существующий стек бутылочек опыта может быть дополнен
                availableSpace += 64 - item.getAmount();
            } else if (item.getType() == Material.GLASS_BOTTLE) {
                // Пустая бутылочка будет заменена - это освободит место!
                // Каждая пустая бутылочка освобождает место для одной бутылочки опыта
                availableSpace += item.getAmount();
            }
            // Другие предметы не учитываем - они не освободят место
        }

        return availableSpace;
    }

    private void addExperienceBottles(PlayerInventory inventory, int totalAmount) {
        int remaining = totalAmount;

        // Сначала попробуем добавить к существующим стекам бутылочек опыта
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

        // Затем добавляем в пустые слоты
        for (int i = 0; i < inventory.getSize(); i++) {
            if (remaining <= 0) break;

            ItemStack item = inventory.getItem(i);
            if (item == null) {
                int stackSize = Math.min(64, remaining);
                inventory.setItem(i, new ItemStack(Material.EXPERIENCE_BOTTLE, stackSize));
                remaining -= stackSize;
            }
        }

        // Если остались бутылочки (маловероятно, но на всякий случай), добавляем через addItem
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
                    inventory.setItem(i, null); // Полностью удаляем стек
                    remaining -= amount;
                } else {
                    item.setAmount(amount - remaining); // Уменьшаем стек
                    remaining = 0;
                }

                if (remaining <= 0) break;
            }
        }
    }

    // Методы для работы с опытом (остаются без изменений)
    private int getTotalPlayerExperience(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        int totalExp = 0;

        // Добавляем опыт за текущий уровень
        totalExp += Math.round(progress * getExpToNextLevel(level));

        // Добавляем опыт за все предыдущие уровни
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
