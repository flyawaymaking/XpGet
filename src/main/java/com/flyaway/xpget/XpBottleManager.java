package com.flyaway.xpget;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class XpBottleManager {
    private final XpGetPlugin plugin;
    public final int EXP_PER_BOTTLE;

    public XpBottleManager(XpGetPlugin plugin, int expPerBottle) {
        this.plugin = plugin;
        this.EXP_PER_BOTTLE = expPerBottle;
    }

    public record InventoryStats(int emptyBottles, int spaceForXp) {
    }

    public InventoryStats scanInventory(PlayerInventory inv) {
        int empty = 0;
        int space = 0;

        ItemStack[] contents = inv.getStorageContents();

        for (ItemStack item : contents) {
            if (item == null) {
                space += 64;
            } else if (item.getType() == Material.GLASS_BOTTLE) {
                empty += item.getAmount();
                space += item.getAmount();
            } else if (item.getType() == Material.EXPERIENCE_BOTTLE) {
                space += 64 - item.getAmount();
            }
        }
        return new InventoryStats(empty, space);
    }

    public int getMaxBottlesToConvert(Player player) {
        int playerExp = player.getTotalExperience();
        InventoryStats stats = scanInventory(player.getInventory());

        int maxByExp = playerExp / EXP_PER_BOTTLE;
        int maxBottles = Math.min(maxByExp, stats.emptyBottles());
        int actualBottles = Math.min(maxBottles, stats.spaceForXp());
        return Math.max(actualBottles, 0);
    }

    public void convertSpecific(Player player, int bottleAmount) {
        int requiredExp = bottleAmount * EXP_PER_BOTTLE;
        int playerExp = player.getTotalExperience();

        if (playerExp < requiredExp) {
            plugin.sendMessage(player, "not-enough-exp",
                    "required", String.valueOf(requiredExp),
                    "current", String.valueOf(playerExp));
            return;
        }

        InventoryStats stats = scanInventory(player.getInventory());

        if (stats.emptyBottles() < bottleAmount) {
            plugin.sendMessage(player, "not-enough-empty",
                    "needed", String.valueOf(bottleAmount),
                    "current", String.valueOf(stats.emptyBottles()));
            return;
        }

        if (stats.spaceForXp() < bottleAmount) {
            plugin.sendMessage(player, "not-enough-space",
                    "available", String.valueOf(stats.spaceForXp()));
            return;
        }

        performConversion(player, bottleAmount);
        plugin.sendMessage(player, "success-specific",
                "amount", String.valueOf(bottleAmount));
    }

    public void convertMax(Player player) {
        int playerExp = player.getTotalExperience();

        InventoryStats stats = scanInventory(player.getInventory());
        int maxByExp = playerExp / EXP_PER_BOTTLE;
        int maxBottles = Math.min(maxByExp, stats.emptyBottles());

        if (maxBottles <= 0) {
            if (stats.emptyBottles() == 0) {
                plugin.sendMessage(player, "no-empty-bottles");
            } else {
                plugin.sendMessage(player, "not-enough-exp-for-one");
            }
            return;
        }

        int actualBottles = Math.min(maxBottles, stats.spaceForXp());
        if (actualBottles <= 0) {
            plugin.sendMessage(player, "not-enough-space-even");
            return;
        }

        performConversion(player, actualBottles);
        plugin.sendMessage(player, "success-max", "actual", String.valueOf(actualBottles));
    }

    private void performConversion(Player player, int bottleAmount) {
        PlayerInventory inventory = player.getInventory();
        int requiredExp = bottleAmount * EXP_PER_BOTTLE;

        inventory.removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleAmount));
        player.giveExp(-requiredExp);
        Map<Integer, ItemStack> leftoverItems = inventory.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, bottleAmount));

        if (!leftoverItems.isEmpty()) {
            Location dropLocation = player.getLocation();
            World world = player.getWorld();
            for (ItemStack item : leftoverItems.values()) {
                world.dropItemNaturally(dropLocation, item);
            }
        }
    }
}
