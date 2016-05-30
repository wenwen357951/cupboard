package com.mics.spigotPlugin.cupboard.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.timer.TeleportRunnable;
import com.mics.spigotPlugin.cupboard.utils.Locales;


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
			p.sendMessage(Locales.NOT_IN_NO_ACCESS_AREA.getString());
			return true;
		}
		
		Location new_location = null;
		for(int count = 1; count < 30; count++){
			new_location = this.aroundSafeLocationFind(p, oringal_location, count);
			if(new_location != null) break;
		}
		if(new_location == null){
			p.sendMessage(Locales.TELEPORT_NOT_FOUND.getString());
			return true;
		}
		new_location.setX(new_location.getBlockX()+0.5);
		new_location.setZ(new_location.getBlockZ()+0.5);
		
		TeleportRunnable teleport = new  TeleportRunnable(new_location, p.getLocation().clone(), p);
		teleport.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, teleport, 20, 20));
		//p.teleport(new_location);
		
		p.sendMessage(Locales.TELEPORT_NOW.getString());
		return true;
	}
	
	private Location aroundSafeLocationFind(Player p, Location org_location, int count) {
		Location location = org_location.clone();
		location.add(1,0,0);
		Location location_sendtoGround = sendToGround(location);
		if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
			return location_sendtoGround;
		for(int i=0; i < count; i++){
			location.add(0,0,1);
			location_sendtoGround = sendToGround(location);
			if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
				return location_sendtoGround;
		}
		for(int i= -count ; i < count; i++){
			location.add(-1,0,0);
			location_sendtoGround = sendToGround(location);
			if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
				return location_sendtoGround;
		}
		for(int i= -count; i < count; i++){
			location.add(0,0,-1);
			location_sendtoGround = sendToGround(location);
			if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
				return location_sendtoGround;
		}
		for(int i= -count ; i < count; i++){
			location.add(1,0,0);
			location_sendtoGround = sendToGround(location);
			if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
				return location_sendtoGround;
		}
		for(int i=0; i < count; i++){
			location.add(0,0,1);
			location_sendtoGround = sendToGround(location);
			if(location_sendtoGround != null && !this.plugin.data.checkIsLimit(location_sendtoGround, p))
				return location_sendtoGround;
		}
		return null;
	}

	private boolean isStable(Location l) {
		Location location = l.clone();
		if(
				(
						location.getBlock().getLightLevel() > 7 || 
						location.getBlock().getLightFromSky() != 0 ||
						location.getWorld().getEnvironment() == World.Environment.NETHER
				) &&
				location.add(0,1,0).getBlock().getType().equals(Material.AIR) &&
				location.add(0,1,0).getBlock().getType().equals(Material.AIR)
			)return true;
		return false;
	}

	public Location sendToGround(Location org_location) {
		Location location = org_location.clone();
		int y = location.getBlockY() + 10;
		if(location.getWorld().getEnvironment() == World.Environment.NETHER){
			if (y > 125)
				y = 125; //防止生到地獄樓上Orz
		}
		int y_min = y - 20; 
		for(; y > y_min; y--){
			location.setY(y);
			if(location.getBlock().getType() == Material.AIR){
				continue;
			}
			if(blockBlockList.contains(location.getBlock().getType())){
				continue;
			}
			if(this.isStable(location)){
				location.add(0,1,0);
				return location;
			}
		}
		return null;
	}
}
