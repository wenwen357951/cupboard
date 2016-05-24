package com.mics.spigotPlugin.cupboard;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Util {
	static public void msgToPlayer(Player p, String str){
		p.sendMessage(str);
	}
	static public String LocToString(Location l){
		String str = String.format("%s,%d,%d,%d",
				l.getWorld().getName(),
				l.getBlockX(),
				l.getBlockY(),
				l.getBlockZ());
		return str;
	}
}

