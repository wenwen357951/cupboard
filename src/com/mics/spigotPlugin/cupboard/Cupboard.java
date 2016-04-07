package com.mics.spigotPlugin.cupboard;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class Cupboard extends JavaPlugin implements Listener {
	Data data;
	@Override
	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        //處理資料庫
        data = new Data(getDataFolder());
    }
	
    @Override
    public void onDisable() {

    }

    //移除金磚
    @EventHandler
    public void onGoldBlockBreak(BlockBreakEvent event){
    	if(event.getBlock().getType() == Material.GOLD_BLOCK){
    		//Player p = event.getPlayer();
    		data.removeCupboard(event.getBlock());
    		//Util.msgToPlayer(p, "已移除工具櫃");
    	}
    }
    
    //放置金磚
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGoldBlockPlace(BlockPlaceEvent event){
    	if(event.getBlockPlaced().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		if(!data.putCupboard(event.getBlockPlaced(), p)){
    			Util.msgToPlayer(p, "距離其他工具櫃太近。");
    			event.setCancelled(true);
    			return;
    		}
    		Util.msgToPlayer(p, "工具櫃已經放置並取得授權。");
    	}
    }

    
    //授權/取消授權
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event){
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;                    		// off hand packet, ignore.
    	if (event.getClickedBlock().getType() != Material.GOLD_BLOCK) return;       	// 非黃金磚則無視
    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return; 						// 非右鍵方塊則無視
        if (event.getItem() != null && event.getItem().getType().isBlock()) return;   	// 非空手則無視
    	
		Player p = event.getPlayer();
		String str;
		if(!data.checkCupboardExist(event.getClickedBlock())){
			str="此方塊並非由玩家放置或資料遺失，請拆除後重新放置";
		}else if(data.toggleBoardAccess(p, event.getClickedBlock())){
			str="工具櫃已授權";
		} else {
			str="工具櫃已取消授權";
		}
		
    	GameMode p_gamemode = p.getGameMode();
		boolean limit = data.checkIsLimit(p.getLocation(), p);
    	
    	if(p_gamemode == GameMode.SURVIVAL && limit){
    		p.setGameMode(GameMode.ADVENTURE);
    	}

    	if(p_gamemode == GameMode.ADVENTURE && !limit){
    		p.setGameMode(GameMode.SURVIVAL);
    	}
		Util.msgToPlayer(p, str);
    }
    
    //==========以下為保護措施=========
    
    //進入範圍內之玩家會設定為冒險模式
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e){
    	for(int i=0; i<2000; i++){
    		//TODO maybe need improve speed
	    	Player p = e.getPlayer();
	    	GameMode p_gamemode = p.getGameMode();
	    	boolean limit = data.checkIsLimit(p.getLocation(), p);
	    	
	    	if(p_gamemode == GameMode.SURVIVAL && limit){
	    		p.setGameMode(GameMode.ADVENTURE);
	    	}
	
	    	if(p_gamemode == GameMode.ADVENTURE && !limit){
	    		p.setGameMode(GameMode.SURVIVAL);
	    	}
    	}
    	
    }
    
  //禁止使用石製開關 以及 石製踏板
    @EventHandler
    public void onRightClickDoor(PlayerInteractEvent event){
    	Block b = event.getClickedBlock();
    	Player p = event.getPlayer();
    	if ((
    			event.getAction() == Action.RIGHT_CLICK_BLOCK && 
				b.getType() == Material.STONE_BUTTON
			) || (
    			event.getAction() == Action.PHYSICAL &&
    			b.getType() == Material.STONE_PLATE
			))
    			if(data.checkIsLimit(b, p)) event.setCancelled(true);
    	
    }
    //防止其他玩家破壞方塊
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p))
        		e.setCancelled(true);
        } else {
        	if(data.checkIsLimit(b))
        		e.setCancelled(true);
        }
    }
    //防止其他玩家放置方塊
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p))
        		e.setCancelled(true);
        } else {
        	if(data.checkIsLimit(b))
        		e.setCancelled(true);
        }
    }
    
    //防止玩家使用水桶
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlockClicked().getLocation()
    			.add(e.getBlockFace().getModX(),e.getBlockFace().getModY(),e.getBlockFace().getModZ())
    			.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		e.setCancelled(true);
        		p.updateInventory();
        	}
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlockClicked();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		e.setCancelled(true);
        		p.updateInventory();
        	}
        }
    }
    
    //TNT or Creeper爆炸
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event){
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(data.checkIsLimit(block)){
    			event.blockList().remove(block);
    		}
    	}
    }
    
    //防止方塊被燒壞
    // TODO 火焰不會消失
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onBlockBurnDamage(BlockBurnEvent e){
    	Block b = e.getBlock();
    	if(data.checkIsLimit(b))
    		e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    void onCupboardPiston(BlockPistonExtendEvent e){
    	for(Block block : e.getBlocks()){
    		if(block.getType().equals(Material.GOLD_BLOCK)){
    			e.setCancelled(true);
    		}
    	}
    }
    
    
}
