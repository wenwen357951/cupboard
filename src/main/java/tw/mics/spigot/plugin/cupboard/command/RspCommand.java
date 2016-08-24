package tw.mics.spigot.plugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;

public class RspCommand implements CommandExecutor {
	Cupboard plugin;
	public RspCommand(Cupboard i){
		this.plugin = i;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if( (sender instanceof Player) && !sender.isOp() ) {
            sender.sendMessage("§4you have no permission");
            return true;
        }
        
        if(args.length != 1){
            return false;
        }
        
        Player p = this.plugin.getServer().getPlayer(args[0]);
        if(p == null){
            sender.sendMessage("Can't find that player");
            return true;
        }
        
        
        SpawnLocationManager.applyPlayerProtect(p);
        if(SpawnLocationManager.useNewSpawn()){
            SpawnLocationManager.teleportPlayerToNewSpawn(p);
        } else {
            p.sendMessage(String.format(Locales.BED_WORLD_SPAWN_UPDATE_TIME.getString(), SpawnLocationManager.getTimeLeft()));
            p.teleport(SpawnLocationManager.getSpawnLocation());
        }
            
        
        return true;
    }

}
