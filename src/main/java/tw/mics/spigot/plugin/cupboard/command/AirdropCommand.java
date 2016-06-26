package tw.mics.spigot.plugin.cupboard.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Drops;
import tw.mics.spigot.plugin.cupboard.entity.FallingPackageEntity;

public class AirdropCommand  implements CommandExecutor {
	Cupboard plugin;
	public AirdropCommand(Cupboard i){
		this.plugin = i;
	}
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if( arg3.length == 0 ){
			sendHelp(sender);
		} else if( arg3[0].equalsIgnoreCase("summon") ) {
			if (arg3.length == 3 ){
				try {
					double x = Double.valueOf(arg3[1]);
					double z = Double.valueOf(arg3[2]);
					airdrop(x, z);
					sender.sendMessage(String.format("Airdrop summon on x: %f z: %f", x, z));
				} catch (NumberFormatException err) {
					sendHelp(sender);
				}
			} else if( (sender instanceof Player) && arg3.length == 1 ){
				airdrop(((Player)sender).getLocation());
				sender.sendMessage("Airdrop summon on your location.");
			} else {
				sendHelp(sender);
			}
		} else if (arg3[0].equalsIgnoreCase("additem")) {
			if(!(sender instanceof Player)){
				sender.sendMessage("§4this command must run on player");
				return true;
			} else if(arg3.length == 2) {
				try {
					double chance = Double.valueOf(arg3[1]);
					Player p = (Player) sender;
					Drops.addDrops(p.getInventory().getItemInMainHand(), chance);
					sender.sendMessage("Items is added, and drops.yml is saved.");
					return true;
				} catch (NumberFormatException err) {
					sendHelp(sender);
				}
			} else {
				sendHelp(sender);
			}
		} else if (arg3[0].equalsIgnoreCase("getdrop")) {
			sender.sendMessage("This command is not finish :(");
		} else if (arg3[0].equalsIgnoreCase("getitemlist")) {
			sender.sendMessage("This command is not finish :(");
		} else if (arg3[0].equalsIgnoreCase("reload")) {
			Drops.reload();
			sender.sendMessage("drops.yml loaded.");
		} else {
			sendHelp(sender);
		}
		return true;
	}
	
	private void sendHelp(CommandSender sender){
		sender.sendMessage("/airdrop summon <x> <z>    - this will summon airdrop");
		sender.sendMessage("/airdrop additem <chance>  - this can add your on hand item to drops.yml");
		sender.sendMessage("/airdrop getitem <number>  - get airdrop item");
		sender.sendMessage("/airdrop getitemlist       - get airdrop item");
		sender.sendMessage("/airdrop reload            - reload drops.yml");
	}

	private void airdrop(double x, double z){
		airdrop(new Location(plugin.getServer().getWorld("world"), x, 255, z));
	}

	//TODO this is have a little diffirent one at schedule.Airdrop
	private void airdrop(Location l){
		Location drop_loc = l.clone();
		
		drop_loc.setY(l.getWorld().getMaxHeight());
		new FallingPackageEntity(drop_loc, Material.NOTE_BLOCK);
	}
	
}
