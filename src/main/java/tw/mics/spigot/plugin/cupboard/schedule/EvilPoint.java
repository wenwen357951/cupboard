package tw.mics.spigot.plugin.cupboard.schedule;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Material;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.data.EvilPointData;

public class EvilPoint {
    Cupboard plugin;
    Runnable runnable;
    EvilPointData evilpoint;
    LinkedHashMap<UUID, Integer> limit;
    
    int schedule_id;
    public EvilPoint(Cupboard i){
        this.plugin = i;
        evilpoint = i.evilpoint;
        setupRunnable();
        limit = new LinkedHashMap<UUID, Integer>();
    }
    
    public void removeRunnable(){
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
        this.plugin.logDebug("Evilpoint timer task removed");
    }
    
    private void setupRunnable(){
        runnable = new Runnable(){
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(p->{
                    if(!limit.containsKey(p.getUniqueId())) limit.put(p.getUniqueId(), 0);
                    if(
                            !p.getInventory().contains(Material.TNT) &&
                            !p.getEnderChest().contains(Material.TNT) &&
                            limit.get(p.getUniqueId()) < 60 &&
                            evilpoint.getEvil(p) > 0
                    ){
                        evilpoint.minusEvil(p, 1);
                        limit.put(p.getUniqueId(), limit.get(p.getUniqueId())+1);
                    }
                });
                evilpoint.scoreboardUpdate();
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 6000);
        this.plugin.logDebug("Evilpoint timer task added");
    }
}
