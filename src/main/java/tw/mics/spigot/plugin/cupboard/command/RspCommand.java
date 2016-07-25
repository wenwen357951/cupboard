package tw.mics.spigot.plugin.cupboard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
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
        
        if(Config.PP_PLAYER_RANDOM_SPAWN_ENABLE.getBoolean()){
            if(Config.PP_PLAYER_SPAWN_PROTECT.getBoolean()){
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 3));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 3));
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 3));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
            }
            
            //傳送到世界重生點
            if(SpawnLocationManager.checkNewSpawnLocation()){
                String msg = Locales.BED_WORLD_SPAWN_UPDATED.getString();
                for( Player pl : plugin.getServer().getOnlinePlayers() ){
                    if(pl.getBedSpawnLocation() == null)
                        pl.sendMessage(msg);
                }
            } else {
                p.sendMessage(String.format(Locales.BED_WORLD_SPAWN_UPDATE_TIME.getString(), SpawnLocationManager.getTimeLeft()));
            }
            p.teleport(SpawnLocationManager.getSpawnLocation());
            
        } else {
            sender.sendMessage("random spawn is disable");
        }
        
        return true;
    }

}
