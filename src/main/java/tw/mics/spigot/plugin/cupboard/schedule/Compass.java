package tw.mics.spigot.plugin.cupboard.schedule;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;

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
                            p.setCompassTarget(nearest_player.getLocation().add(getRandom(25), 0, getRandom(25)));
                        }
                        //plugin.getServer().getLogger().info(String.format(" x:%.2f z:%.2f", p.getCompassTarget().getX(), p.getCompassTarget().getZ()));
                    }
                });
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 100);
        this.plugin.logDebug("Compass check timer task added");
    }
    private double getRandom(int size){
        Random random = new Random();
        return random.nextInt(size*2) - size;
    }
}
