package tw.mics.spigot.plugin.cupboard.listener;

import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
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
    
    
    private final static Material[] doors = {
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR,
            Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR,
            Material.TRAP_DOOR,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK,
    };
    //防止其他玩家破壞方塊
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if(b.getType() == Material.GOLD_BLOCK) return; //拆除金磚不防止
        if( p != null){
            if(data.checkIsLimit(b, p)){
                if(this.plugin.isOP(p))return;
                e.setCancelled(true);
                p.sendMessage(Locales.NO_ACCESS.getString());
                //DOOR
                if(Arrays.asList(doors).contains(b.getType())){
                    Location resend_location = b.getLocation().add(0, 1, 0);
                    p.sendBlockChange(resend_location, resend_location.getBlock().getType(), resend_location.getBlock().getData());
                }
                
                //SIGN
                if(b.getType() == Material.SIGN){
                    Location resend_location = b.getLocation();
                    p.sendBlockChange(resend_location, resend_location.getBlock().getType(), resend_location.getBlock().getData());
                }
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
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if( p != null){
            if(Config.TNT_SP_ENABLE.getBoolean() && e.getBlockPlaced().getType().equals(Material.TNT) && p.getGameMode() == GameMode.SURVIVAL){
                e.getBlockPlaced().setType(e.getBlockReplacedState().getType());
                setUpTNT(b.getLocation().add(0.5,0,0.5));
                return;
            } else if(data.checkIsLimit(b, p)){
                if(this.plugin.isOP(p))return;
                p.sendMessage(Locales.NO_ACCESS.getString());
                e.setCancelled(true);
            } else if(
                    b.getType().equals(Material.BED_BLOCK) &&
                    b.getWorld().getEnvironment() == Environment.NORMAL &&
                    !SpawnLocationManager.checkPlayerSpawn(b.getLocation(), p)
            ){
                p.setBedSpawnLocation(b.getLocation());
                p.sendMessage(Locales.BED_SPAWN_SET.getString());
            }
        }
    }
    
  //防止玩家使用水桶
    @EventHandler
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
    @EventHandler
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
    
    private final static Material[] limitInteractBlocks = {
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR,
            Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR,
            Material.TRAP_DOOR,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR,
            Material.FENCE_GATE,
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.LEVER,
            Material.STONE_BUTTON,
            Material.WOOD_BUTTON,
            Material.REDSTONE_COMPARATOR_OFF,
            Material.REDSTONE_COMPARATOR_ON,
            Material.DIODE_BLOCK_OFF,
            Material.DIODE_BLOCK_ON,
            Material.DAYLIGHT_DETECTOR,
            Material.DAYLIGHT_DETECTOR_INVERTED,
            Material.DIODE_BLOCK_ON,
            Material.REDSTONE,
            Material.BED_BLOCK,
            Material.ENDER_CHEST,
            Material.ANVIL,
            Material.CAKE_BLOCK,
    };
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(!(
                    (b.getState() instanceof InventoryHolder) ||
                    (Arrays.asList(limitInteractBlocks).contains(b.getType()))
            )) return;

            //禁止玩家與限制區域互動
            if(data.checkIsLimit(b, p)){
                if(this.plugin.isOP(p)) return;
                p.sendMessage(Locales.NO_ACCESS.getString());
                if( b.getType() == Material.WATER || b.getType() == Material.LAVA)p.updateInventory();
                event.setCancelled(true);
                return;
            }
                
            //禁止未授權玩家使用床，其他則記錄重生點
            if(    
                b.getType() == Material.BED_BLOCK &&
                b.getWorld().getEnvironment() == Environment.NORMAL &&
                !SpawnLocationManager.checkPlayerSpawn(b.getLocation(), p) 
            ){
                p.setBedSpawnLocation(b.getLocation());
                p.sendMessage(Locales.BED_SPAWN_SET.getString());
                event.setCancelled(true);
                return;
            }
        } else if(event.getAction() == Action.PHYSICAL){
            if(data.checkIsLimit(b, p)){
                if(b.getType() == Material.TRIPWIRE || b.getType() == Material.TRIPWIRE_HOOK) return;
                if(this.plugin.isOP(p, false)) return;
                event.setCancelled(true);
                return;
            }
        }
    }
    
    //禁止物件和限制區域互動
    @EventHandler
    public void onEntryInteract(EntityInteractEvent event){
        Block b = event.getBlock();
        Entity e = event.getEntity();
        Player p = null;
        
        if(b.getType() == Material.TRIPWIRE || b.getType() == Material.TRIPWIRE_HOOK) return;
        
        //Maybe can add others
        if(e.getPassenger() instanceof Player){
            p = (Player) e.getPassenger();
        }
        
        if (p != null){
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
        e.setCancelled(checkPiston(e.getBlocks(), e.getBlock(), true, e.getDirection()));
    }

    @EventHandler
    void onPistonRetract(BlockPistonRetractEvent e){
        e.setCancelled(checkPiston(e.getBlocks(), e.getBlock()));
    }
    
    private Material[] rails = {
            Material.RAILS,
            Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL,
            Material.POWERED_RAIL,
    };
    
    private Material[] ores = {
            Material.COAL_ORE,
            Material.COAL_BLOCK,
            Material.IRON_ORE,
            Material.IRON_BLOCK,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.DIAMOND_BLOCK,
            Material.EMERALD_ORE,
            Material.EMERALD_BLOCK,
            Material.LAPIS_ORE,
            Material.LAPIS_BLOCK,
            Material.REDSTONE_ORE,
            Material.GLOWING_REDSTONE_ORE,
    };
    private boolean checkPiston(List<Block> blocks, Block piston){
        return checkPiston(blocks, piston, false, null);
    }
    private boolean checkPiston(List<Block> blocks, Block piston, Boolean push, BlockFace direction){
        boolean check_flag = false;
        String uuid = null;
        if(piston.hasMetadata("owner_uuid")){
            uuid = piston.getMetadata("owner_uuid").get(0).asString();
            check_flag = true;
        }
        if(push && check_flag){
            Location l = piston.getLocation().clone();
            switch(direction){
            case UP:
                l.add(0, (blocks.size()+1), 0);
                break;
            case DOWN:
                l.add(0, -(blocks.size()+1), 0);
                break;
            case WEST:
                l.add(-(blocks.size()+1), 0, 0);
                break;
            case EAST:
                l.add((blocks.size()+1), 0, 0);
                break;
            case NORTH:
                l.add(0, 0, -(blocks.size()+1));
                break;
            case SOUTH:
                l.add(0, 0, (blocks.size()+1));
                break;
            default:
                break;
            }
            if(plugin.cupboards.checkIsLimitByUUIDString(l, uuid)){
                return true;
            }
        }
        for(Block block : blocks){
            if(block.getType().equals(Material.GOLD_BLOCK)){
                return true;
            }
            if(Config.WP_TNT_NO_PISTON.getBoolean() && block.getType().equals(Material.TNT)){
                return true;
            }
            if(Config.WP_RAILS_NO_PISTON.getBoolean() && Arrays.asList(rails).contains(block.getType())){
                return true;
            }
            if(Config.WP_ORES_NO_PISTON.getBoolean() && Arrays.asList(ores).contains(block.getType())){
                return true;
            }
            if(check_flag){
                if(plugin.cupboards.checkIsLimitByUUIDString(block, uuid)){
                    return true;
                } else {
                    block.setMetadata("owner_uuid", new FixedMetadataValue(plugin, uuid));
                }
            }
        }
        return false;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if( p != null){
            if(!e.isCancelled()){
                if(b.getType().equals(Material.PISTON_BASE) || b.getType().equals(Material.PISTON_STICKY_BASE)){
                    b.setMetadata("owner_uuid", new FixedMetadataValue(plugin, p.getUniqueId().toString()));
                }
            }
        }
    }
}
