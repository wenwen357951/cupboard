package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;

public class TNTExplosionListener extends MyListener {
	public TNTExplosionListener(Cupboard instance)
	{
		super(instance);
	    new ArrayList<String>();
	}
	
	final Material[] high_blast_resistance_block = {
	        Material.ANVIL,
	        Material.ENCHANTMENT_TABLE,
	        Material.ENDER_CHEST,
	};

	//爆炸後
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event){
    	if(!event.getEntity().getType().equals(EntityType.PRIMED_TNT)) return;
    	//this.plugin.logDebug("TNT Explision");
    	   Location tnt_location = event.getEntity().getLocation();
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
						   }
						   if(Arrays.asList(high_blast_resistance_block).contains(exploded_block.getType())){
						       if(Config.TNT_OTHERS_HIGH_BLAST_RESISTANCE_BREAK_PROBABILITY.getDouble()>=Math.random()){
                                   exploded_block.setType(Material.AIR);
                               }
						   }
					   }
		 	 	   }
		   double distance_limit = Config.TNT_EXPLOSION_DISTANCE_LIMIT.getDouble();
		   if(distance_limit != -1){
		       Iterator<Block> itr = event.blockList().iterator();
		       while(itr.hasNext()){
		           Block b = itr.next();
		           if(tnt_location.distance(b.getLocation()) > distance_limit){
		               itr.remove();
		           }
		       }
		   }
    	
    }
    
    //爆炸前
    @EventHandler
    public void onTNTPrime(ExplosionPrimeEvent e){
    	//is a primed TNT?
    	if(e.getEntity() instanceof TNTPrimed){
    		e.setRadius((float) Config.TNT_EXPLOSION_RADIUS.getDouble());//get tnt location
	 	   Location tnt_location = e.getEntity().getLocation();
	    	//Remove water block
		   double obs_bk_radius = Config.TNT_BREAK_RADIUS.getDouble();
		   int obs_bk_radius_ceil = (int) Math.ceil(obs_bk_radius);
		   for(int i = (int)-obs_bk_radius_ceil; i < obs_bk_radius_ceil; i++)
			   for(int j = (int)-obs_bk_radius_ceil; j < obs_bk_radius_ceil; j++)
		 	 	   for(int k = (int)-obs_bk_radius_ceil; k < obs_bk_radius_ceil; k++){
		 	 		   Location exploded_block_location = tnt_location.clone().add(i,j,k);
					   if ( tnt_location.distance(exploded_block_location) < obs_bk_radius ){
			 	 		   Block exploded_block = exploded_block_location.getBlock();
						   if(exploded_block.getType().equals(Material.WATER) || exploded_block.getType().equals(Material.STATIONARY_WATER)){
							   if(Config.TNT_WATER_BREAK_PROBABILITY.getDouble()>=Math.random()){
								   exploded_block.setType(Material.AIR);
	 						   }
						   } else if(exploded_block.getType().equals(Material.LAVA) || exploded_block.getType().equals(Material.STATIONARY_LAVA)){
							   if(Config.TNT_LAVA_BREAK_PROBABILITY.getDouble()>=Math.random()){
								   exploded_block.setType(Material.AIR);
	 						   }
						   }
					   }
		 	 	   }
    	}
    }
}
