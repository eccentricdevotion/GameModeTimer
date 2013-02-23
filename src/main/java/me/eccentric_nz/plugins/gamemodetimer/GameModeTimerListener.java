package me.eccentric_nz.plugins.gamemodetimer;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GameModeTimerListener implements Listener {

    private GameModeTimer plugin;

    public GameModeTimerListener(GameModeTimer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChangeWorldFrom(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        String fromw = event.getFrom().getName();
        if (plugin.getConfig().getBoolean("worlds." + fromw + ".enabled") && !plugin.getConfig().getString("worlds." + fromw + ".gamemode").equalsIgnoreCase("SURVIVAL")) {
            p.setGameMode(GameMode.SURVIVAL);
            if (plugin.getConfig().getInt("worlds." + fromw + ".players") != -1) {
                int num = (plugin.gmtPlayerLimits.containsKey(fromw)) ? plugin.gmtPlayerLimits.get(fromw) : 0;
                if (num > 0) {
                    plugin.gmtPlayerLimits.put(fromw, num - 1);
                    //p.sendMessage("You left, player count is now: " + plugin.gmtPlayerLimits.get(fromw));
                }
                if (num == 0 && plugin.gmtHasSwitched.contains(fromw)) {
                    plugin.gmtHasSwitched.remove(fromw);
                    plugin.gmtLimitReached.remove(fromw);
                }
            }
        }
    }

    @EventHandler
    public void onTeleportToWorld(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        String tow = event.getTo().getWorld().getName();
        if (plugin.getConfig().getBoolean("worlds." + tow + ".enabled") && plugin.getConfig().getInt("worlds." + tow + ".players") != -1) {
            int num = 0;
            int limit = plugin.getConfig().getInt("worlds." + tow + ".players");
            if (plugin.gmtPlayerLimits.containsKey(tow)) {
                num = plugin.gmtPlayerLimits.get(tow);
            }
            //p.sendMessage("Player count in this world is: " + num);
            if (num < limit && !plugin.gmtLimitReached.contains(tow)) {
                if (num == 0 && plugin.getConfig().getBoolean("worlds." + tow + ".set_morning")) {
                    //first player in world set time to 0
                    event.getTo().getWorld().setTime(0);
                }
                plugin.gmtPlayerLimits.put(tow, num + 1);
                if (num + 1 == limit) {
                    plugin.gmtLimitReached.add(tow);
                }
                //p.sendMessage("You joined, player count is now: " + plugin.gmtPlayerLimits.get(tow));
                p.sendMessage(plugin.MY_PLUGIN_NAME + "Welcome to " + tow + ". Your game mode will be switched to " + plugin.getConfig().getString("worlds." + tow + ".gamemode") + " at " + plugin.getConfig().getString("worlds." + tow + ".time") + ". We suggest you get busy!");
            } else {
                event.setCancelled(true);
                p.sendMessage(plugin.MY_PLUGIN_NAME + "We're sorry but " + tow + " has reached the maximum number of players!");
            }
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String w = p.getLocation().getWorld().getName();
        if (plugin.gmtPlayerLimits.containsKey(w)) {
            int num = plugin.gmtPlayerLimits.get(w);
            plugin.gmtPlayerLimits.put(w, num - 1);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String w = p.getLocation().getWorld().getName();
        if (plugin.gmtPlayerLimits.containsKey(w)) {
            int num = plugin.gmtPlayerLimits.get(w);
            plugin.gmtPlayerLimits.put(w, num + 1);
        }
    }
}
