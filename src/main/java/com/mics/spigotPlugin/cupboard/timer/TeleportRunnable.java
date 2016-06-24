package com.mics.spigotPlugin.cupboard.timer;



import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.config.Locales;

public class TeleportRunnable implements Runnable  {
	Location dest;
	Location org; //原始使用者位置
	Player player;
	int count;
	int taskId;

    public TeleportRunnable(Location l,Location o, Player p ) {
        this.dest = l;
        this.org = o;
        this.player = p;
        count = 0;
    }

    public void run() {
    	if(
    			this.org.getX() == this.player.getLocation().getX() &&
				this.org.getY() == this.player.getLocation().getY() &&
				this.org.getZ() == this.player.getLocation().getZ()
		){
        	if(count < 10){
        		if(count >= 7){
        			this.player.sendMessage(String.valueOf(10 - count));
        		}
        		count++;
        		return;
        	}
    		this.player.teleport(this.dest);
    		Bukkit.getServer().getScheduler().cancelTask(taskId);
    	} else {
    		this.player.sendMessage(Locales.TELEPORT_FAIL.getString());
    		Bukkit.getServer().getScheduler().cancelTask(taskId);
    	}
    }
    
    public void setTaskId(int id){
    	this.taskId = id;
    }
}