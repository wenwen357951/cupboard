package tw.mics.spigot.plugin.cupboard.listener;

import java.util.Arrays;

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
import org.bukkit.event.vehicle.VehicleDamageEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;

public class CupboardEntityProtectListener extends MyListener {
	final static private Material[] protect_vehicle = {
	    Material.ARMOR_STAND,
        Material.BOAT,
        Material.BOAT_ACACIA,
        Material.BOAT_BIRCH,
        Material.BOAT_DARK_OAK,
        Material.BOAT_JUNGLE,
        Material.BOAT_SPRUCE,
        Material.MINECART ,
        Material.COMMAND_MINECART,
        Material.EXPLOSIVE_MINECART,
        Material.HOPPER_MINECART,
        Material.POWERED_MINECART,
        Material.STORAGE_MINECART,
	};
	private CupboardsData data;

	public CupboardEntityProtectListener(Cupboard instance)
	{
		super(instance);
		
	    this.data = this.plugin.cupboards;
	}
	
	//保護船隻 / 礦車
	@EventHandler
	public void onVehicleDestroy(VehicleDamageEvent event) {
        if(event.getAttacker() instanceof Player){
    	    Location bl = event.getVehicle().getLocation().getBlock().getLocation();
            Player p = (Player) event.getAttacker();
            if(data.checkIsLimit(bl, p)){
                if(this.plugin.isOP(p)) return;
                event.setCancelled(true);
                p.updateInventory();
            }
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
    public void onPlayerInteract(PlayerInteractEntityEvent e){
        if(e.getRightClicked().getType().isAlive()) return; //不阻止活著的生物
        Location bl = e.getRightClicked().getLocation().getBlock().getLocation();
        Player p = e.getPlayer();
        if(data.checkIsLimit(bl, p)){
            if(this.plugin.isOP(p)) return;
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }
    
    //防止未授權玩家和物件互動
    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent e){
        if(e.getRightClicked().getType().isAlive()) return; //不阻止活著的生物
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
			Arrays.asList(protect_vehicle).contains(e.getItem().getType())
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
