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
        int expPerBottle = plugin.getXpBottleManager().EXP_PER_BOTTLE;
        int maxBottles = plugin.getXpBottleManager().getMaxBottlesToConvert(player);
        menuHelper.addItem(inventory, "items.one", "{exp}", String.valueOf(expPerBottle));
        menuHelper.addItem(inventory, "items.stack", "{exp}", String.valueOf(expPerBottle * 64));
        menuHelper.addItem(inventory, "items.max",
                "{exp}", String.valueOf(expPerBottle * maxBottles),
                "{max_bottles}", String.valueOf(maxBottles));
        menuHelper.addCustomItems(inventory);

        if (!menuHelper.isEnabled("divider")) return;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) continue;

            ItemStack item = menuHelper.createCustomItem("divider");
            inventory.setItem(i, item);
        }
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
