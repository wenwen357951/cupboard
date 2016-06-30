package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerRespawnEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class PlayerRespawnListener extends MyListener {
	List<Player> mob_ignore_player;
	
	public PlayerRespawnListener(Cupboard instance)
	{
	    super(instance);
	    mob_ignore_player = new ArrayList<Player>();
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
		mob_ignore_player.add(p);
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				mob_ignore_player.remove(p);
			}
		}, 160); //TODO change protect timer CONFIG
	}

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
