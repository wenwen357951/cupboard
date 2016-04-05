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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
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
    			//Util.msgToPlayer(p, "工具櫃放置失敗");
    			event.setCancelled(true);
    			return;
    		}
    		//Util.msgToPlayer(p, "已放置工具櫃");
    	}
    }

    
    //授權/取消授權
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event){
    	if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
	    	if(event.getClickedBlock().getType() == Material.GOLD_BLOCK){
	    		Player p = event.getPlayer();
    			String str;
    			if(!data.checkCupboardExist(event.getClickedBlock())){
    				str="此方塊並非由玩家放置或資料遺失，請拆除後重新放置";
    			}else if(data.toggleBoardAccess(p, event.getClickedBlock())){
    				str="工具櫃已授權";
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

    //防止火焰燃燒
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onBlockIgnite(BlockIgniteEvent e)
    {
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
    
    //防止方塊被燒壞
    // TODO 火焰不會消失
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onBlockBurnDamage(BlockBurnEvent e){
    	Block b = e.getBlock();
    	if(data.checkIsLimit(b))
    		e.setCancelled(true);
    }
    
    
}
