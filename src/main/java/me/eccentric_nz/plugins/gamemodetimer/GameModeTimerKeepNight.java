package me.eccentric_nz.plugins.gamemodetimer;

import org.bukkit.World;

public class GameModeTimerKeepNight {

    private GameModeTimer plugin;

    public GameModeTimerKeepNight(GameModeTimer plugin) {
        this.plugin = plugin;
    }

    public void timechk() {
        for (World w : plugin.gmtWorlds) {
            if (plugin.getConfig().getBoolean("worlds." + w.getName() + ".keep_night")
                    && plugin.gmtPlayerLimits.containsKey(w.getName())
                    && plugin.gmtHasSwitched.contains(w.getName())) {
                if (plugin.gmtPlayerLimits.get(w.getName()) > 0) {
                    Long now = w.getTime();
                    Long dusk = 21500L;
                    Long dawn = plugin.getConfig().getLong("worlds." + w.getName() + ".time") + 1000;
                    if (now < dawn || now > dusk) {
                        // set the time to dawn
                        w.setTime(dawn);
                    }
                }
            }
        }
    }
}