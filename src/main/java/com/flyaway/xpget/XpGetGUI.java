package com.flyaway.xpget;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XpGetGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final JavaPlugin plugin;
    private final XpGetCommand xpGetCommand;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public XpGetGUI(Player player, JavaPlugin plugin, XpGetCommand xpGetCommand) {
        this.player = player;
        this.plugin = plugin;
        this.xpGetCommand = xpGetCommand;
        this.inventory = Bukkit.createInventory(this, 27, getTitle());
        initializeItems();
    }

    private Component getTitle() {
        String title = plugin.getConfig().getString("gui.title", "<gold>Конвертация опыта");
        return miniMessage.deserialize(title);
    }

    private void initializeItems() {
        ItemStack item1 = createGuiItem(
                "gui.item-1.material",
                "gui.item-1.name",
                "gui.item-1.lore",
                "gui.item-1.amount"
        );
        inventory.setItem(11, item1);

        ItemStack item64 = createGuiItem(
                "gui.item-64.material",
                "gui.item-64.name",
                "gui.item-64.lore",
                "gui.item-64.amount"
        );
        inventory.setItem(13, item64);

        ItemStack itemMax = createGuiItem(
                "gui.item-max.material",
                "gui.item-max.name",
                "gui.item-max.lore",
                "gui.item-max.amount"
        );
        inventory.setItem(15, itemMax);

        ItemStack close = createGuiItem(
                "gui.close.material",
                "gui.close.name",
                "gui.close.lore",
                "gui.close.amount"
        );
        inventory.setItem(22, close);

        ItemStack border = createGuiItem(
                "gui.border.material",
                "gui.border.name",
                "gui.border.lore",
                "gui.border.amount"
        );

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }
    }

    private ItemStack createGuiItem(String materialPath, String namePath, String lorePath, String amountPath) {
        Material defaultMaterial = Material.STONE;
        String materialName = plugin.getConfig().getString(materialPath, defaultMaterial.name());
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = defaultMaterial;
            plugin.getLogger().warning("Invalid material in config: " + materialName + ", using default: " + defaultMaterial);
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        String displayName = plugin.getConfig().getString(namePath, "");
        if (!displayName.isEmpty()) {
            meta.displayName(miniMessage.deserialize(displayName));
        }

        List<String> loreConfig = plugin.getConfig().getStringList(lorePath);
        if (!loreConfig.isEmpty()) {
            List<Component> lore = loreConfig.stream()
                    .map(miniMessage::deserialize)
                    .toList();
            meta.lore(lore);
        }

        int amount = plugin.getConfig().getInt(amountPath, 1);
        if (amount > 1 & amount <= 64) {
            item.setAmount(amount);
        }

        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (slot) {
                case 11:
                    xpGetCommand.onCommand(player, null, "xpget", new String[]{"1"});
                    break;
                case 13:
                    xpGetCommand.onCommand(player, null, "xpget", new String[]{"64"});
                    break;
                case 15:
                    xpGetCommand.onCommand(player, null, "xpget", new String[]{"max"});
                    break;
                case 22:
                    player.closeInventory();
                    break;
            }
        });
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
