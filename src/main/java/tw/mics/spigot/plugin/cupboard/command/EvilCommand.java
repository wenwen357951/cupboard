package tw.mics.spigot.plugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class EvilCommand implements CommandExecutor {
	Cupboard plugin;
	public EvilCommand(Cupboard i){
		this.plugin = i;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("this command not ready to use.");
        return true;
    }

}
