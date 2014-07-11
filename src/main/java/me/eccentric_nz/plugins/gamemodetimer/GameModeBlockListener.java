package me.eccentric_nz.plugins.gamemodetimer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GameModeBlockListener implements Listener {

    private final GameModeTimer plugin;

    public GameModeBlockListener(GameModeTimer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String w = event.getPlayer().getLocation().getWorld().getName();
        boolean no_build = plugin.getConfig().getBoolean("worlds." + w + ".no_build");
        if (no_build && plugin.gmtHasSwitched.contains(w)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String w = event.getPlayer().getLocation().getWorld().getName();
        boolean no_build = plugin.getConfig().getBoolean("worlds." + w + ".no_build");
        if (no_build && plugin.gmtHasSwitched.contains(w)) {
            event.setCancelled(true);
        }
    }
}
