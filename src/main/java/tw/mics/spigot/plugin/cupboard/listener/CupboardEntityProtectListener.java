package tw.mics.spigot.plugin.cupboard.listener;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;

public class CupboardEntityProtectListener extends MyListener {
	private ArrayList<Material> protect_vehicle;
	private CupboardsData data;

	public CupboardEntityProtectListener(Cupboard instance)
	{
		super(instance);
		
		setUpProtectEntity();
	    this.data = this.plugin.cupboards;
	}
	
    private void setUpProtectEntity(){
    	protect_vehicle = new ArrayList<Material>();
    	protect_vehicle.add(Material.ARMOR_STAND);
    	protect_vehicle.add(Material.BOAT);
    	protect_vehicle.add(Material.MINECART );
    	protect_vehicle.add(Material.COMMAND_MINECART );
    	protect_vehicle.add(Material.EXPLOSIVE_MINECART );
    	protect_vehicle.add(Material.HOPPER_MINECART );
    	protect_vehicle.add(Material.POWERED_MINECART );
    	protect_vehicle.add(Material.STORAGE_MINECART );
    }
    //防止未授權玩家和物件互動
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e){
        Location bl = e.getRightClicked().getLocation().getBlock().getLocation();
        Player p = e.getPlayer();
        if(data.checkIsLimit(bl, p)){
            if(this.plugin.isOP(p)) return;
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }
    
    //保護物品展示框
    @EventHandler
    public void onPlayerHitEntity(EntityDamageByEntityEvent e){
        if(e.getEntityType() != EntityType.ITEM_FRAME) return;
        if(e.getDamager() instanceof Player){
            Location bl = e.getEntity().getLocation().getBlock().getLocation();
            Player p = (Player) e.getDamager();
            if(data.checkIsLimit(bl, p)){
                if(this.plugin.isOP(p)) return;
                e.setCancelled(true);
                p.updateInventory();
            }
        }
    }
    
    //防止未授權玩家和物件互動
    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent e){
        Location bl = e.getRightClicked().getLocation().getBlock().getLocation();
        Player p = e.getPlayer();
        if(data.checkIsLimit(bl, p)){
            if(this.plugin.isOP(p)) return;
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
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
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
    	if (e.getRemover() instanceof Player){
    		Player p = (Player) e.getRemover();
    		if(this.plugin.cupboards.checkIsLimit(bl, p)){
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
	    	if(this.plugin.cupboards.checkIsLimit(e.getClickedBlock(), p)){
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
        	if (this.plugin.cupboards.checkIsLimit(e.getEntity().getLocation().getBlock(), p)){
        		if(this.plugin.isOP(p))return;
        		e.setCancelled(true);
        	}
    	}
    }

}
