package com.mics.spigotPlugin.cupboard.listener;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mics.spigotPlugin.cupboard.Cupboard;
import com.mics.spigotPlugin.cupboard.Data;

public class CupboardEntityProtectListener implements Listener {
	private Cupboard plugin;
	ArrayList<Material> protect_vehicle;
	public Data data;

	public CupboardEntityProtectListener(Cupboard instance)
	{
	    this.plugin = instance;
	    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	    this.plugin.logDebug("CupboardUseProtectListener Registed.");
	    this.protect_vehicle = this.plugin.protect_vehicle;
	    this.data = this.plugin.data;
	}


    //防止Hanging類物品被未授權玩家放置
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
		Player p = e.getPlayer();
		if(data.checkIsLimit(bl, p)){
			if(this.plugin.isOP(p)) return;
			e.setCancelled(true);
			e.getPlayer().updateInventory();
		}
	}
    
    //防止Hanging類物品被未授權玩家移除
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
    	//NEEDFIX -- TNT LIGHT BY ALLOW USER WILL DESTORY HANGING ITEM
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
    	if (e.getRemover() instanceof Player){
    		Player p = (Player) e.getRemover();
    		if(this.plugin.data.checkIsLimit(bl, p)){
    			if(this.plugin.isOP(p)) return;
    			e.setCancelled(true);
    		}
    	}
	}
    

    //防止船隻/礦車/盔甲架被放置
    @EventHandler
    public void onBoatPlace(PlayerInteractEvent e){
    	if (
			e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			e.getItem() != null &&
			protect_vehicle.contains(e.getItem().getType())
		){
    	Player p = e.getPlayer();
	    	if(this.plugin.data.checkIsLimit(e.getClickedBlock(), p)){
	    		if(this.plugin.isOP(p))return;
	    		e.setCancelled(true);
	    		e.getPlayer().updateInventory();
	    	}
    	}
    }
    
    //防止盔甲架被移除
    @EventHandler
    public void onArmorStandDamage(EntityDamageByEntityEvent e){
    	if (e.getEntity().getType() != EntityType.ARMOR_STAND) return;
    	if (e.getDamager() instanceof Player){
    		Player p = (Player) e.getDamager();
        	if (this.plugin.data.checkIsLimit(e.getEntity().getLocation().getBlock(), p)){
        		if(this.plugin.isOP(p))return;
        		e.setCancelled(true);
        	}
    	}
    }

}
