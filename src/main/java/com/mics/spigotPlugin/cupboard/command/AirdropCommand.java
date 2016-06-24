package com.mics.spigotPlugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class AirdropCommand  implements CommandExecutor {
	Cupboard plugin;
	public AirdropCommand(Cupboard i){
		this.plugin = i;
	}
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if (arg3.length == 2 ){
			try {
				double x = Integer.valueOf(arg3[0]);
				double z = Integer.valueOf(arg3[1]);
				airdrop(x, z);
				return true;
			} catch (NumberFormatException err) {
			}
		} else if( (sender instanceof Player) && arg3.length == 0 ){
			airdrop(((Player)sender).getLocation().getBlockX(), ((Player)sender).getLocation().getBlockZ());
		}
		return false;
	}
	private void airdrop(double x, double z){
		//plugin.logDebug(String.format("Command Call Airdrop at %f %f location", x, z));
	}
}
