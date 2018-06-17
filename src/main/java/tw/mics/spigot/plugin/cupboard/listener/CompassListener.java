package tw.mics.spigot.plugin.cupboard.listener;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.evilpoint.data.EvilPointData;
import tw.mics.spigot.plugin.evilpoint.EvilPoint;

public class CompassListener extends MyListener {
    
    public CompassListener(Cupboard instance) {
        super(instance);
    }
    
    //使用指南針
    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if(p.getGameMode() != GameMode.SURVIVAL)return;
        if(event.getHand() == null) return;
        if(
                event.getAction() == Action.RIGHT_CLICK_AIR || 
                event.getAction() == Action.RIGHT_CLICK_BLOCK
        ){
            switch(event.getHand()){
            default:
            case HAND:
                if(p.getInventory().getItemInMainHand().getType() != Material.COMPASS) return;
                break;
            case OFF_HAND:
                if(p.getInventory().getItemInOffHand().getType() != Material.COMPASS) return;
                break;
            }
            //確定邪惡精華足夠
            if(Config.EVILESSENCE_ENABLE.getBoolean()){
                if(!p.getInventory().contains(Material.COMMAND_MINECART, Config.EVILESSENCE_COMPASS_COST.getInt())){
                    p.sendMessage(Locales.TNT_EVILESSENCE_NOT_ENOUGH.getString());
                    return;
                }
            }
            if(p.getWorld().getEnvironment() != Environment.NORMAL){
                p.sendMessage(Locales.COMPASS_NOT_FOUND_PLAYER.getString());
                return;
            }
            List<Player> players = p.getWorld().getPlayers();
            players.remove(p);
            Iterator<Player> itr = players.iterator();
            while(itr.hasNext()){
                Player lp = itr.next();
                if(
                    lp.getLocation().getBlockY() > Config.COMPASS_TARGET_Y.getInt() ||
                    lp.getGameMode() != GameMode.SURVIVAL
                ){
                    itr.remove();
                }
            }
            if(players.size() == 0){
                p.sendMessage(Locales.COMPASS_NOT_FOUND_PLAYER.getString());
                return;
            }
            Player target_p = players.get(new Random().nextInt(players.size()));
            Double random = Math.random();
            Location loc = target_p.getLocation().add(
                    Config.COMPASS_DEVIATION.getInt() * Math.sin(Math.PI * 2 * random), 
                    0, 
                    Config.COMPASS_DEVIATION.getInt() * Math.cos(Math.PI * 2 * random)
            );
            EvilPointData evilpointdata = EvilPoint.getInstance().evilpointdata;
            Player player = event.getPlayer();
            evilpointdata.plusEvil(player, 30);
            p.sendMessage(Locales.COMPASS_SUCCESSFUL.getString());
            target_p.sendMessage(Locales.COMPASS_BE_POINTED.getString());
            p.setCompassTarget(loc);
            
            Inventory inv = p.getInventory();
            for(int i=0; i<Config.EVILESSENCE_COMPASS_COST.getInt(); i++){
                inv.setItem(inv.first(Material.COMMAND_MINECART), null);
            }
        }
    }
}
