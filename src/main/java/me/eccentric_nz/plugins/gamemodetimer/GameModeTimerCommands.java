package me.eccentric_nz.plugins.gamemodetimer;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameModeTimerCommands implements CommandExecutor {

    private GameModeTimer plugin;

    public GameModeTimerCommands(GameModeTimer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("gmt")) {
            if (args[0].equalsIgnoreCase("leaderboard")) {
                if (plugin.gmtLeaderboard.size() > 0) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + "Leaderboard Top 5");
                    int i = 0;
                    for (Map.Entry<Long, String> entry : plugin.gmtLeaderboard.descendingMap().entrySet()) {
                        if (i++ < 6) {
                            sender.sendMessage(i + ". " + entry.getKey() + " : " + entry.getValue());
                        }
                    }
                } else {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + "There are no Leaderboard stats yet");
                }
                return true;
            }
            if (!sender.hasPermission("gamemodetimer.admin")) {
                sender.sendMessage(plugin.MY_PLUGIN_NAME + "You do not have permission to use this command!");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(plugin.MY_PLUGIN_NAME + "Not enough command arguments!");
                return false;
            }
            Set<String> worlds = plugin.getConfig().getConfigurationSection("worlds").getKeys(false);
            if (!worlds.contains(args[1])) {
                sender.sendMessage(plugin.MY_PLUGIN_NAME + "Could not find world in the config!");
                return true;
            }
            if (args[0].equalsIgnoreCase("toggle")) {
                String tf = args[2].toLowerCase();
                if (!tf.equals("true") && !tf.equals("false")) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + ChatColor.RED + "The last argument must be true or false!");
                    return false;
                }
                plugin.getConfig().set("worlds." + args[1] + ".enabled", Boolean.valueOf(tf));
                if (tf.equals("true")) {
                    plugin.gmtWorlds.add(Bukkit.getServer().getWorld(args[1]));
                } else {
                    plugin.gmtWorlds.remove(Bukkit.getServer().getWorld(args[1]));
                }
            }
            if (args[0].equalsIgnoreCase("time")) {
                Long time;
                try {
                    time = Long.parseLong(args[2]);
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + ChatColor.RED + "The last argument must be a number!");
                    return false;
                }
                plugin.getConfig().set("worlds." + args[1] + ".time", time);
            }
            if (args[0].equalsIgnoreCase("gamemode")) {
                if (!Arrays.asList(GameMode.values()).contains(GameMode.valueOf(args[2]))) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + ChatColor.RED + "Doesn't recognise that GameMode!");
                    return false;
                }
                plugin.getConfig().set("worlds." + args[1] + ".gamemode", GameMode.valueOf(args[2]).toString());
            }
            if (args[0].equalsIgnoreCase("players")) {
                int num;
                try {
                    num = Integer.parseInt(args[2]);
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + ChatColor.RED + "The last argument must be a number!");
                    return false;
                }
                plugin.getConfig().set("worlds." + args[1] + ".players", num);
            }
            if (args[0].equalsIgnoreCase("set_morning") || args[0].equalsIgnoreCase("keep_night") || args[0].equalsIgnoreCase("no_build") || args[0].equalsIgnoreCase("keep_leaderboard")) {
                String tf = args[2].toLowerCase();
                if (!tf.equals("true") && !tf.equals("false")) {
                    sender.sendMessage(plugin.MY_PLUGIN_NAME + ChatColor.RED + "The last argument must be true or false!");
                    return false;
                }
                plugin.getConfig().set("worlds." + args[1] + "." + args[0].toLowerCase(), Boolean.valueOf(tf));
            }
            plugin.saveConfig();
            sender.sendMessage(plugin.MY_PLUGIN_NAME + "Config updated successfully");
            return true;
        }
        return false;
    }
}
