package me.eccentric_nz.plugins.gamemodetimer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameModeTimer extends JavaPlugin implements Listener {

    protected GameModeTimer plugin;
    public List<World> gmtWorlds;
    public String MY_PLUGIN_NAME;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    GameModeTimerListener gmtListener;
    GameModeTimerKeepNight gmtTimeKeeper;
    private GameModeTimerCommands commando;
    public HashMap<String, String> gmtAfterlife = new HashMap<String, String>();
    public HashMap<String, Integer> gmtPlayerLimits = new HashMap<String, Integer>();
    public HashMap<String, Long> gmtLastManStandingStart = new HashMap<String, Long>();
    public TreeMap<Long, String> gmtLeaderboard = new TreeMap<Long, String>();
    public List<String> gmtLimitReached = new ArrayList<String>();
    public List<String> gmtHasSwitched = new ArrayList<String>();

    @Override
    public void onDisable() {
        // save the leaderboard
        saveLeaderboard();
    }

    @Override
    public void onEnable() {
        plugin = this;
        MY_PLUGIN_NAME = "[GameModeTimer] ";
        this.saveDefaultConfig();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                doWorldConfig();
                gmtWorlds = getWorlds();
                gmtListener = new GameModeTimerListener(plugin);
                pm.registerEvents(gmtListener, plugin);
                commando = new GameModeTimerCommands(plugin);
                getCommand("gmt").setExecutor(commando);
                gmtTimeKeeper = new GameModeTimerKeepNight(plugin);
                loadLeaderboard();
            }
        }, 10L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                timer();
            }
        }, 20L, 10L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                gmtTimeKeeper.timechk();
            }
        }, 60L, 600L);
    }

    private void timer() {
        for (World w : gmtWorlds) {
            Long now = w.getTime();
            Long time = getConfig().getLong("worlds." + w.getName() + ".time");
            GameMode change = GameMode.valueOf(getConfig().getString("worlds." + w.getName() + ".gamemode"));
            if (now >= time) {
                if (!gmtHasSwitched.contains(w.getName())) {
                    gmtHasSwitched.add(w.getName());
                    gmtLastManStandingStart.put(w.getName(), now);
                }
                // get players in this world
                List<Player> players = w.getPlayers();
                for (Player p : players) {
                    GameMode gm = p.getGameMode();
                    if (!gm.equals(change) && !p.hasPermission("gamemodetimer.bypass") && p.isOnline()) {
                        p.setGameMode(change);
                    }
                }
            }
        }
    }

    private void doWorldConfig() {
        // add worlds
        List<World> worlds = Bukkit.getServer().getWorlds();
        for (World w : worlds) {
            String worldname = "worlds." + w.getName();
            if (!getConfig().contains(worldname)) {
                getConfig().set(worldname + ".enabled", false);
                getConfig().set(worldname + ".gamemode", "ADVENTURE");
                getConfig().set(worldname + ".time", 12500);
                getConfig().set(worldname + ".players", 20);
                getConfig().set(worldname + ".keep_night", true);
                System.out.println(MY_PLUGIN_NAME + " Added '" + w.getName() + "' to config.");
            }
        }
        // now remove worlds that may have been deleted
        Set<String> cWorlds = getConfig().getConfigurationSection("worlds").getKeys(false);
        for (String cw : cWorlds) {
            if (getServer().getWorld(cw) == null) {
                getConfig().set("worlds." + cw, null);
                System.out.println(MY_PLUGIN_NAME + " Removed '" + cw + " from config");
            }
        }
        saveConfig();
    }

    private List<World> getWorlds() {
        List<World> list = new ArrayList<World>();
        Set<String> worlds = getConfig().getConfigurationSection("worlds").getKeys(false);
        for (String w : worlds) {
            if (Bukkit.getServer().getWorld(w) != null && getConfig().getBoolean("worlds." + w + ".enabled")) {
                System.out.println(MY_PLUGIN_NAME + "Enabling " + w + " for timed GameMode switching");
                list.add(getServer().getWorld(w));
            }
        }
        return list;
    }

    private void loadLeaderboard() {
        File file = new File(getDataFolder() + File.separator + "leaderboard.txt");
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(":");
                    Long time = 0L;
                    try {
                        time = Long.parseLong(split[0]);
                    } catch (NumberFormatException nfe) {
                    }
                    gmtLeaderboard.put(time, split[1]);
                }
                br.close();
                getServer().getConsoleSender().sendMessage(MY_PLUGIN_NAME + "Loading chunks from chunks.txt!");
            } catch (IOException e) {
                getServer().getConsoleSender().sendMessage(MY_PLUGIN_NAME + "Could not create and write to chunks.txt! " + e.getMessage());
            }
        }
    }

    private void saveLeaderboard() {
        if (gmtLeaderboard.size() > 0) {
            String file = getDataFolder() + File.separator + "leaderboard.txt";
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
                for (Map.Entry<Long, String> entry : gmtLeaderboard.entrySet()) {
                    String line = entry.getKey() + ":" + entry.getValue();
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                getServer().getConsoleSender().sendMessage(MY_PLUGIN_NAME + "Could not create and write to leaderboard.txt! " + e.getMessage());
            }
        }
    }
}
