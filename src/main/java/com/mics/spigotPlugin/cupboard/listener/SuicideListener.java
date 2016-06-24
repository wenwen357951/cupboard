package com.mics.spigotPlugin.cupboard.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class SuicideListener extends MyListener {
	public SuicideListener(Cupboard instance) {
		super(instance);
	}
	
    @EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
    	Player p = e.getEntity();
    	if(p.hasMetadata("suicide")){
    		p.removeMetadata("suicide", plugin);
    		e.setDeathMessage("");
    	}
    }
}
