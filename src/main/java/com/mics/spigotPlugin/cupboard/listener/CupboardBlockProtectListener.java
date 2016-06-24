package com.mics.spigotPlugin.cupboard.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.config.Config;
import com.mics.spigotPlugin.cupboard.config.Locales;
import com.mics.spigotPlugin.cupboard.data.CupboardsData;

public class CupboardBlockProtectListener extends MyListener {
	private CupboardsData data;
	public CupboardBlockProtectListener(Cupboard instance)
	{
	    super(instance);
	    this.data = this.plugin.cupboards;
	}
    
	//防止其他玩家破壞方塊
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		if(this.plugin.isOP(p))return;
        		e.setCancelled(true);
    			p.sendMessage(Locales.NO_ACCESS.getString());
        	}
        } else {
        	if(data.checkIsLimit(b)){
        		e.setCancelled(true);
        	}
        }
    }
    //防止其他玩家放置方塊
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
            	if(this.plugin.isOP(p))return;
            	if(Config.TNT_SP_ENABLE.getBoolean() && e.getBlockPlaced().getType().equals(Material.TNT)){
            		e.getBlockPlaced().setType(e.getBlockReplacedState().getType());
            		b.getWorld().spawn(e.getBlockPlaced().getLocation().add(0.5,0,0.5), TNTPrimed.class);
            		return;
            	}
        		e.setCancelled(true);
    			p.sendMessage(Locales.NO_ACCESS.getString());
        	}
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
    
 // 防止方塊被燒壞
    @EventHandler
    void onBlockBurnDamage(BlockBurnEvent e){
    	Block b = e.getBlock();
    	if(data.checkIsLimit(b)){
    		e.setCancelled(true);
    		Location l = e.getBlock().getLocation();
    		if(Math.random() < 0.1){ //當物品要損壞時10%機率火焰消失
    			if(l.add(1,0,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(-2,0,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(1,1,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,-2,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,1,1).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,0,-2).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    		}
    	}
    }

    @EventHandler
    void onFireSpread(BlockSpreadEvent e){
    	if(e.getSource().getType() != Material.FIRE) return;
    	if(data.checkIsLimit(e.getBlock())){
    		e.getSource().setType(Material.AIR);
    	}
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
