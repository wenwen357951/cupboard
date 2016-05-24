package com.mics.spigotPlugin.cupboard.listener;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.Util;

public class GoldBlockListener implements Listener {
	private Cupboard plugin;

	public GoldBlockListener(Cupboard instance)
	{
	    this.plugin = instance;
	    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	    this.plugin.logDebug("GoldBlockListener Registed.");
	}
	//移除金磚
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGoldBlockBreak(BlockBreakEvent event){
    	if(event.isCancelled())return;
    	if(event.getBlock().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		plugin.data.removeCupboard(event.getBlock());
    		Util.msgToPlayer(p, "金磚已拆除");
    	}
    }
    
    //放置金磚
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGoldBlockPlace(BlockPlaceEvent event){
    	if(event.isCancelled())return;
    	if(event.getBlockPlaced().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		if(!plugin.data.putCupboard(event.getBlockPlaced(), p)){
    			Util.msgToPlayer(p, "距離其他金磚太近。");
    			event.setCancelled(true);
    			return;
    		}
    		Util.msgToPlayer(p, "金磚已放置並取得授權。(潛行右鍵取得說明)");
    		//Util.msgToPlayer(p, "*** 貼心提醒: 金磚並不防止被活塞推動。 ***");
    	}
    }
    
  //右鍵金磚 授權/取消授權
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event){
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;                    		// off hand packet, ignore.
    	if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.GOLD_BLOCK) return;       	// 非黃金磚則無視
    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return; 						// 非右鍵方塊則無視
        //if (event.getItem() != null && event.getItem().getType().isBlock()) return;   	// 非空手則無視
    	Location front_block_loc = event.getClickedBlock().getLocation().clone();
		Player p = event.getPlayer();
    	switch(event.getBlockFace()){
    	case EAST:
    		front_block_loc.add(1,0,0);
    		break;
    	case WEST:
    		front_block_loc.add(-1,0,0);
    		break;
    	case SOUTH:
    		front_block_loc.add(0,0,1);
    		break;
    	case NORTH:
    		front_block_loc.add(0,0,-1);
    		break;
    	case UP:
    		front_block_loc.add(0,1,0);
    		break;
    	case DOWN:
    		front_block_loc.add(0,-1,0);
    		break;
		default:
			break;
    	}
    	if(front_block_loc.getBlock().getType().isSolid()){
    		p.sendMessage("被擋住了，無法授權/取消授權。");
    		return;
    	}
    	
		if (p.isSneaking()){
			//說明
			p.sendMessage("§a金磚放置後產生19x19x19的方形保護區，金磚在正中央。");
			p.sendMessage("§a保護區只防止下列事項: ");
			p.sendMessage("§71. 未授權者無法放置/移除方塊。");
			p.sendMessage("§72. 未授權者無法使用石製壓力版/石製按鈕。");
			p.sendMessage("§73. 怪物/動物無法觸發石製壓力版");
			p.sendMessage("§74. 防止任何種類爆炸炸掉保護區。");
			p.sendMessage("§75. 防止金磚被活塞推動。");
			p.sendMessage("§c特別警告以下為可執行事件: ");
			p.sendMessage("§72. 使用木製門/按鈕/壓力版/箱子/控制桿/活塞/柵欄門");
			p.sendMessage("§73. 打掉方塊使上方會掉落之方塊掉落");
			return;
		}
		String str;
		if(!plugin.data.checkCupboardExist(event.getClickedBlock())){
			str="此金磚非由玩家放置或資料遺失，請拆除後重新放置";
		} else if(plugin.data.toggleBoardAccess(p, event.getClickedBlock())){
			str="已授權";
		} else {
			str="已取消授權";
		}
		event.setCancelled(true);
		p.updateInventory();
		Util.msgToPlayer(p, str);
    }
}
