package tw.mics.spigot.plugin.cupboard.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public class TNTExplosionListener extends MyListener {
    
    class ChanceTureTo {
        ChanceTureTo(Double c, Material t){
            chance = c;
            turn_to = t;
        }
        public Double chance;
        public Material turn_to;
    }
    
    Map<Material, ChanceTureTo> other_chance_destory_block_list;
    
	public TNTExplosionListener(Cupboard instance)
	{
		super(instance);
		other_chance_destory_block_list = new HashMap<Material, ChanceTureTo>();
	    for(String str : Config.TNT_BREAKCHANCE.getStringList()){
	        String[] strs = str.split(":");
            other_chance_destory_block_list.put(Material.valueOf(strs[0]), 
                    new ChanceTureTo( Double.valueOf(strs[1]), Material.valueOf(strs[2])));
	    }
	}

	//爆炸後
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event){
        if(!(event.getEntity() instanceof TNTPrimed)) return;
    	event.blockList().clear();
    	Location loc = event.getLocation();
    	Integer r = Config.TNT_EXPLOSION_RADIUS.getInt();
        for(int x = -r; x <= r; x++){
            for(int y = -r; y <= r; y++){
                for(int z = -r; z <= r; z++){
            	    Block b = loc.clone().add(x, y, z).getBlock();
            	    if(b.getType() == Material.TNT){
            	        b.setType(Material.AIR);
            	        Util.setUpTNT(b.getLocation().add(0.5, 0, 0.5));
            	    }
            	    ChanceTureTo ctt = other_chance_destory_block_list.get(b.getType());
            	    if(ctt != null){
            	        if(ctt.chance > new Random().nextFloat()){
            	            b.setType(ctt.turn_to);
            	        }
            	    } else {
            	        b.breakNaturally();
            	    }
                }
            }
    	}
    }
    
    //爆炸前
    @EventHandler
    public void onTNTPrime(ExplosionPrimeEvent e){
        if(!(e.getEntity() instanceof TNTPrimed)) return;
        e.setRadius(0);
    }
    
    //發射器禁用
    @EventHandler
    public void onDispense(BlockDispenseEvent event){
        if(event.getItem().getType() == Material.TNT){
            event.setCancelled(true);
        }
    }
    
    //禁止使用釣魚竿
    @EventHandler
    public void onFishingTNT(PlayerFishEvent event){
        if(event.getCaught() instanceof TNTPrimed){
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run() {
                    event.getCaught().setVelocity(new Vector());
                }
            });
        }
    }
}
