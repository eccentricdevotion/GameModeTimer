package me.eccentric_nz.plugins.gamemodetimer;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameModeTimerListener implements Listener {

    private GameModeTimer plugin;

    public GameModeTimerListener(GameModeTimer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        String fromw = event.getFrom().getName();
        if (plugin.getConfig().getBoolean("worlds." + fromw + ".enabled") && !plugin.getConfig().getString("worlds." + fromw + ".gamemode").equalsIgnoreCase("SURVIVAL")) {
            p.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        String w = p.getLocation().getWorld().getName();
        if (plugin.getConfig().getBoolean("worlds." + w + ".enabled")) {
            plugin.gmtAfterlife.put(p.getName(), w);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        String spawnWorld = event.getRespawnLocation().getWorld().getName();
        if (plugin.gmtAfterlife.containsKey(p.getName()) && spawnWorld.equalsIgnoreCase(plugin.gmtAfterlife.get(p.getName()))) {
            p.setGameMode(GameMode.SURVIVAL);
        }
    }
}