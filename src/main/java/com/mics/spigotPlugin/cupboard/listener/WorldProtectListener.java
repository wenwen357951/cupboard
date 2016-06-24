package com.mics.spigotPlugin.cupboard.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.config.Config;
import com.mics.spigotPlugin.cupboard.config.Locales;
import com.mics.spigotPlugin.cupboard.utils.Util;

public class WorldProtectListener extends MyListener {
	private List<String> cant_flow_liquid;

	public WorldProtectListener(Cupboard instance)
	{
		super(instance);
	    cant_flow_liquid = new ArrayList<String>();
	}
	
	//更改地獄門搜尋半徑
	//防止地獄門被岩漿封門，如果地獄門被完全封死，將自動清除地獄門附近的東西
	@EventHandler
	public void onNetherPortal(PlayerPortalEvent e){
		if(!Config.WP_NETHER_DOOR_PROTECT_ENABLE.getBoolean())return;
		e.getPortalTravelAgent().setSearchRadius(Config.WP_NETHER_SREACH_RADIUS.getInt());
	}
	
	@EventHandler
	public void onPortalTeleport(PlayerTeleportEvent e){
		if(!Config.WP_NETHER_DOOR_PROTECT_ENABLE.getBoolean())return;
		if(!Config.WP_NETHER_REMOVE_BLOCK.getBoolean())return;
		if( e.getCause() == TeleportCause.NETHER_PORTAL ){
			for(int y = -2; y < 6; y++){
				Location l = e.getTo().clone().add(0,y,0);
				for(int count = 1; count <= 3; count++){
					aroundLavaCleaner(l, count, y<0 ? true : false);
				}
			}
		}
	}
	
	//暫時防止岩漿流動
    @EventHandler
    public void onLavaFlow(BlockFromToEvent e){
		if(!Config.WP_NETHER_REMOVE_BLOCK.getBoolean())return;
    	if(this.cant_flow_liquid.contains(Util.LocToString(e.getToBlock().getLocation()))){
    		e.setCancelled(true);
    	}
    }
	
	private void aroundLavaCleaner(Location location, int count, boolean place_floor){
		lavaClean(location, place_floor);
		location.add(1,0,0);
		lavaClean(location, place_floor);
		for(int i=0; i < count; i++){
			location.add(0,0,1);
			lavaClean(location, place_floor);
		}
		for(int i= -count ; i < count; i++){
			location.add(-1,0,0);
			lavaClean(location, place_floor);
		}
		for(int i= -count; i < count; i++){
			location.add(0,0,-1);
			lavaClean(location, place_floor);
		}
		for(int i= -count ; i < count; i++){
			location.add(1,0,0);
			lavaClean(location, place_floor);
		}
		for(int i=0; i < count; i++){
			location.add(0,0,1);
			lavaClean(location, place_floor);
		}
	}
	
	private void lavaClean(Location l, boolean place_floor){
		final Location tmp_l = l.clone();
		if(l.getBlock().getType() == Material.LAVA || l.getBlock().getType() == Material.STATIONARY_LAVA){
			if(place_floor){
				if(l.getWorld().getEnvironment() == Environment.NORMAL){
					l.getBlock().setType(Material.STONE);
				} else if (l.getWorld().getEnvironment() == Environment.NETHER) {
					l.getBlock().setType(Material.NETHERRACK);
				}
			} else {
				l.getBlock().setType(Material.AIR);
			}
			cant_flow_liquid.add(Util.LocToString(tmp_l));
			this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
				public void run() {
					cant_flow_liquid.remove(Util.LocToString(tmp_l));
				}
	    	}, 100);
			//this.plugin.log("Lava %s is clean", Util.LocToString(l));
			//lavaClean(l.clone().add(0,1,0), false);
		}
	}
	
	//豬人會掉落地獄疙瘩
	@EventHandler
	public void onPigZombieDeath(EntityDeathEvent e){
		if(!Config.WP_PIGZOMBIE_DROP_NETHER_WART.getBoolean())return;
		LivingEntity entity = e.getEntity();
        World world = entity.getWorld();
        ArrayList<ItemStack> will_remove = new ArrayList<ItemStack>();
        boolean in_nether = world.getEnvironment().equals(World.Environment.NETHER);
        int add_nether_wart_count = 0;
        if (in_nether && entity instanceof PigZombie){
        	for(ItemStack i : e.getDrops()){
        		if(i.getType().equals(Material.GOLD_NUGGET)){
        			if(Config.WP_PIGZOMBIE_DROP_NETHER_WART_PERCENT.getDouble() >= Math.random()){
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
		if(!Config.WP_NETHER_DOOR_PROTECT_ENABLE.getBoolean())return;
		if(Config.WP_ANTI_NETHER_DOOR_ENTITY_TELEPORT.getBoolean())e.setCancelled(true);
    }
    
    //防止玩家擋住地獄門
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockNetherDoor(BlockPlaceEvent e){
		if(!Config.WP_NETHER_DOOR_PROTECT_ENABLE.getBoolean())return;
    	if(!Config.WP_ANTI_NETHER_DOOR_BLOCK.getBoolean())return;
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