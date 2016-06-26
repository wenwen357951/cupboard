package tw.mics.spigot.plugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class ReloadCommand implements CommandExecutor{
	Cupboard plugin;
	public ReloadCommand(Cupboard i){
		this.plugin = i;
	}
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		plugin.reload();
		sender.sendMessage("Config Reloaded!");
		return true;
	}
}
