package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;

public class PlayerRespawnListener extends MyListener {
	List<Player> mob_ignore_player;
	private Map<String, List<ItemStack>> saveinv;
    public PlayerRespawnListener(Cupboard instance)
    {
        super(instance);
        mob_ignore_player = new ArrayList<Player>();
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

    
    //玩家重生
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
        Location l = event.getRespawnLocation();
        

        //怪物無視
        mob_ignore_player.add(p);
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run() {
                mob_ignore_player.remove(p);
            }
        }, 200); //TODO change protect timer CONFIG
        
        //物品恢復
        if(saveinv.containsKey(p.getUniqueId().toString())){
            for(ItemStack i: saveinv.get(p.getUniqueId().toString())){
                event.getPlayer().getInventory().addItem(i);
            }
            saveinv.remove(p.getUniqueId().toString());
        }
        
        //床復活岩漿確認
        if(event.isBedSpawn()){
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
            } else if(this.plugin.cupboards.checkIsLimit(l, p)){
                //Spawn location check
                p.setBedSpawnLocation(null);
                p.sendMessage(Locales.SPAWN_WITHOUT_ACCESS.getString());
            } else if(
                //spawn lava check
                event.getRespawnLocation().getBlock().getType() == Material.LAVA ||
                event.getRespawnLocation().getBlock().getType() == Material.STATIONARY_LAVA ||
                event.getRespawnLocation().clone().add(0,1,0).getBlock().getType() == Material.LAVA ||
                event.getRespawnLocation().clone().add(0,1,0).getBlock().getType() == Material.STATIONARY_LAVA
            ){
                event.getPlayer().setBedSpawnLocation(null);
                event.getPlayer().sendMessage(Locales.BED_HAVE_LAVA.getString());
            } else {
                return; //重生點沒問題 直接在床重生
            }
        }
        
        //隨機重生
        if(Config.PP_PLAYER_RANDOM_SPAWN_ENABLE.getBoolean()){
            if(SpawnLocationManager.checkNewSpawnLocation()){
                String msg = Locales.BED_WORLD_SPAWN_UPDATED.getString();
                event.getPlayer().sendMessage(msg);
                for( Player pl : plugin.getServer().getOnlinePlayers() ){
                    if(pl.getBedSpawnLocation() == null)
                        pl.sendMessage(msg);
                }
            } else {
                event.getPlayer().sendMessage(String.format(Locales.BED_WORLD_SPAWN_UPDATE_TIME.getString(), SpawnLocationManager.getTimeLeft()));
            }
            event.setRespawnLocation(SpawnLocationManager.getSpawnLocation());
        }
		
	}

	
	//怪物無視事件
	@EventHandler
	public void onPlayerHitPlayer(EntityDamageByEntityEvent event){
		if(this.mob_ignore_player.size() == 0) return;
		if(
			event.getEntity() instanceof Player
		){
			Player entity = (Player) event.getEntity();
			if(this.mob_ignore_player.contains(entity)){
				event.setCancelled(true);
			}
		}
	}	
	
	@EventHandler
	public void onPlayerHitPlayer(EntityDamageEvent event){
		if(this.mob_ignore_player.size() == 0) return;
		if(
			event.getEntity() instanceof Player
		){
			Player entity = (Player) event.getEntity();
			if(this.mob_ignore_player.contains(entity)){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	 public void onMobTargetPlayer(EntityTargetEvent event){
		 if(
				 event.getTarget() instanceof Player &&
				 event.getReason() == TargetReason.CLOSEST_PLAYER
			 ){
			 Player p = (Player) event.getTarget();
			 if(this.mob_ignore_player == null) return;
			 if(this.mob_ignore_player.contains(p)){
				 event.setCancelled(true);
			 }
		 }
	 }
}
