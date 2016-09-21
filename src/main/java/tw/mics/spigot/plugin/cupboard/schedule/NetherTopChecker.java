package tw.mics.spigot.plugin.cupboard.schedule;

import org.bukkit.World.Environment;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class NetherTopChecker {
    Cupboard plugin;
    Runnable runnable;
    int schedule_id;
    public NetherTopChecker(Cupboard i){
        this.plugin = i;
        setupRunnable();
    }
    
    public void removeRunnable(){
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
        this.plugin.logDebug("Nether top checke task removed");
    }
    
    private void setupRunnable(){
        runnable = new Runnable(){
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(p->{
                    if(
                            p.getWorld().getEnvironment() == Environment.NETHER &&
                            p.getLocation().getY() > 128
                            ){
                        p.damage(10);
                    }
                });
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 5);
        this.plugin.logDebug("Nether top checke task added");
    }
}
