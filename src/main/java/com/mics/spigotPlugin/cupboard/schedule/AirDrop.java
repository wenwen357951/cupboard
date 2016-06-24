package com.mics.spigotPlugin.cupboard.schedule;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Util;

//import net.minecraft.server.v1_9_R2.nbtTag

public class AirDrop {
	Cupboard plugin;
	Runnable runnable;
	int schedule_id;
	int drop_time_count;
	ArrayList<StorageMinecart> minecarts;
	
	public AirDrop(Cupboard i){
		this.plugin = i;
		minecarts = new ArrayList<StorageMinecart>();
		drop_time_count = getNextDropRandomTime();
		setupRunnable();
		running();
	}
	
	private int getNextDropRandomTime(){
		return new Random().nextInt(Config.AIR_DROP_MAX_TIME.getInt() - Config.AIR_DROP_MIN_TIME.getInt()) + Config.AIR_DROP_MIN_TIME.getInt();
	}
	
	private void everyMin(){
		drop_time_count--;
		if(drop_time_count == Config.AIR_NOTIFY_TIME.getInt()){
			//TODO Notify All player airdrop will drop
		} else if(drop_time_count <= 0){
			//TODO Check player amount
			airDrop();
			drop_time_count = getNextDropRandomTime();
		}
	}
	private ItemStack getRandomItems(){
		ItemStack item = new ItemStack(Material.STONE_PICKAXE);
    	ItemMeta meta = item.getItemMeta();
    	meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
    	//NBTTagCompound compound = NBTTagCompound.getNewNBTTagCompound();
    	//compound
    	//item.setItemMeta(meta);
    	return item;
	}
	
	private void airDrop(){ //run every min
		World world = plugin.getServer().getWorld("world");
		Location l = Util.getRandomLocation(world);
		l.setY(world.getMaxHeight()-1);
		if(l.getChunk().load()){
			plugin.logDebug(Util.LocToString(l));
			StorageMinecart minecart = world.spawn(l, StorageMinecart.class);
			minecart.setCustomName("空投物資箱");
			
			
	    	
			minecart.getInventory().setItem(0, getRandomItems());
			minecarts.add(minecart);
		}
	}
	
	private void running(){
		// next_air_drop_time *= 1200 //Convert ticks to min
		schedule_id = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, runnable, 1); //have to change 1200
	}
	
	private void setupRunnable(){
		runnable = new Runnable(){
			@Override
			public void run() {
				everyMin();
				running();
			}
		};
	}
}
