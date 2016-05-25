package com.mics.spigotPlugin.cupboard.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPortalEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Locales;

public class WorldProtectListener implements Listener {
	private Cupboard plugin;

	public WorldProtectListener(Cupboard instance)
	{
	    this.plugin = instance;
	    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	    this.plugin.logDebug("WorldProtectListener Registed.");
	}
	
	//防止除玩家之外之物件透過地獄門傳送
    @EventHandler
    public void onEntityPortal(EntityPortalEvent e){
		if(Config.ANTI_NETHER_DOOR_ENTITY_TELEPORT.getBoolean())e.setCancelled(true);
    }
    
    //防止玩家擋住地獄門
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockNetherDoor(BlockPlaceEvent e){
    	if(!Config.ANTI_NETHER_DOOR_BLOCK.getBoolean())return;
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
    	if(
    			b.getLocation().add(1,0,0).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(-1,0,0).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(0,0,1).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(0,0,-1).getBlock().getType() == Material.PORTAL
    			){
	        if( p != null && b.getType().isSolid()){
	        	if(b.getType() == Material.WOOD_PLATE) return;
	        	if(b.getType() == Material.STONE_PLATE) return;
	        	if(b.getType() == Material.IRON_PLATE) return;
	        	if(b.getType() == Material.GOLD_PLATE) return;
        		p.sendMessage(Locales.DO_NOT_BLOCK_NETHER_DOOR.getString());
        		e.setCancelled(true);
	    	}
    	}
        
    }
}
