package com.mics.spigotPlugin.cupboard.command;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.Cupboard;


public class KillCommand implements CommandExecutor{
	Cupboard plugin;
	List<Material> blockBlockList;
	public KillCommand(Cupboard i){
		this.plugin = i;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§4this command must run on player");
			return true;
		}
		Player p = (Player) sender;
		p.setHealth(0);
		return true;
	}
}
