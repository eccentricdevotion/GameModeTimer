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

    private final GameModeTimer plugin;

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
                int num = (plugin.gmtPlayerCount.containsKey(fromw)) ? plugin.gmtPlayerCount.get(fromw) : 0;
                if (num > 0) {
                    plugin.gmtPlayerCount.put(fromw, num - 1);
                    p.sendMessage("You left, player count is now: " + plugin.gmtPlayerCount.get(fromw));
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
            if (plugin.gmtPlayerCount.containsKey(tow)) {
                num = plugin.gmtPlayerCount.get(tow);
            }
            p.sendMessage("Player count in this world is: " + num);
            if (num < limit && !plugin.gmtHasSwitched.contains(tow)) {
                if (num == 0 && plugin.getConfig().getBoolean("worlds." + tow + ".set_morning")) {
                    //first player in world set time to 0
                    event.getTo().getWorld().setTime(0);
                }
                plugin.gmtPlayerCount.put(tow, num + 1);
                p.sendMessage("You joined, player count is now: " + plugin.gmtPlayerCount.get(tow));
                p.sendMessage(plugin.MY_PLUGIN_NAME + "Welcome to " + tow + ". Your game mode will be switched to " + plugin.getConfig().getString("worlds." + tow + ".gamemode") + " at " + plugin.getConfig().getString("worlds." + tow + ".time") + ". We suggest you get busy!");
            } else {
                event.setCancelled(true);
                if (plugin.gmtHasSwitched.contains(tow)) {
                    p.sendMessage(plugin.MY_PLUGIN_NAME + "We're sorry but " + tow + " has has already switched game modes, try joining again later!");
                } else {
                    p.sendMessage(plugin.MY_PLUGIN_NAME + "We're sorry but " + tow + " has reached the maximum number of players!");
                }
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
        if (plugin.gmtPlayerCount.containsKey(w) && (plugin.gmtPlayerCount.get(w) - 1) == 0 && plugin.gmtHasSwitched.contains(w)) {
            p.sendMessage("You were the last man standing!");
            plugin.gmtHasSwitched.remove(w);
            plugin.gmtPlayerCount.remove(w);
            if (plugin.getConfig().getBoolean("worlds." + w + ".keep_leaderboard")) {
                Long now = p.getLocation().getWorld().getTime();
                Long time = now - plugin.gmtLastManStandingStart.get(w);
                // write time to leaderboard
                plugin.gmtLeaderboard.put(time, p.getName());
            }
        } else if (plugin.gmtPlayerCount.containsKey(w)) {
            int num = plugin.gmtPlayerCount.get(w);
            plugin.gmtPlayerCount.put(w, num - 1);
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
        if (plugin.gmtPlayerCount.containsKey(w)) {
            int num = plugin.gmtPlayerCount.get(w);
            plugin.gmtPlayerCount.put(w, num - 1);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String w = p.getLocation().getWorld().getName();
        if (plugin.gmtPlayerCount.containsKey(w)) {
            int num = plugin.gmtPlayerCount.get(w);
            plugin.gmtPlayerCount.put(w, num + 1);
        }
    }
}
