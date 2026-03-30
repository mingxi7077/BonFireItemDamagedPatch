package com.puddingkc;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BonfireUtils {

    private static final Pattern HEX_INLINE = Pattern.compile("(?i)&#([0-9A-F]{6})");

    private final BonfireItemDamaged plugin;
    private final NamespacedKey creationTimeKey;
    private final NamespacedKey stateKey;

    public BonfireUtils(BonfireItemDamaged plugin) {
        this.plugin = plugin;
        this.creationTimeKey = new NamespacedKey(plugin, "creation_time");
        this.stateKey = new NamespacedKey(plugin, "state");
    }

    public void checkInventory(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack stack = contents[slot];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            checkItem(stack);
            inventory.setItem(slot, stack);
        }
    }

    public void checkItem(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }

        NBTItem nbt = new NBTItem(stack);
        if (nbt.hasTag("itemsadder")) {
            NBTCompound itemsAdder = nbt.getCompound("itemsadder");
            if (itemsAdder != null && itemsAdder.hasTag("id") && itemsAdder.hasTag("namespace")) {
                String id = itemsAdder.getString("id");
                String namespace = itemsAdder.getString("namespace");
                String namespaced = namespace + ":" + id;
                if (plugin.itemsAdder.containsKey(namespaced)) {
                    checkTime(stack);
                    return;
                }
            }
        }

        if (plugin.vanilla.containsKey(stack.getType())) {
            checkTime(stack);
        }
    }

    public void checkTime(ItemStack stack) {
        NBTItem nbt = new NBTItem(stack);
        long maxTimeMinutes = getMaxTime(stack, nbt);
        if (maxTimeMinutes <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        Integer originalDamage = null;
        if (meta instanceof Damageable damageable) {
            originalDamage = damageable.getDamage();
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        long creationTime = resolveCreationTime(now, nbt, pdc);

        long elapsed = Math.max(0L, now - creationTime);
        long maxMillis = maxTimeMinutes * 60L * 1000L;

        StateResult state = resolveState(elapsed, maxMillis);
        if (plugin.writeStateLore && state.rawLore != null) {
            applyStateLore(meta, state.rawLore);
        } else {
            removeStateLore(meta);
        }

        pdc.set(stateKey, PersistentDataType.INTEGER, state.stateIndex);

        if (originalDamage != null && meta instanceof Damageable damageable) {
            damageable.setDamage(originalDamage);
        }

        stack.setItemMeta(meta);
    }

    private long resolveCreationTime(long now, NBTItem nbt, PersistentDataContainer pdc) {
        if (pdc.has(creationTimeKey, PersistentDataType.LONG)) {
            Long stored = pdc.get(creationTimeKey, PersistentDataType.LONG);
            return stored != null ? stored : now;
        }

        long creationTime = now;
        if (nbt.hasTag("creation_time")) {
            Long old = nbt.getLong("creation_time");
            if (old != null) {
                creationTime = old;
            }
        }
        pdc.set(creationTimeKey, PersistentDataType.LONG, creationTime);
        return creationTime;
    }

    private StateResult resolveState(long elapsed, long maxMillis) {
        if (elapsed >= maxMillis) {
            return new StateResult(5, plugin.level5);
        }
        if (elapsed >= maxMillis / 2L) {
            return new StateResult(4, plugin.level4);
        }
        if (elapsed >= maxMillis / 3L) {
            return new StateResult(3, plugin.level3);
        }
        if (elapsed >= maxMillis / 5L) {
            return new StateResult(2, plugin.level2);
        }
        if (elapsed >= maxMillis / 6L) {
            return new StateResult(1, plugin.level1);
        }
        return new StateResult(0, plugin.level0);
    }

    private void applyStateLore(ItemMeta meta, String rawLore) {
        String rendered = colorize(rawLore);
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        List<String> knownRendered = knownRenderedStateLines();

        Set<String> knownNormalized = new LinkedHashSet<>();
        for (String line : knownRendered) {
            String normalized = normalizeForMatch(line);
            if (!normalized.isEmpty()) {
                knownNormalized.add(normalized);
            }
        }

        String statePrefix = resolveStatePrefix(knownNormalized);
        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String normalized = normalizeForMatch(line);
            if (!isStateLine(normalized, knownNormalized, statePrefix)) {
                continue;
            }
            if (!replaced) {
                lore.set(i, rendered);
                replaced = true;
            } else {
                lore.remove(i);
                i--;
            }
        }

        if (!replaced) {
            lore.add(rendered);
        }

        meta.setLore(lore.isEmpty() ? null : lore);
    }

    private void removeStateLore(ItemMeta meta) {
        if (!meta.hasLore()) {
            return;
        }
        List<String> lore = new ArrayList<>(meta.getLore());
        List<String> knownRendered = knownRenderedStateLines();

        Set<String> knownNormalized = new LinkedHashSet<>();
        for (String line : knownRendered) {
            String normalized = normalizeForMatch(line);
            if (!normalized.isEmpty()) {
                knownNormalized.add(normalized);
            }
        }
        String statePrefix = resolveStatePrefix(knownNormalized);

        boolean changed = false;
        for (int i = 0; i < lore.size(); i++) {
            String normalized = normalizeForMatch(lore.get(i));
            if (!isStateLine(normalized, knownNormalized, statePrefix)) {
                continue;
            }
            lore.remove(i);
            i--;
            changed = true;
        }

        if (changed) {
            meta.setLore(lore.isEmpty() ? null : lore);
        }
    }

    private static boolean isStateLine(String normalized, Set<String> knownNormalized, String prefix) {
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        if (knownNormalized.contains(normalized)) {
            return true;
        }
        return prefix != null && !prefix.isBlank() && normalized.startsWith(prefix);
    }

    private static String resolveStatePrefix(Set<String> knownNormalized) {
        for (String line : knownNormalized) {
            int idx = Math.max(line.indexOf(':'), line.indexOf('：'));
            if (idx > 0) {
                return line.substring(0, idx + 1);
            }
        }
        return "";
    }

    private List<String> knownRenderedStateLines() {
        List<String> list = new ArrayList<>(6);
        addIfNotNull(list, plugin.level0);
        addIfNotNull(list, plugin.level1);
        addIfNotNull(list, plugin.level2);
        addIfNotNull(list, plugin.level3);
        addIfNotNull(list, plugin.level4);
        addIfNotNull(list, plugin.level5);
        return list;
    }

    private static void addIfNotNull(List<String> list, String value) {
        if (value != null) {
            list.add(colorize(value));
        }
    }

    private long getMaxTime(ItemStack stack, NBTItem nbt) {
        long maxTime = 0L;
        Integer vanillaTime = plugin.vanilla.get(stack.getType());
        if (vanillaTime != null) {
            maxTime = vanillaTime;
        }

        if (nbt.hasTag("itemsadder")) {
            NBTCompound itemsAdder = nbt.getCompound("itemsadder");
            if (itemsAdder != null && itemsAdder.hasTag("id") && itemsAdder.hasTag("namespace")) {
                String namespaced = itemsAdder.getString("namespace") + ":" + itemsAdder.getString("id");
                Integer iaTime = plugin.itemsAdder.get(namespaced);
                if (iaTime != null) {
                    maxTime = iaTime;
                }
            }
        }
        return maxTime;
    }

    private static String colorize(String input) {
        if (input == null) {
            return null;
        }
        String expanded = expandInlineHex(input);
        return ChatColor.translateAlternateColorCodes('&', expanded);
    }

    private static String expandInlineHex(String input) {
        Matcher matcher = HEX_INLINE.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1).toUpperCase(Locale.ROOT);
            String replacement = "&x"
                    + "&" + hex.charAt(0)
                    + "&" + hex.charAt(1)
                    + "&" + hex.charAt(2)
                    + "&" + hex.charAt(3)
                    + "&" + hex.charAt(4)
                    + "&" + hex.charAt(5);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String normalizeForMatch(String value) {
        if (value == null) {
            return "";
        }
        String stripped = ChatColor.stripColor(value);
        if (stripped == null) {
            stripped = value;
        }
        return stripped.trim().toLowerCase(Locale.ROOT);
    }

    private record StateResult(int stateIndex, String rawLore) {
    }
}
