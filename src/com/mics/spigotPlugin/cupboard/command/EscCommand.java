package com.mics.spigotPlugin.cupboard.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.timer.TeleportRunnable;


public class EscCommand implements CommandExecutor{
	Cupboard plugin;
	List<Material> blockBlockList;
	public EscCommand(Cupboard i){
		this.plugin = i;
		blockBlockList = new ArrayList<Material>();
		blockBlockList.add(Material.STATIONARY_LAVA);
		blockBlockList.add(Material.LAVA);
		blockBlockList.add(Material.FIRE);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§4this command must run on player");
			return true;
		}
		Player p = (Player) sender;
		Location oringal_location = p.getLocation();
		if(!plugin.data.checkIsLimit(oringal_location, p)){
			p.sendMessage("你不在未授權的範圍內");
			return true;
		}
		int count = 1;
		Location new_location;
		while(true){
			new_location = oringal_location.add(count , 0 , 0);
				if(this.sendToGround(new_location))
					if(!plugin.data.checkIsLimit(new_location, p))
						break;
			new_location = oringal_location.add(-count , 0 , 0);
				if(this.sendToGround(new_location))
					if(!plugin.data.checkIsLimit(new_location, p))
						break;
			new_location = oringal_location.add(0 , 0 , count);
				if(this.sendToGround(new_location))
					if(!plugin.data.checkIsLimit(new_location, p))
						break;
			new_location = oringal_location.add(0 , 0 , -count);
				if(this.sendToGround(new_location))
					if(!plugin.data.checkIsLimit(new_location, p))
						break;
			if(count > 100){
				p.sendMessage("找不到適合的傳送點，請盡可能的靠近非保護區再試一次");
				break;
			}
			count++;
		}
		new_location.setX(new_location.getBlockX()+0.5);
		new_location.setZ(new_location.getBlockZ()+0.5);
		//new java.util.Timer().schedule( new TeleportTimerTask(new_location, p.getLocation().clone(), p), 10000);
		TeleportRunnable teleport = new  TeleportRunnable(new_location, p.getLocation().clone(), p);
		teleport.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, teleport, 20, 20));
		
		p.sendMessage("10秒後傳送，請勿移動");
		//p.teleport(new_location);
		return true;
	}
	
	public boolean sendToGround(Location location) {
		//l.add(-0.5,-1,-0.5);
		location.setY(255);
		for(double y = location.getY(); y > 60; y-=1){
			location.setY(y);
			if(location.getBlock().getType() == Material.AIR) continue;
			if(blockBlockList.contains(location.getBlock().getType())){
				return false;
			}
			location.add(0,1,0);
			return true;
		}
		return false;
	}
}
