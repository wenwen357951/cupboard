package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public class WorldProtectListener extends MyListener {
	private List<String> cant_flow_lava;
	private List<Chunk> cant_flow_liquid;

	public WorldProtectListener(Cupboard instance)
	{
		super(instance);
		cant_flow_liquid = new ArrayList<Chunk>();
	    cant_flow_lava = new ArrayList<String>();
	}
	
	//更改地獄門搜尋半徑
	//防止地獄門被岩漿封門，如果地獄門被完全封死，將自動清除地獄門附近的東西
	@EventHandler
	public void onNetherPortal(PlayerPortalEvent e){
		if(!Config.WP_NETHER_DOOR_PROTECT_ENABLE.getBoolean())return;
		e.getPortalTravelAgent().setSearchRadius(Config.WP_NETHER_SREACH_RADIUS.getInt());
		
		if(Config.WP_NETHER_SCALE_ENABLE.getBoolean()){
    		Location l = e.getFrom().clone();
    		if(
		        l.getWorld().getEnvironment() == Environment.NORMAL && 
		        e.getTo().getWorld().getEnvironment() == Environment.NETHER
	        ){
    		    l.multiply(1.0/Config.WP_NETHER_SCALE.getDouble());
    		} else if(l.getWorld().getEnvironment() == Environment.NETHER) {
    		    l.multiply(Config.WP_NETHER_SCALE.getDouble());
    		} else {
    		    return;
    		}
    		l.setWorld(e.getTo().getWorld());
    		e.setTo(e.getPortalTravelAgent().findOrCreate(Util.changeLocationInBorder(l)));
		}
		
		if(Config.WP_ANTI_NETHER_DOOR_BLOCK.getBoolean()){
    		final Player p = e.getPlayer();
    		final Location l = e.getFrom().clone();
    		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run() {
                    if(p.getLocation().add(0,1,0).getBlock().getType() == Material.PORTAL){
                        p.teleport(l);
                        p.sendMessage(Locales.WP_NETHER_PORTEL_TELEPORT_BACK.getString());
                    }
                }
    		}, 400);
		}
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
    	if(this.cant_flow_lava.contains(Util.LocToString(e.getToBlock().getLocation()))){
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
			cant_flow_lava.add(Util.LocToString(tmp_l));
			this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
				public void run() {
					cant_flow_lava.remove(Util.LocToString(tmp_l));
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
    
    //防止玩家過快放置水流
    //TODO clear data after time
    private HashMap<String, LiquidCounter> player_liquid_counter = new HashMap<String, LiquidCounter>();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
        if(!Config.WP_ANTI_LIQUID_FAST_PUT.getBoolean())return;
        Block b = e.getBlockClicked().getLocation()
                .add(e.getBlockFace().getModX(),e.getBlockFace().getModY(),e.getBlockFace().getModZ())
                .getBlock();
        onBucket(e, b);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBucketFill(PlayerBucketFillEvent e){
        if(!Config.WP_ANTI_LIQUID_FAST_PUT.getBoolean())return;
        Block b = e.getBlockClicked();
        onBucket(e, b);
    }
    
    private void onBucket(PlayerBucketEvent e, Block b){
        if(e.isCancelled()) return;
        String puuid = e.getPlayer().getUniqueId().toString();
        LiquidCounter counter = player_liquid_counter.get(puuid);
        if(counter == null){
            counter = new LiquidCounter();
            player_liquid_counter.put(puuid, counter);
        }
        counter.count += 1;
        if(counter.count > 10){
            if((System.currentTimeMillis() - counter.time) < 60000){
                cant_flow_liquid.add(b.getChunk());
                if(counter.count == 11)
                    e.getPlayer().sendMessage(Locales.WP_LIQUID_LIMIT.getString());
            } else {
                counter.reset();
            }
        }
        counter.update_time();
    }
    
    //暫時防止液體流動
    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e){
        if(!Config.WP_ANTI_LIQUID_FAST_PUT.getBoolean())return;
        Location l = e.getBlock().getLocation();
        if(this.cant_flow_liquid.contains(l.getChunk())){
            e.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
                @Override
                public void run() {
                    cant_flow_liquid.remove(l.getChunk());
                }
            });
        }
    }
    
    class LiquidCounter{
        public int count;
        public long time;
        public LiquidCounter(){
            count = 0;
            time = System.currentTimeMillis();
        }
        public void reset() {
            count = 0;
            time = System.currentTimeMillis();
        }
        public void update_time(){
            time = System.currentTimeMillis();
        }
    }
}
