package com.flyaway.xpget;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XpGetGUI implements InventoryHolder {
    private final MenuHelper menuHelper;
    private Inventory inventory;
    private final Player player;
    private final XpGetPlugin plugin;

    public XpGetGUI(Player player, XpGetPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.menuHelper = new MenuHelper(plugin, "divider", "items");
        rebuild();
    }

    private void rebuild() {
        this.inventory = Bukkit.createInventory(this, menuHelper.getSize(), menuHelper.getTitle());
        initializeItems();
    }

    private void initializeItems() {
        XpBottleManager manager = plugin.getXpBottleManager();
        int playerExp = manager.getCurrentExp(player);
        int maxBottles = manager.getMaxBottlesToConvert(player);
        addConversionItem("items.one", playerExp, 1);
        addConversionItem("items.stack", playerExp, 64);
        addConversionItem("items.max", playerExp, maxBottles);
        menuHelper.addCustomItems(inventory);

        if (!menuHelper.isEnabled("divider")) return;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) continue;

            ItemStack item = menuHelper.createCustomItem("divider");
            inventory.setItem(i, item);
        }
    }

    /**
     * {@code {levels}} is what the conversion costs from the player's current level, since a
     * level is worth more points the higher it is. {@code lore-not-enough-exp} replaces
     * {@code lore} when the player cannot afford the conversion.
     */
    private void addConversionItem(@NotNull String path, int playerExp, int bottles) {
        int expPerBottle = plugin.getXpBottleManager().EXP_PER_BOTTLE;
        int requiredExp = bottles * expPerBottle;
        int level = XpLevels.levelForPoints(playerExp);
        boolean enoughExp = playerExp >= Math.max(requiredExp, expPerBottle);
        int levelAfter = enoughExp ? XpLevels.levelForPoints(playerExp - requiredExp) : level;

        menuHelper.addItem(inventory, path, !enoughExp,
                "{exp}", String.valueOf(requiredExp),
                "{level}", String.valueOf(level),
                "{level_after}", String.valueOf(levelAfter),
                "{levels}", String.valueOf(level - levelAfter),
                "{level_required}", String.valueOf(XpLevels.levelRequiredFor(Math.max(requiredExp, expPerBottle))),
                "{max_bottles}", String.valueOf(bottles));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Inventory clickedInv = event.getClickedInventory();

        if (clickedInv == null || !(clickedInv.getHolder() instanceof XpGetGUI)) return;

        List<String> actions = menuHelper.getItemActions(clickedItem);
        if (actions == null || actions.isEmpty()) return;
        Player player = (Player) event.getWhoClicked();

        for (String action : actions) {
            handleAction(player, action);
        }
    }

    private void handleAction(@NotNull Player player, @NotNull String action) {
        int idx = action.indexOf(']');
        if (idx == -1) return;
        String subAction = action.substring(idx + 1).trim();

        if (action.startsWith("[close]")) {
            player.closeInventory();
        } else if (action.startsWith("[cmd]")) {
            player.performCommand(subAction);
        } else if (action.startsWith("[main-item]")) {
            switch (subAction) {
                case "items.one" -> plugin.getXpBottleManager().convertSpecific(player, 1);
                case "items.stack" -> plugin.getXpBottleManager().convertSpecific(player, 64);
                case "items.max" -> plugin.getXpBottleManager().convertMax(player);
            }
            rebuild();
            player.openInventory(inventory);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
