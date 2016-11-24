package tw.mics.spigot.plugin.cupboard.schedule;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class GoldBlockCheckerSchedule {
    Cupboard plugin;
    Runnable runnable;
    Iterator<? extends Player> iter;

    int schedule_id;
    int part;

    public GoldBlockCheckerSchedule(Cupboard i) {
        this.plugin = i;
        part = 0;
        iter = Bukkit.getServer().getOnlinePlayers().iterator();
        setupRunnable();
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            public void run() {
                int player_count = Bukkit.getServer().getOnlinePlayers().size();
                int delay = 200;
                if(Bukkit.getServer().getOnlinePlayers().size() > 0){
                    if (!iter.hasNext()) {
                        iter = Bukkit.getServer().getOnlinePlayers().iterator();
                    }
                    Player p = iter.next();
                    plugin.cupboards.cleanNotExistCupboard(p);
                    delay = 200 / player_count;
                }
                if (delay < 1)
                    delay = 1;
                schedule_id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, runnable, 0);
        this.plugin.logDebug(this.getClass().getName() + " timer task removed");
    }
}
