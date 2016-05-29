package com.mics.spigotPlugin.cupboard.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Util;

public class TNTExplosionListener  implements Listener {
	private Cupboard plugin;
	private List<String> cant_flow_liquid;
	public TNTExplosionListener(Cupboard instance)
	{
	    this.plugin = instance;
	    this.cant_flow_liquid = new ArrayList<String>();
	    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	    this.plugin.logDebug("TNTExplosionListener Registed.");
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event){
    	if(!event.getEntity().getType().equals(EntityType.PRIMED_TNT)) return;
    	//this.plugin.logDebug("TNT Explision");
    	
    	//get tnt location
 	   Location tnt_location =event.getEntity().getLocation();
    	//Remove can't destory block
	   double obs_bk_radius = Config.TNT_BREAK_RADIUS.getDouble();
	   int obs_bk_radius_ceil = (int) Math.ceil(obs_bk_radius);
	   for(int i = (int)-obs_bk_radius_ceil; i < obs_bk_radius_ceil; i++)
		   for(int j = (int)-obs_bk_radius_ceil; j < obs_bk_radius_ceil; j++)
	 	 	   for(int k = (int)-obs_bk_radius_ceil; k < obs_bk_radius_ceil; k++){
	 	 		   Location exploded_block_location = tnt_location.clone().add(i,j,k);
				   if ( tnt_location.distance(exploded_block_location) < obs_bk_radius ){
		 	 		   Block exploded_block = exploded_block_location.getBlock();
					   if(exploded_block.getType().equals(Material.OBSIDIAN)){
						   if(Config.TNT_OBSIDIAN_BREAK_PROBABILITY.getDouble()>=Math.random()){
							   exploded_block.setType(Material.getMaterial(Config.TNT_OBSIDIAN_BREAK_TO.getString()));
						   }
					   } else if(exploded_block.getType().equals(Material.WATER)  ||  exploded_block.getType().equals(Material.STATIONARY_WATER)){
						   if(Config.TNT_WATER_BREAK_PROBABILITY.getDouble()>=Math.random()){
							   exploded_block.setType(Material.AIR);
							   addToNoFlow(exploded_block_location);
 						   }
					   } else if(exploded_block.getType().equals(Material.LAVA)){
						   if(Config.TNT_LAVA_BREAK_PROBABILITY.getDouble()>=Math.random()){
							   exploded_block.setType(Material.OBSIDIAN);
							   addToNoFlow(exploded_block_location);
 						   }
					   } else if(exploded_block.getType().equals(Material.STATIONARY_LAVA)){
						   if(Config.TNT_LAVA_BREAK_PROBABILITY.getDouble()>=Math.random()){
							   exploded_block.setType(Material.COBBLESTONE);
							   addToNoFlow(exploded_block_location);
 						   }
					   }
				   }
	 	 	   }
    }
	private void addToNoFlow(Location l){
		cant_flow_liquid.add(Util.LocToString(l));
    	this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
			@Override
			public void run() {
				cant_flow_liquid.remove(Util.LocToString(l));
			}
    	}, 100);
    }
    

    @EventHandler
    public void onWaterFlow(BlockFromToEvent e){
    	if(this.cant_flow_liquid.contains(Util.LocToString(e.getToBlock().getLocation()))){
        	this.plugin.logDebug(e.getToBlock().toString());
    		e.setCancelled(true);
    	}
    }
}
