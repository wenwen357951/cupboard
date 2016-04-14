package com.mics.spigotPlugin.cupboard.timer;



import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    	if(this.org.equals(this.player.getLocation())){
        	if(count < 30){
        		count++;
        		return;
        	}
    		this.player.teleport(this.dest);
    		Bukkit.getServer().getScheduler().cancelTask(taskId);
    	} else {
    		this.player.sendMessage("§4傳送失敗");
    		Bukkit.getServer().getScheduler().cancelTask(taskId);
    	}
    }
    
    public void setTaskId(int id){
    	this.taskId = id;
    }
}