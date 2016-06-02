package com.mics.spigotPlugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class ReloadCommand implements CommandExecutor{
	Cupboard plugin;
	public ReloadCommand(Cupboard i){
		this.plugin = i;
	}
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		plugin.reload();
		return true;
	}
}
