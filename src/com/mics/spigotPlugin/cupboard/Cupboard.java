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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
    		Util.msgToPlayer(p, "已移除工具櫃");
    	}
    }
    
    //放置金磚
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGoldBlockPlace(BlockPlaceEvent event){
    	if(event.getBlockPlaced().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		if(!data.putCupboard(event.getBlockPlaced(), p)){
    			Util.msgToPlayer(p, "工具櫃放置失敗");
    			event.setCancelled(true);
    			return;
    		}
    		Util.msgToPlayer(p, "已放置工具櫃");
    	}
    }

    
    //授權/取消授權
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event){
    	if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    	if(event.getClickedBlock().getType() == Material.GOLD_BLOCK){
	    		Player p = event.getPlayer();
    			String str;
    			if(data.toggleBoardAccess(p, event.getClickedBlock())){
    				str="已授權";
    			} else {
    				str="已取消授權";
    			}
	    		Util.msgToPlayer(p, str);
	    	}
    	}
    }
    
    //==========以下為保護措施=========
    
    //防止其他玩家破壞方塊
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
    	if(data.checkIsLimit(event.getBlock(), event.getPlayer()))
    		event.setCancelled(true);
    }
    //防止其他玩家放置方塊
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event){
    	if(data.checkIsLimit(event.getBlock(), event.getPlayer()))
    		event.setCancelled(true);
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
}
