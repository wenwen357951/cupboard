package com.mics.spigotPlugin.cupboard;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
    		Player p = event.getPlayer();
    		data.removeCupboard(event.getBlock());
    		Util.msgToPlayer(p, "金磚已拆除");
    	}
    }
    
    //放置金磚
    @EventHandler(priority = EventPriority.HIGH)
    public void onGoldBlockPlace(BlockPlaceEvent event){
    	if(event.getBlockPlaced().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		if(!data.putCupboard(event.getBlockPlaced(), p)){
    			Util.msgToPlayer(p, "距離其他金磚太近。");
    			event.setCancelled(true);
    			return;
    		}
    		Util.msgToPlayer(p, "金磚已放置並取得授權。(蹲下右鍵取得說明)");
    	}
    }

    
    //授權/取消授權
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event){
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;                    		// off hand packet, ignore.
    	if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.GOLD_BLOCK) return;       	// 非黃金磚則無視
    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return; 						// 非右鍵方塊則無視
        //if (event.getItem() != null && event.getItem().getType().isBlock()) return;   	// 非空手則無視
		Player p = event.getPlayer();
		if (p.isSneaking()){
			//說明
			p.sendMessage("§a金磚放置後產生19x19x19的方形保護區，金磚在正中央。");
			p.sendMessage("§a保護區只防止下列事項: ");
			p.sendMessage("§71. 未授權者無法放置/移除方塊。");
			p.sendMessage("§72. 未授權者無法使用石製壓力版/石製按鈕。");
			p.sendMessage("§73. 防止Creeper/TNT炸掉保護區。");
			p.sendMessage("§73. 防止黃金磚被活塞推動。");
			p.sendMessage("§c特別警告以下為可執行事件: ");
			p.sendMessage("§71. 怪物仍可觸發石製壓力版");
			p.sendMessage("§72. 使用木製門/木製按鈕/木製踏板/控制桿/活塞");
			p.sendMessage("§73. 打掉下面方塊使會掉落之方塊掉落");
			return;
		}
		String str;
		if(!data.checkCupboardExist(event.getClickedBlock())){
			str="此金磚非由玩家放置或資料遺失，請拆除後重新放置";
		}else if(data.toggleBoardAccess(p, event.getClickedBlock())){
			str="已授權";
		} else {
			str="已取消授權";
		}
		event.setCancelled(true);
		Util.msgToPlayer(p, str);
    }
    
    //==========以下為保護措施=========
    
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
    @EventHandler(priority = EventPriority.HIGH)
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
    @EventHandler(priority = EventPriority.HIGH)
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
    @EventHandler(priority = EventPriority.HIGH)
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
    @EventHandler(priority = EventPriority.HIGH)
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
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event){
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(data.checkIsLimit(block)){
				event.blockList().remove(block);
    		}
    	}
    }
    
    //模組爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(BlockExplodeEvent event){
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(data.checkIsLimit(block)){
    			event.blockList().remove(block);
    		}
    	}
    }
    
    //防止方塊被燒壞
    // TODO 火焰不會消失
    @EventHandler(priority = EventPriority.HIGH)
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
