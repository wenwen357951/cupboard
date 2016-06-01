package com.mics.spigotPlugin.cupboard;

import java.util.Collection;
import java.util.List;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Locales;

public class WorldBorder {
	Cupboard plugin;
	Runnable runnable;
	long last_border = 600000000;
	public WorldBorder(Cupboard i){
		this.plugin = i;
		//init border
		List<World> worlds = plugin.getServer().getWorlds();
		long passed_sec = plugin.getServer().getWorld("world").getFullTime();
		setWorldBorder(worlds, passed_sec);
		setupRunnable();
	}
	
	private void setWorldBorder(List<World> worlds, long passed_sec){
		long border = getSize(passed_sec,
				Config.WB_INIT_RADIUS.getInt(), 
				Config.WB_DEDUCT_TIME.getInt(),
				Config.WB_DEDUCT_AMOUNT.getInt());
		if(last_border == border) return; //未改變則略過
		last_border = border;
		long nether_border = border / Config.WB_NETHER_SCALE.getInt();
		long ender_border = border / Config.WB_ENDER_SCALE.getInt();
		Collection<? extends Player> players = this.plugin.getServer().getOnlinePlayers();
		
		for( World w : worlds){
			w.getWorldBorder().setCenter(0.5, 0.5);
			if(w.getEnvironment() == Environment.NETHER){
				w.getWorldBorder().setSize(nether_border * 2);
			} else if(w.getEnvironment() == Environment.THE_END) {
				w.getWorldBorder().setSize(ender_border * 2);
			} else {
				w.getWorldBorder().setSize(border * 2);
			}
		}
		
		String str = String.format(Locales.BORDER_IS_CHANGED.getString(),
				Config.WB_DEDUCT_TIME.getInt() / 60.0,
				Config.WB_DEDUCT_AMOUNT.getInt(),
				border,
				nether_border,
				ender_border
			);
		
		for (Player p : players){
			p.sendMessage(str);
		}
		
	}
	
	private long getSize(long passed_sec, int init, int deduct_time, int deduct_amount){
		long radius = (init - (passed_sec / deduct_time)	* deduct_amount);
		if(radius < Config.WB_MIN_RADIUS.getInt())radius = Config.WB_MIN_RADIUS.getInt();
		return  radius;
	}
	
	private void setupRunnable(){
		runnable = new Runnable(){
			@Override
			public void run() {
				List<World> worlds = plugin.getServer().getWorlds();
				long passed_sec = plugin.getServer().getWorld("world").getFullTime() / 20;
				setWorldBorder(worlds, passed_sec);
			}
		};
		this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 20);
	}
}
