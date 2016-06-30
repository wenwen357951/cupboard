package tw.mics.spigot.plugin.cupboard.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;

public class CupboardUseProtectListener extends MyListener{
	public CupboardsData data;

	public CupboardUseProtectListener(Cupboard instance)
	{
		super(instance);
	    this.data = this.plugin.cupboards;
	}
	
	

	//禁止使用石製開關 以及 石製踏板
	@EventHandler
	public void onUseStoneButton(PlayerInteractEvent event){
		Block b = event.getClickedBlock();
		Player p = event.getPlayer();
		if (
			event.getAction() == Action.RIGHT_CLICK_BLOCK && 
			b.getType() == Material.STONE_BUTTON
		) 
			if(data.checkIsLimit(b, p)){
				if(this.plugin.isOP(p)) return;
				event.setCancelled(true);
			}
      	
      }

    //禁止玩家使用石製踏板
      @EventHandler
      public void onUseStonePlate(PlayerInteractEvent event){
          Block b = event.getClickedBlock();
          Player p = event.getPlayer();
          if (
                  event.getAction() == Action.PHYSICAL &&
                  b.getType() == Material.STONE_PLATE
          ){
              if(data.checkIsLimit(b, p)){
                  if(this.plugin.isOP(p)) return;
                  event.setCancelled(true);
              }
          }
      }
      
      //禁止未授權玩家使用箱子
        @EventHandler
        public void onUseChest(PlayerInteractEvent event){
            if(!Config.CUPBOARD_PROTECT_CHEST.getBoolean())return;
            Block b = event.getClickedBlock();
            Player p = event.getPlayer();
            if (
                    event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    (
                        b.getType() == Material.CHEST ||
                        b.getType() == Material.TRAPPED_CHEST
                    )
            ){
                if(data.checkIsLimit(b, p)){
                    if(this.plugin.isOP(p)) return;
                    p.sendMessage(Locales.NO_ACCESS.getString());
                    event.setCancelled(true);
                }
            }
        }
      
      //禁止未授權玩家使用床，其他則記錄重生點
      @EventHandler
      public void onUseBed(PlayerInteractEvent event){
          Block b = event.getClickedBlock();
          Player p = event.getPlayer();
          if (
                  event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                  b.getType() == Material.BED_BLOCK
          ){
              if(data.checkIsLimit(b, p)){
                  if(this.plugin.isOP(p)) return;
                  p.sendMessage(Locales.NO_ACCESS.getString());
                  event.setCancelled(true);
              } else {
                  if(!SpawnLocationManager.checkPlayerSpawn(b.getLocation(), p)){
                      p.setBedSpawnLocation(b.getLocation());
                      p.sendMessage(Locales.BED_SPAWN_SET.getString());
                      event.setCancelled(true);
                  }
              }
          }
      }

    //禁止動物/怪物使用石製踏板 (玩家騎在動物上則增加玩家權限判斷
    @EventHandler
    public void onEntryUseStonePlate(EntityInteractEvent event){
    	Block b = event.getBlock();
    	if( b.getType() == Material.STONE_PLATE ){
        	Entity e = event.getEntity();
        	if (e.getPassenger() instanceof Player){
        		Player p = (Player) e.getPassenger();
        		if(data.checkIsLimit(b, p)){
        			if(this.plugin.isOP(p)) return;
        			event.setCancelled(true);
        		}
        	} else {
        		if(data.checkIsLimit(b)) event.setCancelled(true);
        	}
    	}
    }
}
