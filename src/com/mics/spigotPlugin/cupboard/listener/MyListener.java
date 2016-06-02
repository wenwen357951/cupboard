package com.mics.spigotPlugin.cupboard.listener;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.mics.spigotPlugin.cupboard.Cupboard;

public abstract class MyListener implements Listener {
	protected Cupboard plugin;
	public MyListener(Cupboard instance){
		this.plugin = instance;
	    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	    this.plugin.logDebug(this.getClass().getSimpleName() + " Registed.");
	}
	
	public void unregisterListener(){
		HandlerList.unregisterAll(this);
	    this.plugin.logDebug(this.getClass().getSimpleName() + " Unregisted.");
	}
}
