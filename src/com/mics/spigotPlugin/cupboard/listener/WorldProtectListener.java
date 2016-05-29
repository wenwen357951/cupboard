package com.mics.spigotPlugin.cupboard.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.inventory.ItemStack;

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
	
	//豬人會掉落地獄疙瘩
	@EventHandler
	public void onPigZombieDeath(EntityDeathEvent e){
		if(!Config.PIGZOMBIE_DROP_NETHER_WART.getBoolean())return;
		LivingEntity entity = e.getEntity();
        World world = entity.getWorld();
        ArrayList<ItemStack> will_remove = new ArrayList<ItemStack>();
        boolean in_nether = world.getEnvironment().equals(World.Environment.NETHER);
        int add_nether_wart_count = 0;
        if (in_nether && entity instanceof PigZombie){
        	for(ItemStack i : e.getDrops()){
        		if(i.getType().equals(Material.GOLD_NUGGET)){
        			if(Config.PIGZOMBIE_DROP_NETHER_WART_PERCENT.getDouble() >= Math.random()){
            			will_remove.add(i);
        			}
        		}
        	}
        	//add
        	for(int i = 0; i < add_nether_wart_count; i++){
        	}
        	//remove
        	for(ItemStack i: will_remove){
        		e.getDrops().remove(i);
        		ItemStack ns = new ItemStack(Material.NETHER_STALK);
        		ns.setAmount(i.getAmount());
        		e.getDrops().add(ns);
        	}
        }
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
