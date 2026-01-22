package com.flyaway.xpget;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MenuHelper {
    private final XpGetPlugin plugin;
    private final NamespacedKey actionsKey;
    private final Set<String> excludedKeys;

    public MenuHelper(XpGetPlugin plugin, String... excludedKeys) {
        this.plugin = plugin;
        this.actionsKey = new NamespacedKey(plugin, "actions");

        Set<String> allExcludedKeys = new HashSet<>(Arrays.asList(excludedKeys));
        allExcludedKeys.add("size");
        allExcludedKeys.add("title");
        this.excludedKeys = allExcludedKeys;
    }

    private ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection("gui");
    }

    public @NotNull Component getTitle(@NotNull String... placeholders) {
        String title = getConfigSection().getString("title", "<red>Title");
        return plugin.toComponent(replacePlaceholders(title, placeholders));
    }

    public int getSize() {
        return getConfigSection().getInt("size", 27);
    }

    public boolean isEnabled(@NotNull String path) {
        return getConfigSection().getBoolean(path, true);
    }

    public void setItemToSlot(@NotNull Inventory inv, @NotNull String path, @NotNull ItemStack item) {
        int slot = getConfigSection().getInt(path + ".slot");
        if (slot < inv.getSize()) inv.setItem(slot, item);
    }

    public void addItem(@NotNull Inventory inventory, @NotNull String path, @NotNull String... placeholders) {
        if (!getConfigSection().isConfigurationSection(path)) return;

        String name = getName(path);
        List<Component> lore = getLore(path, placeholders);
        ItemStack item = getItemStack(path);

        applyMetaToItem(item, name, lore, List.of("[main-item] " + path));

        setItemToSlot(inventory, path, item);
    }

    public void addCustomItems(@NotNull Inventory inventory) {
        for (String key : getConfigSection().getKeys(false)) {
            if (excludedKeys.contains(key)) {
                continue;
            }

            ItemStack item = createCustomItem(key);

            setItemToSlot(inventory, key, item);
        }
    }

    public @NotNull ItemStack createCustomItem(@NotNull String path, @NotNull String... placeholders) {
        String name = getName(path);
        List<Component> lore = getLore(path, placeholders);
        ItemStack item = getItemStack(path);
        List<String> actions = getConfigSection().getStringList(path + ".actions");

        applyMetaToItem(item, name, lore, actions);
        return item;
    }

    private @NotNull String getName(@NotNull String path) {
        return getConfigSection().getString(path + ".name", path);
    }

    private @NotNull List<Component> getLore(@NotNull String path, @NotNull String... placeholders) {
        List<String> loreStrings = getConfigSection().getStringList(path + ".lore");
        List<Component> lore = new ArrayList<>();
        for (String loreLine : loreStrings) {
            loreLine = replacePlaceholders(loreLine, placeholders);
            lore.add(plugin.toComponent(loreLine));
        }
        return lore;
    }

    private @NotNull ItemStack getItemStack(@NotNull String path) {
        int amount = getConfigSection().getInt(path + ".amount", 1);
        String materialName = getConfigSection().getString(path + ".material");
        Material material = null;

        if (materialName != null) {
            material = Material.getMaterial(materialName.toUpperCase());
        }

        if (material == null || material.isAir() || !material.isItem()) {
            plugin.getLogger().info("Material " + materialName + " is invalid, using BEDROCK material!");
            material = Material.BEDROCK;
        }

        return new ItemStack(material, amount);
    }

    private void applyMetaToItem(@NotNull ItemStack item, @Nullable String displayName, @NotNull List<Component> lore, @Nullable List<String> actions) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.displayName(plugin.toComponent(displayName));
            }
            meta.lore(lore);
            if (actions != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                String actionsString = String.join(";:;", actions);
                pdc.set(actionsKey, PersistentDataType.STRING, actionsString);
            }
            item.setItemMeta(meta);
        }
    }

    private @NotNull String replacePlaceholders(@NotNull String text, @NotNull String... placeholders) {
        if (placeholders == null || placeholders.length % 2 != 0) {
            return text;
        }

        String result = text;
        for (int i = 0; i < placeholders.length; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return result;
    }

    public @Nullable List<String> getItemActions(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String actionsString = pdc.get(actionsKey, PersistentDataType.STRING);
        if (actionsString == null || actionsString.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(actionsString.split(";:;"));
    }
}
