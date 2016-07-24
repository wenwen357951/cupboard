package tw.mics.spigot.plugin.cupboard.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;

public class CupboardBlockProtectListener extends MyListener {
	private CupboardsData data;
	public CupboardBlockProtectListener(Cupboard instance)
	{
	    super(instance);
	    this.data = this.plugin.cupboards;
	}
	

    private void setUpTNT(Location l){
        TNTPrimed tnt = l.getWorld().spawn(l, TNTPrimed.class);
        tnt.setGravity(false);
        tnt.setGlowing(true);
        tnt.setVelocity(new Vector(0, 0, 0));
        tnt.setFuseTicks(200);
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
            } else if(Config.TNT_SP_ENABLE.getBoolean() && b.getType().equals(Material.TNT) && p.getGameMode() == GameMode.SURVIVAL){
                b.setType(Material.AIR);
                setUpTNT(b.getLocation().add(0.5,0,0.5));
                e.setCancelled(true);
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
            if(Config.TNT_SP_ENABLE.getBoolean() && e.getBlockPlaced().getType().equals(Material.TNT) && p.getGameMode() == GameMode.SURVIVAL){
                e.getBlockPlaced().setType(e.getBlockReplacedState().getType());
                setUpTNT(b.getLocation().add(0.5,0,0.5));
                return;
            }
            if(!e.isCancelled()){
                if(b.getType().equals(Material.PISTON_BASE) || b.getType().equals(Material.PISTON_STICKY_BASE)){
                    b.setMetadata("owner_uuid", new FixedMetadataValue(plugin, p.getUniqueId().toString()));
                }
            }
        }
    }
    
    //禁止玩家與限制區域互動
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(b.getType() == Material.GOLD_BLOCK) return;              //金磚給金磚專區判斷
        
        if(data.checkIsLimit(b, p)){
            if(this.plugin.isOP(p)) return;
            p.sendMessage(Locales.NO_ACCESS.getString());
            if( b.getType() == Material.WATER || b.getType() == Material.LAVA)p.updateInventory();
            event.setCancelled(true);
            return;
        }
            
        //禁止未授權玩家使用床，其他則記錄重生點
        if(    
            !SpawnLocationManager.checkPlayerSpawn(b.getLocation(), p) &&
            b.getType() == Material.BED_BLOCK
        ){
            p.setBedSpawnLocation(b.getLocation());
            p.sendMessage(Locales.BED_SPAWN_SET.getString());
            event.setCancelled(true);
            return;
        }
    }
    
    //禁止物件和限制方塊互動
    @EventHandler
    public void onEntryUseStonePlate(EntityInteractEvent event){
    Block b = event.getBlock();
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
    
    // 防止火焰
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
    

    
    //防止活塞
    @EventHandler
    void onPistonExtend(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(block.getType().equals(Material.GOLD_BLOCK)){
                e.setCancelled(true);
                return;
            }
            if(Config.WP_TNT_NO_PISTON.getBoolean() && block.getType().equals(Material.TNT)){
                e.setCancelled(true);
                return;
            }
            if(e.getBlock().hasMetadata("owner_uuid")){
                String uuid = e.getBlock().getMetadata("owner_uuid").get(0).asString();
                if(plugin.cupboards.checkIsLimitByUUIDString(block, uuid)){
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    void onPistonRetract(BlockPistonRetractEvent e){
        for(Block block : e.getBlocks()){
            if(block.getType().equals(Material.GOLD_BLOCK)){
                e.setCancelled(true);
                return;
            }
            if(Config.WP_TNT_NO_PISTON.getBoolean() && block.getType().equals(Material.TNT)){
                e.setCancelled(true);
                return;
            }
            if(e.getBlock().hasMetadata("owner_uuid")){
                String uuid = e.getBlock().getMetadata("owner_uuid").get(0).asString();
                if(plugin.cupboards.checkIsLimitByUUIDString(block, uuid)){
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }    
}
