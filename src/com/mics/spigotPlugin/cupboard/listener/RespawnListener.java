package com.mics.spigotPlugin.cupboard.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.utils.Locales;


public class RespawnListener implements Listener {
	Cupboard plugin;
	public RespawnListener(Cupboard instance)
	{
		this.plugin = instance;
	  	this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	  	this.plugin.logDebug("SpawnListener Registed.");
	}
	
    @EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Location l = event.getRespawnLocation();
		Player p = event.getPlayer();
		if(event.isBedSpawn()){
			if(this.plugin.data.checkIsLimit(l, p)){
				p.setBedSpawnLocation(null);
				p.sendMessage(Locales.SPAWN_WITHOUT_ACCESS.getString());
			} else {
				//TODO offline delete spawn location
			}
		}
	}

}
