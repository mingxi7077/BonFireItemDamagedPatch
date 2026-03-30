package com.puddingkc;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class BonfireListeners implements Listener {

    private final BonfireItemDamaged plugin;
    private final BonfireUtils bonfireUtils;

    public BonfireListeners(BonfireItemDamaged plugin) {
        this.plugin = plugin;
        this.bonfireUtils = new BonfireUtils(plugin);
    }

    private boolean shouldSkip(Player player) {
        return player.hasPermission("bonfire.item.bypass") || player.getGameMode() == GameMode.SPECTATOR;
    }

    private void scheduleInventoryCheck(Player player, long delayTicks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            bonfireUtils.checkInventory(player.getInventory());
            player.updateInventory();
        }, delayTicks);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (shouldSkip(player) || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (shouldSkip(player)) {
            return;
        }
        scheduleInventoryCheck(player, 20L);
    }
}
