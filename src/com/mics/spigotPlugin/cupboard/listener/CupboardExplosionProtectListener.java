package com.mics.spigotPlugin.cupboard.listener;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.Data;
import com.mics.spigotPlugin.cupboard.utils.Config;

public class CupboardExplosionProtectListener extends MyListener {
	private Data data;

	public CupboardExplosionProtectListener(Cupboard instance)
	{
	    super(instance);
	    this.data = this.plugin.data;
	}
	
    //TNT or Creeper爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event){
    	boolean remove_flag = false;
    	if(
			(
				Config.ANTI_TNT_EXPLOSION.getBoolean() &&
				event.getEntity().getType().equals(EntityType.PRIMED_TNT)
			) || (
				Config.ANTI_OTHERS_EXPLOSION.getBoolean() &&
				!event.getEntity().getType().equals(EntityType.PRIMED_TNT)
			)
		) remove_flag = true;
    	
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(remove_flag && data.checkIsLimit(block)){
				event.blockList().remove(block);
    		} else if(block.getType() == Material.GOLD_BLOCK){ //金磚不會被炸壞
    			event.blockList().remove(block);
    		}
    	}
    }
    
    //插件爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(BlockExplodeEvent event){
    	if(Config.ANTI_OTHERS_EXPLOSION.getBoolean()){
	    	for(Block block : new ArrayList<Block>(event.blockList())){
	    		if(data.checkIsLimit(block)){
	    			event.blockList().remove(block);
	    		} else if(block.getType() == Material.GOLD_BLOCK){ //金磚不會被炸壞
	    			event.blockList().remove(block);
	    		}
	    	}
    	}
    }

    //防止Armor stand被炸毀
    @EventHandler
    public void onArmorStandExplosion(EntityDamageByEntityEvent e){
    	if(
    		e.getEntity().getType() == EntityType.ARMOR_STAND &&
    		e.getCause() == DamageCause.ENTITY_EXPLOSION
		){
    		if(
				(Config.ANTI_TNT_EXPLOSION.getBoolean() && e.getDamager().getType() == EntityType.PRIMED_TNT) ||
				(Config.ANTI_OTHERS_EXPLOSION.getBoolean() && e.getDamager().getType() != EntityType.PRIMED_TNT)
			)
    			if(this.plugin.data.checkIsLimit(e.getEntity().getLocation().getBlock()))
    				e.setCancelled(true);
    	}
    }
    
    //插件爆炸
    @EventHandler
    public void onArmorStandExplosion(EntityDamageEvent e){
    	if(
    		e.getEntity().getType() == EntityType.ARMOR_STAND &&
			e.getCause() == DamageCause.BLOCK_EXPLOSION &&
    		this.plugin.data.checkIsLimit(e.getEntity().getLocation().getBlock())
		){
    		e.setCancelled(true);
    	}
    }
    
    //防止Hanging類物品 被Creeper炸掉 / 被TNT炸掉
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
    	//NEEDFIX -- TNT LIGHT BY ALLOW USER WILL DESTORY HANGING ITEM
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
    	if ((e.getRemover() instanceof Player)) {
    		//TNT Light By USER
    		if(Config.ANTI_TNT_EXPLOSION.getBoolean()){
    			e.setCancelled(true);
    		}
    	} else {
    		if(e.getCause() == RemoveCause.ENTITY){ //by Entity
    			if(
    					(Config.ANTI_TNT_EXPLOSION.getBoolean() && e.getRemover().getType() == EntityType.PRIMED_TNT) ||
    					(Config.ANTI_OTHERS_EXPLOSION.getBoolean() && e.getRemover().getType() != EntityType.PRIMED_TNT)
				)
    			if(this.plugin.data.checkIsLimit(bl)) e.setCancelled(true);
    		}
    	}
	}

    //插件爆炸
    @EventHandler
    public void onHangingBreak(HangingBreakEvent e) {
    	if(!Config.ANTI_OTHERS_EXPLOSION.getBoolean())return;
    	Location bl = e.getEntity().getLocation().getBlock().getLocation();
		if(e.getCause() == RemoveCause.EXPLOSION){
			if(this.plugin.data.checkIsLimit(bl)) e.setCancelled(true);
		}
    }

}
