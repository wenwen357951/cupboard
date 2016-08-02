package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;

public class PlayerRespawnListener extends MyListener {
	private Map<String, List<ItemStack>> saveinv;
	
    public PlayerRespawnListener(Cupboard instance)
    {
        super(instance);
        saveinv = new HashMap<String, List<ItemStack>>();
    }
	
	//死亡儲存物品
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player p = event.getEntity();
        List<ItemStack> keepInv = new ArrayList<ItemStack>();
        if(Config.PP_PLAYER_INVENTORY_RECOVERY_PERCENT.getDouble() != 0 && p.isOnline()){
            for(ItemStack i: Arrays.asList(event.getEntity().getInventory().getContents())){
                if(i == null) continue;
                if(new Random().nextDouble() < Config.PP_PLAYER_INVENTORY_RECOVERY_PERCENT.getDouble() ){
                    keepInv.add(i);
                    event.getDrops().remove(i);
                }
            }
            saveinv.put(p.getUniqueId().toString(), keepInv);
        }
    }
    
    //床安全確認
    private boolean isPlayerBedSave(Player p){
        Location l = p.getBedSpawnLocation();
        WorldBorder border = l.getWorld().getWorldBorder();
        int max_x = border.getCenter().add(border.getSize()/2, 0, 0).getBlockX();
        int min_x = border.getCenter().add(-border.getSize()/2, 0, 0).getBlockX();
        int max_z = border.getCenter().add(0, 0, border.getSize()/2).getBlockZ();
        int min_z = border.getCenter().add(0, 0, -border.getSize()/2).getBlockZ();
        if(
                l.getBlockX() > max_x ||
                l.getBlockX() < min_x ||
                l.getBlockZ() > max_z ||
                l.getBlockZ() < min_z
        ){
            //Spawn border check
            p.setBedSpawnLocation(null);
            p.sendMessage(Locales.SPAWN_OUTSIDE_BORDER.getString());
            return false;
        } else if(this.plugin.cupboards.checkIsLimit(l, p)){
            //Spawn location check
            p.setBedSpawnLocation(null);
            p.sendMessage(Locales.SPAWN_WITHOUT_ACCESS.getString());
            return false;
        } else if(
                //spawn lava check
                Config.PP_PLAYER_REMOVE_SPAWN_WITH_LAVA.getBoolean() &&
                (
                    l.getBlock().getType() == Material.LAVA ||
                    l.getBlock().getType() == Material.STATIONARY_LAVA ||
                    l.clone().add(0,1,0).getBlock().getType() == Material.LAVA ||
                    l.clone().add(0,1,0).getBlock().getType() == Material.STATIONARY_LAVA
                )
        ){
            p.setBedSpawnLocation(null);
            p.sendMessage(Locales.BED_HAVE_LAVA.getString());
            return false;
        } else {
            return true; //重生點沒問題 直接在床重生
        }
    }
    
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
        
        //物品恢復
        if(saveinv.containsKey(p.getUniqueId().toString())){
            for(ItemStack i: saveinv.get(p.getUniqueId().toString())){
                event.getPlayer().getInventory().addItem(i);
            }
            saveinv.remove(p.getUniqueId().toString());
        }
        
        //床復活岩漿確認
        if(event.isBedSpawn() && isPlayerBedSave(p)){
            return; //有床且床安全
        }
        
        //套用保護
        SpawnLocationManager.applyPlayerProtect(p);
        
        //隨機重生
        if(Config.PP_PLAYER_RANDOM_SPAWN_ENABLE.getBoolean()){
            if(SpawnLocationManager.useNewSpawn()){
                SpawnLocationManager.teleportPlayerToNewSpawn(event.getPlayer());
            } else {
                p.sendMessage(String.format(Locales.BED_WORLD_SPAWN_UPDATE_TIME.getString(), SpawnLocationManager.getTimeLeft()));
                event.setRespawnLocation(SpawnLocationManager.getSpawnLocation());
            }
        }
	}
	
	//終界門
	@EventHandler
    public void onPortalTeleport(PlayerTeleportEvent event){
        if(!Config.PP_PLAYER_RANDOM_SPAWN_ENABLE.getBoolean()) return;
        if( event.getCause() == TeleportCause.END_PORTAL && event.getTo().getWorld().getEnvironment() == Environment.NORMAL ){
            
            //床安全性確認
            if(event.getPlayer().getBedSpawnLocation() != null && isPlayerBedSave(event.getPlayer())){
                return; //有床且床安全
            }
            
            //套用保護
            SpawnLocationManager.applyPlayerProtect(event.getPlayer());
            
            //隨機重生
            if(SpawnLocationManager.useNewSpawn()){
                SpawnLocationManager.teleportPlayerToNewSpawn(event.getPlayer());
            } else {
                event.setTo(SpawnLocationManager.getSpawnLocation());
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                    @Override
                    public void run() {
                        event.getPlayer().sendMessage(String.format(Locales.BED_WORLD_SPAWN_UPDATE_TIME.getString(), SpawnLocationManager.getTimeLeft()));
                        event.getPlayer().teleport(SpawnLocationManager.getSpawnLocation());
                    }
                });
            }
        }
    }
}
