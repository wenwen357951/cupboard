package tw.mics.spigot.plugin.cupboard.schedule;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;

public class Compass {
    Cupboard plugin;
    Runnable runnable;
    int schedule_id;
    public Compass(Cupboard i){
        this.plugin = i;
        setupRunnable();
    }
    
    public void removeRunnable(){
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
        this.plugin.logDebug("Compass check timer task removed");
    }
    
    private void setupRunnable(){
        runnable = new Runnable(){
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(p->{
                    if(
                            p.getInventory().contains(Material.COMPASS) ||
                            p.getInventory().getItemInOffHand().getType() == Material.COMPASS
                            ){
                        Player nearest_player = null;
                        double nearest_dist = 60000000;
                        for(Player t:plugin.getServer().getOnlinePlayers()){
                            if(p==t)continue;
                            if(t.getGameMode() != GameMode.SURVIVAL)continue;
                            if(t.isOp())continue;
                            if(p.getWorld() == t.getWorld()){
                                double dist = p.getLocation().distance(t.getLocation());
                                if(dist < nearest_dist){
                                    nearest_player = t;
                                    nearest_dist = dist;
                                }
                            }
                        }
                        if(nearest_player == null){
                            p.setCompassTarget(p.getLocation().add(getRandom(100), 0, getRandom(100)));
                        } else {
                            int dev = Config.COMPASS_DEVIATION.getInt();
                            p.setCompassTarget(nearest_player.getLocation().add(dev, 0, getRandom(dev)));
                        }
                        //plugin.getServer().getLogger().info(String.format(" x:%.2f z:%.2f", p.getCompassTarget().getX(), p.getCompassTarget().getZ()));
                    }
                });
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, Config.COMPASS_UPDATE_TIME.getInt());
        this.plugin.logDebug("Compass check timer task added");
    }
    private double getRandom(int size){
        Random random = new Random();
        return random.nextInt(size*2) - size;
    }
}
