package com.puddingkc;

import dev.lone.itemsadder.api.CustomStack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BonfireItemDamaged extends JavaPlugin {

    public String level0;
    public String level1;
    public String level2;
    public String level3;
    public String level4;
    public String level5;
    public boolean writeStateLore;

    public FileConfiguration configuration;

    public final Map<Material, Integer> vanilla = new ConcurrentHashMap<>();
    public final Map<String, Integer> itemsAdder = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        loadConfig();
        getServer().getPluginManager().registerEvents(new BonfireListeners(this), this);
        getLogger().info("BonfireItemDamaged enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BonfireItemDamaged disabled!");
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        configuration = getConfig();

        level0 = configuration.getString("lore.level_0", "&fCondition: &aPerfect");
        level1 = configuration.getString("lore.level_1", "&fCondition: &2Excellent");
        level2 = configuration.getString("lore.level_2", "&fCondition: &6Good");
        level3 = configuration.getString("lore.level_3", "&fCondition: &eNormal");
        level4 = configuration.getString("lore.level_4", "&fCondition: &cDamaged");
        level5 = configuration.getString("lore.level_5", "&fCondition: &4Broken");
        writeStateLore = configuration.getBoolean("lore.write-state-line", false);

        vanilla.clear();
        itemsAdder.clear();

        Logger logger = getLogger();

        if (configuration.contains("VanillaList")) {
            List<String> list = configuration.getStringList("VanillaList");
            for (String row : list) {
                if (row == null || row.isBlank()) {
                    continue;
                }
                String[] parts = row.split("-");
                if (parts.length != 2) {
                    logger.warning("Invalid VanillaList row: " + row);
                    continue;
                }
                String typeName = parts[0].trim().toUpperCase();
                try {
                    int timeMinutes = Integer.parseInt(parts[1].trim());
                    Material material = Material.valueOf(typeName);
                    vanilla.put(material, timeMinutes);
                    logger.info("Loaded vanilla item: " + typeName + " -> " + timeMinutes + " min");
                } catch (IllegalArgumentException ex) {
                    logger.warning("Invalid vanilla item in config: " + typeName);
                }
            }
        }

        if (configuration.contains("ItemsAdderList")) {
            List<String> list = configuration.getStringList("ItemsAdderList");
            for (String row : list) {
                if (row == null || row.isBlank()) {
                    continue;
                }
                String[] parts = row.split("-");
                if (parts.length != 2) {
                    logger.warning("Invalid ItemsAdderList row: " + row);
                    continue;
                }
                String namespaced = parts[0].trim();
                try {
                    int timeMinutes = Integer.parseInt(parts[1].trim());
                    CustomStack stack = CustomStack.getInstance(namespaced);
                    if (stack != null) {
                        String key = stack.getNamespacedID();
                        itemsAdder.put(key, timeMinutes);
                        logger.info("Loaded ItemsAdder item: " + key + " -> " + timeMinutes + " min");
                    } else {
                        logger.warning("ItemsAdder item not found: " + namespaced);
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warning("Invalid ItemsAdder row: " + row);
                }
            }
        }
    }
}
