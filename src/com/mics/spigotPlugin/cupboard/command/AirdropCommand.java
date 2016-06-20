package com.mics.spigotPlugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class AirdropCommand {
	public class ReloadCommand implements CommandExecutor{
		Cupboard plugin;
		public ReloadCommand(Cupboard i){
			this.plugin = i;
		}
		@Override
		public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
			if (!(sender instanceof Player)) {
				if( arg3.length == 3 ){
					sender.sendMessage("Airdrop");
				} else {
					return false;
				}
			} else {
				if( arg3.length == 0 ){
					sender.sendMessage("Airdrop at sender loction");
				} else if ( arg3.length == 0 ){
					sender.sendMessage("Airdrop at static location");
				} else {
					return false;
				}
			}
			return true;
		}
	}
}
